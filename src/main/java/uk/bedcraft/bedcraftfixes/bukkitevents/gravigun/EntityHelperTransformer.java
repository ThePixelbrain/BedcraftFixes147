package uk.bedcraft.bedcraftfixes.bukkitevents.gravigun;

import net.minecraft.entity.Entity;
import net.minecraft.util.RayTraceResult;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("gravigun.common.core.EntityHelper")
@ConfigOptions("bukkitEventBuildGraviGun")
public class EntityHelperTransformer extends BedcraftMiniTransformer {
    @Patch.Method("canGrab(Lnet/minecraft/entity/Entity;)Z")
    public void patchCanGrab(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(Lnet/minecraft/entity/Entity;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    @Patch.Method("tryGrab(Lnet/minecraft/entity/EntityLiving;Z)V")
    public void patchTryGrab(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                GETFIELD("net/minecraft/entity/EntityLiving", "worldObj", "Lnet/minecraft/world/World;"),
                ALOAD(2),
                GETFIELD("net/minecraft/util/RayTraceResult", "blockX", "I"),
                ALOAD(2),
                GETFIELD("net/minecraft/util/RayTraceResult", "blockY", "I"),
                ALOAD(2),
                GETFIELD("net/minecraft/util/RayTraceResult", "blockZ", "I"),
                INVOKEVIRTUAL("net/minecraft/world/World", "getBlockId", "(III)I"),
                ISTORE(4)
        ).jumpBefore();
        ctx.add(
                ALOAD(0),
                ALOAD(2),
                INVOKESTATIC(hooks(), "createEvent", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/RayTraceResult;)Z"),
                IFNE(Lcontinue),
                RETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(Entity entity) {
            return BukkitInterop.blockBreakEvent((int) entity.posX, (int) entity.posY, (int) entity.posZ, "GravityGun", entity.worldObj.worldInfo.getWorldName());
        }

        public static boolean createEvent(Entity entity, RayTraceResult mop) {
            return BukkitInterop.blockBreakEvent(mop.blockX, mop.blockY, mop.blockZ, "GravityGun", entity.worldObj.worldInfo.getWorldName());
        }
    }
}
