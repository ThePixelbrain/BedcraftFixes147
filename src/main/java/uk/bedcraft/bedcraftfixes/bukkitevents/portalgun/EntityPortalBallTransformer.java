package uk.bedcraft.bedcraftfixes.bukkitevents.portalgun;

import net.minecraft.entity.Entity;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("portalgun.common.entity.EntityPortalBall")
@ConfigOptions("bukkitEventPortalGun")
public class EntityPortalBallTransformer extends BedcraftMiniTransformer {
    @Patch.Method("spawnBlock(IIIII)Z")
    public void patchSpawnBlock(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.add(
                ILOAD(1),
                ILOAD(2),
                ILOAD(3),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(IIILnet/minecraft/entity/Entity;)Z"),
                IFNE(Lcontinue),
                ALOAD(0),
                INVOKEVIRTUAL("portalgun/common/entity/EntityPortalBall", "x", "()V"),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(int x, int y, int z, Entity entity) {
            return BukkitInterop.blockPlaceEvent(x, y, z, "PortalGun", entity.worldObj.worldInfo.getWorldName());
        }
    }
}
