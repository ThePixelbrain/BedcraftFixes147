package uk.bedcraft.bedcraftfixes.bukkitevents.ic2;

import ic2.core.block.EntityDynamite;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("ic2.core.block.EntityDynamite")
@ConfigOptions("bukkitEventIC2Explosion")
public class EntityDynamiteTransformer extends BedcraftMiniTransformer {
    @Patch.Method("explode()V")
    public void patchExplode(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(Lic2/core/block/EntityDynamite;)Z"),
                IFNE(Lcontinue),
                RETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(EntityDynamite entityDynamite) {
            // I have no idea why this fake player is called [DYNAME] but I'll just inherit it from Uxels code
            return BukkitInterop.blockBreakEvent((int) entityDynamite.posX, (int) entityDynamite.posY, (int) entityDynamite.posZ, "DYNAME", entityDynamite.worldObj.worldInfo.getWorldName());
        }
    }
}
