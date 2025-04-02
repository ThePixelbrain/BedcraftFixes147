package uk.bedcraft.bedcraftfixes.bukkitevents.ic2;

import ic2.core.ExplosionIC2;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("ic2.core.ExplosionIC2")
@ConfigOptions("bukkitEventIC2Explosion")
public class ExplosionIC2Transformer extends BedcraftMiniTransformer {
    @Patch.Method("doExplosion()V")
    public void patchDoExplosion(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                GETSTATIC("net/minecraft/block/Block", "blocksList", "[Lnet/minecraft/block/Block;"),
                ILOAD(13),
                AALOAD(),
                ASTORE(30)
        ).jumpAfter();
        ctx.add(
                ILOAD(10),
                ILOAD(11),
                ILOAD(12),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(IIILic2/core/ExplosionIC2;)Z"),
                IFNE(Lcontinue),
                RETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(int x, int y, int z, ExplosionIC2 explosionIC2) {
            return BukkitInterop.blockBreakEvent(x, y, z, "EXPLOSIVE", explosionIC2.worldObj.worldInfo.getWorldName());
        }
    }
}
