package uk.bedcraft.bedcraftfixes.appeng;

import appeng.api.me.tiles.ICellProvider;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.util.IGridInterface;
import appeng.me.container.ContainerTerminal;
import appeng.me.tile.TileController;
import nilloader.api.lib.asm.tree.JumpInsnNode;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

@Patch.Class("appeng.me.tile.TileController")
@ConfigOptions("fixTTAppEngStaleViewers")
public class TileControllerTransformer extends BedcraftMiniTransformer {

    @Patch.Method("g()V")
    @Patch.Method.AffectsControlFlow
    public void patchUpdateEntity(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.search(INSTANCEOF("appeng/me/container/ContainerTerminal")).jumpAfter();
        LabelNode Lremove = ((JumpInsnNode) ctx.get()).label;

        ctx.search(
                CHECKCAST("appeng/me/container/ContainerTerminal"),
                ASTORE(5)
        ).jumpAfter();
        ctx.add(
                ALOAD(5),
                ALOAD(0),
                INVOKESTATIC(hooks(), "isViewing", "(Lappeng/me/container/ContainerTerminal;Lappeng/me/tile/TileController;)Z"),
                IFEQ(Lremove)
        );
    }

    public static class Hooks {
        public static boolean isViewing(ContainerTerminal container, TileController controller) {
            ICellProvider provider = container.GetNetworkIME().realInv;
            if (!(provider instanceof IGridTileEntity)) return true;

            IGridInterface grid = ((IGridTileEntity) provider).getGrid();
            return grid != null && grid.getController() == controller;
        }
    }
}
