package uk.bedcraft.bedcraftfixes.appeng;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import nilloader.api.lib.asm.Opcodes;
import nilloader.api.lib.asm.tree.ClassNode;
import nilloader.api.lib.asm.tree.FieldNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftFixesPremain;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

@Patch.Class("appeng.me.tile.TileLevelEmitter")
@ConfigOptions("fixTTAppEngLevelEmitter")
public class TileLevelEmitterTransformer extends BedcraftMiniTransformer {

    @Override
    protected boolean modifyClassStructure(ClassNode clazz) {
        for (FieldNode field : clazz.fields) {
            if (field.name.equals("currentState")) field.access |= Opcodes.ACC_VOLATILE;
        }
        return false;
    }

    @Patch.Method("tickNetwork()V")
    public void patchTickNetwork(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                ICONST_0(),
                PUTFIELD("appeng/me/tile/TileLevelEmitter", "currentState", "Z")
        ).erase();
    }

    @Patch.Method("notifyNeightbors()V")
    @Patch.Method.AffectsControlFlow
    public void patchNotifyNeightbors(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                INVOKESTATIC(hooks(), "notifyNeightbors", "(Lnet/minecraft/tileentity/TileEntity;)V"),
                RETURN()
        );
    }

    public static class Hooks {
        // World drops neighbor updates while editingBlocks is set, which under TT can be another thread's redstone update.
        public static void notifyNeightbors(TileEntity tile) {
            World world = tile.worldObj;
            int x = tile.xCoord;
            int y = tile.yCoord;
            int z = tile.zCoord;
            int blockId = world.getBlockId(x, y, z);

            notifyBlocksOfNeighborChange(world, x, y, z, blockId);
            notifyBlocksOfNeighborChange(world, x - 1, y, z, blockId);
            notifyBlocksOfNeighborChange(world, x, y - 1, z, blockId);
            notifyBlocksOfNeighborChange(world, x, y, z - 1, blockId);
            notifyBlocksOfNeighborChange(world, x, y, z + 1, blockId);
            notifyBlocksOfNeighborChange(world, x, y + 1, z, blockId);
            notifyBlocksOfNeighborChange(world, x + 1, y, z, blockId);
        }

        private static void notifyBlocksOfNeighborChange(World world, int x, int y, int z, int blockId) {
            notifyBlock(world, x - 1, y, z, blockId);
            notifyBlock(world, x + 1, y, z, blockId);
            notifyBlock(world, x, y - 1, z, blockId);
            notifyBlock(world, x, y + 1, z, blockId);
            notifyBlock(world, x, y, z - 1, blockId);
            notifyBlock(world, x, y, z + 1, blockId);
        }

        private static void notifyBlock(World world, int x, int y, int z, int blockId) {
            int id = world.getBlockIdWithoutLoad(x, y, z);
            Block block = id < 1 ? null : Block.blocksList[id];
            if (block == null) return;

            try {
                block.onNeighborBlockChange(world, x, y, z, blockId);
            } catch (Throwable t) {
                BedcraftFixesPremain.log.warn("Exception while updating neighbor of ME Level Emitter", t);
            }
        }
    }
}
