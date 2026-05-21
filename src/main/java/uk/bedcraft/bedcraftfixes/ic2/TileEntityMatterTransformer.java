package uk.bedcraft.bedcraftfixes.ic2;

import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

@Patch.Class("ic2.core.block.machine.tileentity.TileEntityMatter")
@ConfigOptions("ic2MassFabIVInput")
public class TileEntityMatterTransformer extends BedcraftMiniTransformer {
    @Patch.Method("getMaxSafeInput()I")
    public void patchGetMaxSafeInput(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                SIPUSH(8192),
                IRETURN()
        );
    }
}
