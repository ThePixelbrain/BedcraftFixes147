package uk.bedcraft.bedcraftfixes.redpower;

import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

import java.util.concurrent.locks.ReentrantLock;

@Patch.Class("com.eloraam.redpower.machine.TileDeploy")
@ConfigOptions("fixTTRedPowerDeployer")
public class TileDeployTransformer extends BedcraftMiniTransformer {

    @Patch.Method("enableTowards(Lcom/eloraam/redpower/core/WorldCoord;)V")
    public void patchEnableTowards(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(INVOKESTATIC(hooks(), "lock", "()V"));

        ctx.search(
                ALOAD(0),
                INVOKEVIRTUAL("com/eloraam/redpower/machine/TileDeploy", "d", "()V")
        ).jumpAfter();
        ctx.add(INVOKESTATIC(hooks(), "unlock", "()V"));

        ctx.jumpToLastReturn();
        ctx.add(INVOKESTATIC(hooks(), "unlock", "()V"));
    }

    public static class Hooks {
        static final ReentrantLock LOCK = new ReentrantLock();

        public static void lock() {
            LOCK.lock();
        }

        public static void unlock() {
            LOCK.unlock();
        }
    }
}
