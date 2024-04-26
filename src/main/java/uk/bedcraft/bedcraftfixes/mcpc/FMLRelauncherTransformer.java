package uk.bedcraft.bedcraftfixes.mcpc;

import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

@Patch.Class("cpw.mods.fml.relauncher.FMLRelauncher")
@ConfigOptions("mcpcJava8")
public class FMLRelauncherTransformer extends BedcraftMiniTransformer {
    @Patch.Method("setupHome(Ljava/io/File;)V")
    public void patchSetupHome(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.search(
                LDC("java.version"),
                INVOKESTATIC("java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;"),
                LDC("1.8")
        ).jumpAfter();
        ctx.add(
                POP(),
                LDC("ignored")
        );
    }
}
