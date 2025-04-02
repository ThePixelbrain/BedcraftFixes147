package uk.bedcraft.bedcraftfixes.bukkitevents.gravigun;

import net.minecraft.entity.Entity;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("gravigun.common.entity.EntityBlock")
@ConfigOptions("bukkitEventBuildGraviGun")
public class EntityBlockTransformer extends BedcraftMiniTransformer {
    @Patch.Method("j_()V")
    public void patchJ_(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                GETFIELD("gravigun/common/entity/EntityBlock", "p", "Lnet/minecraft/world/World;"),
                GETFIELD("net/minecraft/world/World", "isRemote", "Z")
        ).jumpAfter();
        ctx.search(
                ALOAD(0),
                INVOKEVIRTUAL("gravigun/common/entity/EntityBlock", "x", "()V")
        ).jumpAfter();
        ctx.add(
                ILOAD(1),
                ILOAD(2),
                ILOAD(3),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(IIILnet/minecraft/entity/Entity;)Z"),
                IFNE(Lcontinue),
                RETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(int x, int y, int z, Entity entity) {
            return BukkitInterop.blockPlaceEvent(x, y, z, "GravityGun", entity.worldObj.worldInfo.getWorldName());
        }
    }
}
