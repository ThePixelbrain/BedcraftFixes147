package uk.bedcraft.bedcraftfixes.bukkitevents.buildcraft;

import net.minecraft.world.World;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("buildcraft.builders.FillerPattern")
@ConfigOptions("bukkitEventBuildcraft")
public class FillerPatternTransformer extends BedcraftMiniTransformer {
    @Patch.Method("fill(IIIIIILnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;)Z")
    public void patchFill(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(8),
                ILOAD(14),
                ILOAD(13),
                ILOAD(15),
                INVOKESTATIC("buildcraft/core/utils/BlockUtil", "canChangeBlock", "(Lnet/minecraft/world/World;III)Z")
        ).jumpBefore();
        ctx.add(
                ILOAD(14),
                ILOAD(13),
                ILOAD(15),
                ALOAD(8),
                INVOKESTATIC(hooks(), "createPlaceEvent", "(IIILnet/minecraft/world/World;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    @Patch.Method("empty(IIIIIILnet/minecraft/world/World;)Z")
    public void patchEmpty(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(7),
                ILOAD(13),
                ILOAD(12),
                ILOAD(14),
                INVOKESTATIC("buildcraft/core/utils/BlockUtil", "canChangeBlock", "(Lnet/minecraft/world/World;III)Z")
        ).jumpBefore();
        ctx.add(
                ILOAD(13),
                ILOAD(12),
                ILOAD(14),
                ALOAD(7),
                INVOKESTATIC(hooks(), "createBreakEvent", "(IIILnet/minecraft/world/World;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createPlaceEvent(int x, int y, int z, World world) {
            return BukkitInterop.blockPlaceEvent(x, y, z, "Filler", world.worldInfo.getWorldName());
        }

        public static boolean createBreakEvent(int x, int y, int z, World world) {
            return BukkitInterop.blockBreakEvent(x, y, z, "Filler", world.worldInfo.getWorldName());
        }
    }
}
