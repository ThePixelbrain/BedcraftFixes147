package uk.bedcraft.bedcraftfixes.ic2;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import uk.bedcraft.bedcraftfixes.BedcraftFixesPremain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class UXELEnergyNet {
    private static final ExecutorService WORKERS = Executors.newFixedThreadPool(16, r -> {
        Thread t = new Thread(r);
        t.setName("UXEL IC2 Energy net worker");
        return t;
    });
    private static final ExecutorService LAUNCHER = Executors.newSingleThreadExecutor();
    private final AtomicInteger launching = new AtomicInteger();
    private long globalTick = 0L;
    private final Random rng = new Random();
    private final ConcurrentMap<TileEntity, UXELEnergyNet.NetLocal> sourceToTargetMap = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static final Direction[] directions = Direction.values();

    public void tickStart() {
        if (this.launching.get() <= 0) {
            this.launching.set(1);
            this.globalTick++;
            LAUNCHER.submit(new Runnable() {
                @Override
                public void run() {
                    UXELEnergyNet.this.triggerUpdate();
                }
            });
        }
    }

    private long updatePath(UXELEnergyNet.NetLocal local, UXELEnergyNet.EnergyPath path, LinkedList<UXELEnergyNet.EnergyPath> active) {
        IEnergySink energySink = (IEnergySink)path.target;
        int demanded = energySink.demandsEnergy();
        if (demanded <= 0) {
            return 0L;
        }

        int vmax = Math.min(energySink.getMaxSafeInput(), path.minConductorBreakdownEnergy - 1);
        if (vmax <= 0) {
            return 0L;
        }

        int canOutput = Math.min(local.buffer, vmax);
        int loss = (int)Math.floor(path.loss);
        loss = Math.max(0, loss);
        if (loss >= canOutput) {
            return 0L;
        }

        int toOutput = Math.min(demanded, canOutput);
        if (toOutput < canOutput) {
            toOutput += loss;
            toOutput = Math.min(toOutput, canOutput);
        }

        if (toOutput - loss <= 0) {
            return 0L;
        }

        int returned = energySink.injectEnergy(path.targetDirection, toOutput - loss);
        int consumed;
        if (returned <= 0) {
            consumed = toOutput;
        } else if (returned > toOutput) {
            consumed = loss;
        } else {
            consumed = toOutput - returned;
        }

        path.totalEnergyConducted += toOutput;
        local.buffer -= consumed;
        active.add(path);
        return consumed;
    }

    public void updateLocal(UXELEnergyNet.NetLocal local, List<UXELEnergyNet.EnergyPath> paths) {
        try {
            if (this.rng.nextInt(20) == 0) {
                Collections.shuffle(paths);
            }

            LinkedList<UXELEnergyNet.EnergyPath> activePaths = new LinkedList<>();
            long taken = 0L;

            for (UXELEnergyNet.EnergyPath path : paths) {
                if (local.buffer <= 0) {
                    return;
                }

                taken = this.updatePath(local, path, activePaths);
            }

            if (taken != 0L) {
                int bursts = local.bursts.getAndSet(0);

                for (int i = 0; i < bursts; i++) {
                    taken = 0L;
                    LinkedList<UXELEnergyNet.EnergyPath> newActivePaths = new LinkedList<>();

                    for (UXELEnergyNet.EnergyPath path : activePaths) {
                        if (local.buffer <= 0) {
                            return;
                        }

                        taken = this.updatePath(local, path, newActivePaths);
                    }

                    if (taken == 0L) {
                        return;
                    }

                    activePaths = newActivePaths;
                }
            }
        } catch (Throwable exc) {
            BedcraftFixesPremain.log.warn("Error while trying to update local energy", exc);
        } finally {
            this.launching.decrementAndGet();
        }
    }

    public void triggerUpdate() {
        try {
            List<TileEntity> toUnload = null;

            for (Entry<TileEntity, UXELEnergyNet.NetLocal> local : this.sourceToTargetMap.entrySet()) {
                final UXELEnergyNet.NetLocal fnet = local.getValue();
                if (this.globalTick - fnet.energyTick > 6000L) {
                    if (toUnload == null) {
                        toUnload = new LinkedList<>();
                    }

                    toUnload.add(local.getKey());
                } else if (fnet.buffer > 0) {
                    final List<UXELEnergyNet.EnergyPath> fPath = fnet.targets;
                    if (fPath != null && !fPath.isEmpty()) {
                        this.launching.incrementAndGet();
                        WORKERS.submit(new Runnable() {
                            @Override
                            public void run() {
                                UXELEnergyNet.this.updateLocal(fnet, fPath);
                            }
                        });
                    }
                }
            }

            if (toUnload != null) {
                for (TileEntity unload : toUnload) {
                    BedcraftFixesPremain.log.info(
                                    "Removing tile entity "
                                            + unload.xCoord
                                            + " "
                                            + unload.yCoord
                                            + " "
                                            + unload.zCoord
                                            + " from energy net as it has not been outputting energy for 6000 ticks."
                            );
                    this.sourceToTargetMap.remove(unload);
                }
            }
        } catch (Throwable exc) {
            BedcraftFixesPremain.log.warn("Error while trying to update local energy", exc);
        } finally {
            this.launching.decrementAndGet();
        }
    }

    public void addTileEntity(TileEntity addedTileEntity) {
        if (addedTileEntity instanceof IEnergyTile) {
            this.lock.lock();

            try {
                if (((IEnergyTile)addedTileEntity).isAddedToEnergyNet()) {
                    return;
                }

                if (addedTileEntity instanceof IEnergyAcceptor) {
                    for (UXELEnergyNet.EnergyPath reverseEnergyPath : this.discover(addedTileEntity, true, Integer.MAX_VALUE)) {
                        UXELEnergyNet.NetLocal local = this.sourceToTargetMap.get(reverseEnergyPath.target);
                        if (local != null) {
                            local.targets = null;
                        }
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    public void removeTileEntity(TileEntity removedTileEntity) {
        if (removedTileEntity instanceof IEnergyTile) {
            this.lock.lock();

            try {
                this.sourceToTargetMap.remove(removedTileEntity);
                if (!((IEnergyTile)removedTileEntity).isAddedToEnergyNet()) {
                    BedcraftFixesPremain.log.info("Removing " + removedTileEntity + " from the EnergyNet failed, already removed.");
                    return;
                }

                if (removedTileEntity instanceof IEnergyAcceptor) {
                    for (UXELEnergyNet.EnergyPath reverseEnergyPath : this.discover(removedTileEntity, true, Integer.MAX_VALUE)) {
                        UXELEnergyNet.NetLocal local = this.sourceToTargetMap.get(reverseEnergyPath.target);
                        if (local != null) {
                            local.targets = null;
                        }
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    public int emitEnergyFrom(IEnergySource energySource, int amount) {
        NetLocal netLocal = (NetLocal)this.sourceToTargetMap.get((TileEntity)energySource);
        if (netLocal == null || netLocal.targets == null) {
            if (!this.lock.tryLock()) {
                return amount;
            }

            try {
                List<EnergyPath> paths = this.discover((TileEntity)energySource, false, energySource.getMaxEnergyOutput());
                if (netLocal == null) {
                    netLocal = new NetLocal();
                    netLocal.source = (TileEntity)energySource;
                    netLocal.energyTick = this.globalTick;
                    this.sourceToTargetMap.putIfAbsent((TileEntity)energySource, netLocal);
                    netLocal = (NetLocal)this.sourceToTargetMap.get(energySource);
                }

                netLocal.targets = new ArrayList<>(paths);
            } finally {
                this.lock.unlock();
            }
        }

        netLocal.energyTick = this.globalTick;
        if (amount <= 0) {
            return amount;
        } else {
            int max = amount * 20 - Math.max(0, netLocal.buffer);
            if (max <= 0) {
                return amount;
            } else {
                int amt = Math.min(amount, max);
                netLocal.buffer += amt;
                netLocal.bursts.incrementAndGet();
                return amount - amt;
            }
        }
    }

    public long getTotalEnergyConducted(TileEntity tileEntity) {
        long ret = 0L;
        if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink) {
            for (UXELEnergyNet.EnergyPath reverseEnergyPath : this.discover(tileEntity, true, Integer.MAX_VALUE)) {
                IEnergySource energySource = (IEnergySource)reverseEnergyPath.target;
                UXELEnergyNet.NetLocal netLocal = this.sourceToTargetMap.get(energySource);
                if (netLocal != null) {
                    List<UXELEnergyNet.EnergyPath> paths = netLocal.targets;
                    if (paths != null) {
                        for (UXELEnergyNet.EnergyPath energyPath : paths) {
                            if (tileEntity instanceof IEnergySink && energyPath.target == tileEntity
                                    || tileEntity instanceof IEnergyConductor && energyPath.conductors.contains(tileEntity)) {
                                ret += energyPath.totalEnergyConducted;
                            }
                        }
                    }
                }
            }
        }

        if (tileEntity instanceof IEnergySource) {
            UXELEnergyNet.NetLocal netLocal = this.sourceToTargetMap.get(tileEntity);
            List<UXELEnergyNet.EnergyPath> paths = netLocal.targets;
            if (paths == null) {
                return ret;
            }

            for (UXELEnergyNet.EnergyPath energyPath : paths) {
                ret += energyPath.totalEnergyConducted;
            }
        }

        return ret;
    }

    public long getTotalEnergyEmitted(TileEntity tileEntity) {
        long ret = 0L;
        if (tileEntity instanceof IEnergyConductor) {
            for (UXELEnergyNet.EnergyPath reverseEnergyPath : this.discover(tileEntity, true, Integer.MAX_VALUE)) {
                IEnergySource energySource = (IEnergySource)reverseEnergyPath.target;
                UXELEnergyNet.NetLocal netLocal = this.sourceToTargetMap.get(energySource);
                if (netLocal != null) {
                    List<UXELEnergyNet.EnergyPath> paths = netLocal.targets;
                    if (paths != null) {
                        for (UXELEnergyNet.EnergyPath energyPath : paths) {
                            if (energyPath.conductors.contains(tileEntity)) {
                                ret += energyPath.totalEnergyConducted;
                            }
                        }
                    }
                }
            }
        }

        if (tileEntity instanceof IEnergySource) {
            UXELEnergyNet.NetLocal netLocal = this.sourceToTargetMap.get(tileEntity);
            if (netLocal == null) {
                return ret;
            }

            List<UXELEnergyNet.EnergyPath> paths = netLocal.targets;
            if (paths == null) {
                return ret;
            }

            for (UXELEnergyNet.EnergyPath energyPath : paths) {
                ret += energyPath.totalEnergyConducted;
            }
        }

        return ret;
    }

    public long getTotalEnergySunken(TileEntity tileEntity) {
        long ret = 0L;
        if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink) {
            for (UXELEnergyNet.EnergyPath reverseEnergyPath : this.discover(tileEntity, true, Integer.MAX_VALUE)) {
                IEnergySource energySource = (IEnergySource)reverseEnergyPath.target;
                UXELEnergyNet.NetLocal netLocal = this.sourceToTargetMap.get(energySource);
                if (netLocal != null) {
                    List<UXELEnergyNet.EnergyPath> paths = netLocal.targets;
                    if (paths != null) {
                        for (UXELEnergyNet.EnergyPath energyPath : paths) {
                            if (tileEntity instanceof IEnergySink && energyPath.target == tileEntity
                                    || tileEntity instanceof IEnergyConductor && energyPath.conductors.contains(tileEntity)) {
                                ret += energyPath.totalEnergyConducted;
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    public List<TileEntity> discoverTargets(TileEntity emitter, boolean reverse, int lossLimit) {
        List<UXELEnergyNet.EnergyPath> paths = this.discover(emitter, reverse, lossLimit);
        List<TileEntity> targets = new LinkedList<>();

        for (UXELEnergyNet.EnergyPath path : paths) {
            targets.add(path.target);
        }

        return targets;
    }

    private List<UXELEnergyNet.EnergyTarget> getValidReceivers(TileEntity emitter, boolean reverse) {
        List<UXELEnergyNet.EnergyTarget> validReceivers = new LinkedList<>();

        for (Direction direction : directions) {
            TileEntity target = direction.applyToTileEntity(emitter);
            if (target instanceof IEnergyTile && ((IEnergyTile)target).isAddedToEnergyNet()) {
                Direction inverseDirection = direction.getInverse();
                if ((
                        !reverse && emitter instanceof IEnergyEmitter && ((IEnergyEmitter)emitter).emitsEnergyTo(target, direction)
                                || reverse && emitter instanceof IEnergyAcceptor && ((IEnergyAcceptor)emitter).acceptsEnergyFrom(target, direction)
                )
                        && (
                        !reverse && target instanceof IEnergyAcceptor && ((IEnergyAcceptor)target).acceptsEnergyFrom(emitter, inverseDirection)
                                || reverse && target instanceof IEnergyEmitter && ((IEnergyEmitter)target).emitsEnergyTo(emitter, inverseDirection)
                )) {
                    validReceivers.add(new UXELEnergyNet.EnergyTarget(target, inverseDirection));
                }
            }
        }

        return validReceivers;
    }

    private List<UXELEnergyNet.EnergyPath> discover(TileEntity emitter, boolean reverse, int lossLimit) {
        Map<Object, Object> reachedTileEntities = new HashMap<>();
        LinkedList<TileEntity> tileEntitiesToCheck = new LinkedList<>();
        tileEntitiesToCheck.add(emitter);

        while (!tileEntitiesToCheck.isEmpty()) {
            TileEntity currentTileEntity = tileEntitiesToCheck.remove();
            if (!currentTileEntity.isInvalid()) {
                double currentLoss = 0.0;
                if (currentTileEntity != emitter) {
                    currentLoss = ((UXELEnergyNet.EnergyBlockLink)reachedTileEntities.get(currentTileEntity)).loss;
                }

                for (UXELEnergyNet.EnergyTarget validReceiver : this.getValidReceivers(currentTileEntity, reverse)) {
                    if (validReceiver.tileEntity != emitter) {
                        double additionalLoss = 0.0;
                        if (validReceiver.tileEntity instanceof IEnergyConductor) {
                            additionalLoss = ((IEnergyConductor)validReceiver.tileEntity).getConductionLoss();
                            if (additionalLoss < 1.0E-4) {
                                additionalLoss = 1.0E-4;
                            }

                            if (currentLoss + additionalLoss >= lossLimit) {
                                continue;
                            }
                        }

                        if (!reachedTileEntities.containsKey(validReceiver.tileEntity)
                                || ((UXELEnergyNet.EnergyBlockLink)reachedTileEntities.get(validReceiver.tileEntity)).loss > currentLoss + additionalLoss) {
                            reachedTileEntities.put(validReceiver.tileEntity, new UXELEnergyNet.EnergyBlockLink(validReceiver.direction, currentLoss + additionalLoss));
                            if (validReceiver.tileEntity instanceof IEnergyConductor) {
                                tileEntitiesToCheck.remove(validReceiver.tileEntity);
                                tileEntitiesToCheck.add(validReceiver.tileEntity);
                            }
                        }
                    }
                }
            }
        }

        List<UXELEnergyNet.EnergyPath> energyPaths = new LinkedList<>();

        for (Entry<Object, Object> entry : reachedTileEntities.entrySet()) {
            TileEntity tileEntity = (TileEntity)entry.getKey();
            if (!reverse && tileEntity instanceof IEnergySink || reverse && tileEntity instanceof IEnergySource) {
                UXELEnergyNet.EnergyBlockLink energyBlockLink = (UXELEnergyNet.EnergyBlockLink)entry.getValue();
                UXELEnergyNet.EnergyPath energyPath = new UXELEnergyNet.EnergyPath();
                if (energyBlockLink.loss > 0.1) {
                    energyPath.loss = energyBlockLink.loss;
                } else {
                    energyPath.loss = 0.1;
                }

                energyPath.target = tileEntity;
                energyPath.targetDirection = energyBlockLink.direction;
                if (!reverse && emitter instanceof IEnergySource) {
                    while (true) {
                        tileEntity = energyBlockLink.direction.applyToTileEntity(tileEntity);
                        if (tileEntity == emitter) {
                            break;
                        }

                        if (tileEntity instanceof IEnergyConductor) {
                            IEnergyConductor energyConductor = (IEnergyConductor)tileEntity;
                            energyPath.conductors.add(energyConductor);
                            if (energyConductor.getInsulationEnergyAbsorption() < energyPath.minInsulationEnergyAbsorption) {
                                energyPath.minInsulationEnergyAbsorption = energyConductor.getInsulationEnergyAbsorption();
                            }

                            if (energyConductor.getInsulationBreakdownEnergy() < energyPath.minInsulationBreakdownEnergy) {
                                energyPath.minInsulationBreakdownEnergy = energyConductor.getInsulationBreakdownEnergy();
                            }

                            if (energyConductor.getConductorBreakdownEnergy() < energyPath.minConductorBreakdownEnergy) {
                                energyPath.minConductorBreakdownEnergy = energyConductor.getConductorBreakdownEnergy();
                            }

                            energyBlockLink = (UXELEnergyNet.EnergyBlockLink)reachedTileEntities.get(tileEntity);
                            if (energyBlockLink == null) {
                                BedcraftFixesPremain.log.warn("An energy network pathfinding entry is corrupted.\n" +
                                        "This could happen due to incorrect Minecraft behavior or a bug.\n\n" +
                                        "(Technical information: energyBlockLink, tile entities below)\n" +
                                        "E: " + emitter + " (" + emitter.xCoord + "," + emitter.yCoord + "," + emitter.zCoord + ")\n" +
                                        "C: " + tileEntity + " (" + tileEntity.xCoord + "," + tileEntity.yCoord + "," + tileEntity.zCoord + ")\n" +
                                        "R: " + energyPath.target + " (" + energyPath.target.xCoord + "," + energyPath.target.yCoord + "," + energyPath.target.zCoord + ")");
                            }
                        } else if (tileEntity != null) {
                            BedcraftFixesPremain.log.warn("EnergyNet: EnergyBlockLink corrupted (" + energyPath.target + " [" + energyPath.target.xCoord + " " + energyPath.target.yCoord + " " + energyPath.target.zCoord + "] -> " + tileEntity + " [" + tileEntity.xCoord + " " + tileEntity.yCoord + " " + tileEntity.zCoord + "] -> " + emitter + " [" + emitter.xCoord + " " + emitter.yCoord + " " + emitter.zCoord + "])");
                        }
                    }
                }

                energyPaths.add(energyPath);
            }
        }

        return energyPaths;
    }

    static class EnergyBlockLink {
        Direction direction;
        double loss;

        EnergyBlockLink(Direction direction, double loss) {
            this.direction = direction;
            this.loss = loss;
        }
    }

    static class EnergyPath {
        TileEntity target;
        Direction targetDirection;
        Set<IEnergyConductor> conductors = new LinkedHashSet<>();
        double loss = 0.0;
        int minInsulationEnergyAbsorption = Integer.MAX_VALUE;
        int minInsulationBreakdownEnergy = Integer.MAX_VALUE;
        int minConductorBreakdownEnergy = Integer.MAX_VALUE;
        long totalEnergyConducted;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                UXELEnergyNet.EnergyPath path = (UXELEnergyNet.EnergyPath)o;
                return this.target.equals(path.target);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.target);
        }
    }

    static class EnergyTarget {
        TileEntity tileEntity;
        Direction direction;

        EnergyTarget(TileEntity tileEntity, Direction direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }
    }

    static class NetLocal {
        TileEntity source;
        volatile long energyTick;
        volatile List<UXELEnergyNet.EnergyPath> targets = new LinkedList<>();
        final AtomicInteger bursts = new AtomicInteger();
        volatile int buffer;
    }
}

