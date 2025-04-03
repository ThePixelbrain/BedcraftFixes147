package uk.bedcraft.bedcraftfixes.bukkitevents.computercraft;

import dan200.turtle.shared.TileEntityTurtle;
import net.minecraft.util.Facing;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("dan200.turtle.shared.TileEntityTurtle")
@ConfigOptions("bukkitEventComputerCraft")
public class TileEntityTurtleTransformer extends BedcraftMiniTransformer {
    @Patch.Method("move(I)Z")
    public void patchMove(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                ILOAD(6),
                INVOKESPECIAL("dan200/turtle/shared/TileEntityTurtle", "canPlaceInBlock", "(I)Z")
        ).jumpBefore();
        ctx.add(
                ILOAD(5),
                ILOAD(6),
                ILOAD(7),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createPlaceEvent", "(IIILdan200/turtle/shared/TileEntityTurtle;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    @Patch.Method("useTool(Ldan200/turtle/api/TurtleVerb;I)Z")
    public void patchUseTool(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.add(
                ILOAD(2),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createBreakEventAtDirection", "(ILdan200/turtle/shared/TileEntityTurtle;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    @Patch.Method("place(I[Ljava/lang/Object;)Z")
    public void patchPlace(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.add(
                ILOAD(1),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createPlaceEventAtDirection", "(ILdan200/turtle/shared/TileEntityTurtle;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    @Patch.Method("useStack(Lnet/minecraft/item/ItemStack;IZ[Ljava/lang/Object;F)Z")
    public void patchUseStack(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.add(
                ILOAD(2),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createPlaceEventAtDirection", "(ILdan200/turtle/shared/TileEntityTurtle;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createPlaceEvent(int x, int y, int z, TileEntityTurtle turtle) {
            return BukkitInterop.blockPlaceEvent(
                    x, y, z,
                    String.format("ComputerCraft%d", turtle.getComputerID()),
                    turtle.worldObj.worldInfo.getWorldName()
            );
        }

        public static boolean createBreakEventAtDirection(int dir, TileEntityTurtle turtle) {
            return BukkitInterop.blockBreakEvent(
                    turtle.xCoord + Facing.offsetsXForSide[dir],
                    turtle.yCoord + Facing.offsetsYForSide[dir],
                    turtle.zCoord + Facing.offsetsZForSide[dir],
                    String.format("ComputerCraft%d", turtle.getComputerID()),
                    turtle.worldObj.worldInfo.getWorldName()
            );
        }

        public static boolean createPlaceEventAtDirection(int dir, TileEntityTurtle turtle) {
            return BukkitInterop.blockPlaceEvent(
                    turtle.xCoord + Facing.offsetsXForSide[dir],
                    turtle.yCoord + Facing.offsetsYForSide[dir],
                    turtle.zCoord + Facing.offsetsZForSide[dir],
                    String.format("ComputerCraft%d", turtle.getComputerID()),
                    turtle.worldObj.worldInfo.getWorldName()
            );
        }
    }
}
