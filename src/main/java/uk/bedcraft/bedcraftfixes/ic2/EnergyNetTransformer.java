package uk.bedcraft.bedcraftfixes.ic2;

import nilloader.api.lib.asm.tree.ClassNode;
import nilloader.api.lib.asm.tree.FieldNode;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

import static nilloader.api.lib.asm.Opcodes.*;

@Patch.Class("ic2.core.EnergyNet")
@ConfigOptions("ic2UxelEnergyNet")
public class EnergyNetTransformer extends BedcraftMiniTransformer {

    private static final String ENET       = "ic2/core/EnergyNet";
    private static final String UXEL       = "uk/bedcraft/bedcraftfixes/ic2/UXELEnergyNet";
    private static final String UXEL_DESC  = "Luk/bedcraft/bedcraftfixes/ic2/UXELEnergyNet;";
    private static final String TE_DESC    = "Lnet/minecraft/tileentity/TileEntity;";
    private static final String WORLD_DESC = "Lnet/minecraft/world/World;";
    private static final String SRC_DESC   = "Lic2/api/energy/tile/IEnergySource;";

    @Override
    protected boolean modifyClassStructure(ClassNode clazz) {
        clazz.fields.add(new FieldNode(ASM9, ACC_PUBLIC, "uxelEnergyNet", UXEL_DESC, null, null));
        return false;
    }

    @Patch.Method("<init>()V")
    public void patchInit(PatchContext ctx) {
        ctx.jumpToLastReturn();
        ctx.add(
                ALOAD(0),
                NEW(UXEL),
                DUP(),
                INVOKESPECIAL(UXEL, "<init>", "()V"),
                PUTFIELD(ENET, "uxelEnergyNet", UXEL_DESC)
        );
    }

    // static method — resolve EnergyNet via getForWorld, then delegate to uxelEnergyNet
    @Patch.Method("onTick(" + WORLD_DESC + ")V")
    public void patchOnTick(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                INVOKESTATIC(ENET, "getForWorld", "(" + WORLD_DESC + ")L" + ENET + ";"),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                INVOKEVIRTUAL(UXEL, "tickStart", "()V"),
                RETURN()
        );
    }

    @Patch.Method("addTileEntity(" + TE_DESC + ")V")
    public void patchAddTileEntity(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                INVOKEVIRTUAL(UXEL, "addTileEntity", "(" + TE_DESC + ")V"),
                RETURN()
        );
    }

    @Patch.Method("removeTileEntity(" + TE_DESC + ")V")
    public void patchRemoveTileEntity(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                INVOKEVIRTUAL(UXEL, "removeTileEntity", "(" + TE_DESC + ")V"),
                RETURN()
        );
    }

    @Patch.Method("emitEnergyFrom(" + SRC_DESC + "I)I")
    public void patchEmitEnergyFrom(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                ILOAD(2),
                INVOKEVIRTUAL(UXEL, "emitEnergyFrom", "(" + SRC_DESC + "I)I"),
                IRETURN()
        );
    }

    @Patch.Method("getTotalEnergyConducted(" + TE_DESC + ")J")
    public void patchGetTotalEnergyConducted(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                INVOKEVIRTUAL(UXEL, "getTotalEnergyConducted", "(" + TE_DESC + ")J"),
                LRETURN()
        );
    }

    @Patch.Method("getTotalEnergyEmitted(" + TE_DESC + ")J")
    public void patchGetTotalEnergyEmitted(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                INVOKEVIRTUAL(UXEL, "getTotalEnergyEmitted", "(" + TE_DESC + ")J"),
                LRETURN()
        );
    }

    @Patch.Method("getTotalEnergySunken(" + TE_DESC + ")J")
    public void patchGetTotalEnergySunken(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                INVOKEVIRTUAL(UXEL, "getTotalEnergySunken", "(" + TE_DESC + ")J"),
                LRETURN()
        );
    }

    @Patch.Method("discoverTargets(" + TE_DESC + "ZI)Ljava/util/List;")
    public void patchDiscoverTargets(PatchContext ctx) {
        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD(ENET, "uxelEnergyNet", UXEL_DESC),
                ALOAD(1),
                ILOAD(2),
                ILOAD(3),
                INVOKEVIRTUAL(UXEL, "discoverTargets", "(" + TE_DESC + "ZI)Ljava/util/List;"),
                ARETURN()
        );
    }
}