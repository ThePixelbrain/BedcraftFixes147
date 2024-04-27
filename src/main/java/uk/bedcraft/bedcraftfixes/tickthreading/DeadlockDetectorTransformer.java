package uk.bedcraft.bedcraftfixes.tickthreading;

import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftFixesPremain;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

@Patch.Class("nallar.tickthreading.minecraft.DeadLockDetector")
@ConfigOptions("ttNoDeadlockRecovery")
public class DeadlockDetectorTransformer extends BedcraftMiniTransformer {
    @Patch.Method("tryFixDeadlocks(Ljava/lang/String;)V")
    public void dontFixDeadlocks(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(RETURN());
    }
}
