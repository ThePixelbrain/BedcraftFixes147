package uk.bedcraft.bedcraftfixes.bukkitevents.ic2;

import ic2.core.item.tool.EntityMiningLaser;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("ic2.core.item.tool.EntityMiningLaser")
@ConfigOptions("bukkitEventIC2MiningLaser")
public class EntityMiningLaserTransformer extends BedcraftMiniTransformer {
    @Patch.Method("j_()V")
    public void patchJ_(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                ILOAD(11),
                INVOKEVIRTUAL("ic2/core/item/tool/EntityMiningLaser", "canMine", "(I)Z")
        ).jumpBefore();
        ctx.add(
                ILOAD(8),
                ILOAD(9),
                ILOAD(10),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(IIILic2/core/item/tool/EntityMiningLaser;)Z"),
                IFNE(Lcontinue),
                ALOAD(0),
                INVOKEVIRTUAL("net/minecraft/entity/Entity", "setDead", "()V"),
                RETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(int x, int y, int z, EntityMiningLaser entity) {
            return BukkitInterop.blockBreakEvent(x, y, z, "LASER", entity.worldObj.worldInfo.getWorldName());
        }
    }
}
