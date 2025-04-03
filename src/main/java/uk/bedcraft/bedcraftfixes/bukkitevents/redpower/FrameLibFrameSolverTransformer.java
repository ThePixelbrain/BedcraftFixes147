package uk.bedcraft.bedcraftfixes.bukkitevents.redpower;

import com.eloraam.redpower.core.FrameLib;
import com.eloraam.redpower.core.WorldCoord;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;
import uk.bedcraft.bedcraftfixes.bukkitevents.BukkitInterop;

@Patch.Class("com.eloraam.redpower.core.FrameLib$FrameSolver")
@ConfigOptions("bukkitEventRedPower")
public class FrameLibFrameSolverTransformer extends BedcraftMiniTransformer {
    @Patch.Method("step()Z")
    public void patchStep(PatchContext ctx) {
        LabelNode Lcontinue = new LabelNode();
        ctx.jumpToStart();
        ctx.search(
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/core/FrameLib$FrameSolver", "framemap", "Ljava/util/HashSet;"),
                ALOAD(1),
                INVOKEVIRTUAL("java/util/HashSet", "add", "(Ljava/lang/Object;)Z"),
                POP()
        ).jumpBefore();
        ctx.add(
                ALOAD(1),
                ALOAD(0),
                INVOKESTATIC(hooks(), "createEvent", "(Lcom/eloraam/redpower/core/WorldCoord;Lcom/eloraam/redpower/core/FrameLib$FrameSolver;)Z"),
                IFNE(Lcontinue),
                ICONST_0(),
                IRETURN(),
                Lcontinue
        );
    }

    public static class Hooks {
        public static boolean createEvent(WorldCoord wc, FrameLib.FrameSolver frameSolver) {
            boolean success = BukkitInterop.blockBreakEvent(wc.x, wc.y, wc.z, "FRAME", frameSolver.worldObj.worldInfo.getWorldName());
            if (!success) {
                frameSolver.valid = false;
            }

            return success;
        }
    }
}
