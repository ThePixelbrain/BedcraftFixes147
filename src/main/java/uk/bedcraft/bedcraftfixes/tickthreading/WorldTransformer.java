package uk.bedcraft.bedcraftfixes.tickthreading;

import net.minecraft.world.World;
import net.minecraft.world.gen.ServerChunkSource;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

@Patch.Class("net.minecraft.world.World")
@ConfigOptions("fixTTChunkProviderAssumptions")
public class WorldTransformer extends BedcraftMiniTransformer {
    @Patch.Method("checkChunksExist(IIIIII)Z")
    public void patchCheckChunksExist(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                ILOAD(1),
                ILOAD(2),
                ILOAD(3),
                ILOAD(4),
                ILOAD(5),
                ILOAD(6),
                INVOKESTATIC(hooks(), "checkChunksExist", "(Lnet/minecraft/world/World;IIIIII)Z"),
                IRETURN()
        );
    }

    @Patch.Method("getTEWithoutLoad(III)Lnet/minecraft/tileentity/TileEntity;")
    public void patchGetTEWithoutLoad(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();

        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                GETFIELD("net/minecraft/world/World", "chunkProvider", "Lnet/minecraft/world/chunk/ChunkSource;"),
                CHECKCAST("net/minecraft/world/gen/ServerChunkSource")
        ).jumpBefore();
        ctx.add(
                ALOAD(0),
                GETFIELD("net/minecraft/world/World", "chunkProvider", "Lnet/minecraft/world/chunk/ChunkSource;"),
                INSTANCEOF("net/minecraft/world/gen/ServerChunkSource"),
                IFNE(Lcontinue),
                ACONST_NULL(),
                ARETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean checkChunksExist(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            if (minY > 255 || maxY < 0) return false;
            if (!(world.chunkProvider instanceof ServerChunkSource)) return false;

            return ((ServerChunkSource) world.chunkProvider).chunksExist(minX >> 4, minZ >> 4, maxX >> 4, maxZ >> 4);
        }
    }
}
