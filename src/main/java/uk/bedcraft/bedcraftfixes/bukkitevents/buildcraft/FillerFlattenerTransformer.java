package uk.bedcraft.bedcraftfixes.bukkitevents.buildcraft;

import net.minecraft.tileentity.TileEntity;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("buildcraft.builders.FillerFlattener")
@ConfigOptions("bukkitEventBuildcraft")
public class FillerFlattenerTransformer extends BedcraftMiniTransformer {
    @Patch.Method("iteratePattern(Lnet/minecraft/tileentity/TileEntity;Lbuildcraft/api/core/IBox;Lnet/minecraft/item/ItemStack;)Z")
    public void patchIteratePattern(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(1),
                GETFIELD("net/minecraft/tileentity/TileEntity", "worldObj", "Lnet/minecraft/world/World;"),
                ILOAD(17),
                ILOAD(16),
                ILOAD(18),
                INVOKESTATIC("buildcraft/core/utils/BlockUtil", "canChangeBlock", "(Lnet/minecraft/world/World;III)Z")
        ).jumpBefore();
        ctx.add(
                ILOAD(17),
                ILOAD(16),
                ILOAD(18),
                ALOAD(1),
                INVOKESTATIC(hooks(), "createEvent", "(IIILnet/minecraft/tileentity/TileEntity;)Z"),
                IFNE(Lcontinue),
                ICONST_1(),
                IRETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(int x, int y, int z, TileEntity tile) {
            return BukkitInterop.blockPlaceEvent(x, y, z, "Filler", tile.worldObj.worldInfo.getWorldName());
        }
    }
}
