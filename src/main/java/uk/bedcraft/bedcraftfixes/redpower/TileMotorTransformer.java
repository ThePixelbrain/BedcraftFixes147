package uk.bedcraft.bedcraftfixes.redpower;

import com.eloraam.redpower.machine.TileMotor;
import nilloader.api.lib.asm.tree.*;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import uk.bedcraft.bedcraftfixes.BedcraftFixesPremain;
import uk.bedcraft.bedcraftfixes.BedcraftMiniTransformer;
import uk.bedcraft.bedcraftfixes.ConfigOptions;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static nilloader.api.lib.asm.Opcodes.*;

@Patch.Class("com.eloraam.redpower.machine.TileMotor")
@ConfigOptions("redpowerFramesTickthreading")
public class TileMotorTransformer extends BedcraftMiniTransformer {
    @Override
    protected boolean modifyClassStructure(ClassNode clazz) {
        clazz.fields.add(new FieldNode(ASM9, ACC_PRIVATE|ACC_FINAL, "lock", "Ljava/util/concurrent/locks/ReentrantLock;", null, null));
        return true;
    }
        @Patch.Method("<init>()V")
    public void patchInit(PatchContext ctx) {
        ctx.jumpToLastReturn();
        ctx.jumpBackward(1);

        // this.lock = new ReentrantLock();
        ctx.add(
                ALOAD(0),
                NEW("java/util/concurrent/locks/ReentrantLock"),
                DUP(),
                INVOKESPECIAL("java/util/concurrent/locks/ReentrantLock", "<init>", "()V"),
                PUTFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;")
        );
    }

    @Patch.Method("g()V")
    @Patch.Method.AffectsControlFlow
    public void patchG(PatchContext ctx) {
         // if (!this.lock.tryLock())
         //    return;
         // try {
         //     this.g();
         //     this.lock.unlock();
         // } catch (Exception e) {
         //    this.lock.unlock();
         //    throw e;
         // }

        LabelNode tryStart = new LabelNode();
        LabelNode tryEnd = new LabelNode();
        LabelNode tryHandler = new LabelNode();

        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "tryLock", "()Z"),
                IFNE(tryStart),
                RETURN(),
                tryStart
        );

        ctx.jumpToLastReturn();
        ctx.jumpBackward(1);
        ctx.add(
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "unlock", "()V"),
                GOTO(tryEnd),
                tryHandler,
                ASTORE(1),
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "unlock", "()V"),
                ALOAD(1),
                ATHROW(),
                tryEnd
        );

        ctx.addTryBlock(tryStart, tryEnd, tryHandler, "java/lang/Exception");
    }

    @Patch.Method("onBlockNeighborChange(I)V")
    @Patch.Method.AffectsControlFlow
    public void patchOnBlockNeighborChange(PatchContext ctx) {
        // try {
        //     if (!this.lock.tryLock(25L, TimeUnit.MILLISECONDS)) return;
        //     this.onBlockNeighborChange(l);
        //     this.lock.unlock();
        // } catch (Exception e) {
        //    this.lock.unlock();
        //    throw e;
        // }

//        LabelNode Lcontinue = new LabelNode();
//        LabelNode tryStart = new LabelNode();
//        LabelNode tryEnd = new LabelNode();
//        LabelNode tryHandler = new LabelNode();
//
//        ctx.jumpToStart();
//        ctx.add(
//                tryStart,
//                ALOAD(0),
//                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
//                LDC(25),
//                GETSTATIC("java/util/concurrent/TimeUnit", "MILLISECONDS", "Ljava/util/concurrent/TimeUnit;"),
//                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "tryLock", "(JLjava/util/concurrent/TimeUnit;)Z"),
//                IFNE(Lcontinue),
//                RETURN(),
//                Lcontinue
//        );
//
//        ctx.jumpToLastReturn();
//        ctx.jumpBackward(1);
//        ctx.add(
//                ALOAD(0),
//                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
//                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "unlock", "()V"),
//                GOTO(tryEnd),
//                tryHandler,
//                ASTORE(1),
//                ALOAD(0),
//                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
//                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "unlock", "()V"),
//                ALOAD(1),
//                ATHROW(),
//                tryEnd
//        );
//
//        ctx.addTryBlock(tryStart, tryEnd, tryHandler, "java/lang/Exception");

        LabelNode tryStart = new LabelNode();
        LabelNode tryEnd = new LabelNode();
        LabelNode tryHandler = new LabelNode();

        ctx.jumpToStart();
        ctx.add(
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "tryLock", "()Z"),
                IFNE(tryStart),
                RETURN(),
                tryStart
        );

        ctx.jumpToLastReturn();
        ctx.jumpBackward(1);
        ctx.add(
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "unlock", "()V"),
                GOTO(tryEnd),
                tryHandler,
                ASTORE(1),
                ALOAD(0),
                GETFIELD("com/eloraam/redpower/machine/TileMotor", "lock", "Ljava/util/concurrent/locks/ReentrantLock;"),
                INVOKEVIRTUAL("java/util/concurrent/locks/ReentrantLock", "unlock", "()V"),
                ALOAD(1),
                ATHROW(),
                tryEnd
        );

        ctx.addTryBlock(tryStart, tryEnd, tryHandler, "java/lang/Exception");
    }

    @Patch.Method("pickFrame()V")
    public void patchPickFrame(PatchContext ctx) {
        LabelNode end = new LabelNode();
        ctx.jumpToStart();
        ctx.search(RETURN()).jumpBefore();
        ctx.add(GOTO(end));
        ctx.search(RETURN()).jumpBefore();
        ctx.add(GOTO(end));

        ctx.jumpToLastReturn();
        ctx.jumpBackward(1);
        ctx.add(
                end,
                ALOAD(0),
                ICONST_1(),
                INVOKEVIRTUAL("com/eloraam/redpower/machine/TileMotor", "dropFrame", "(Z)V")
        );
    }

    public static class Hooks {
        public volatile int cooldown = 0;
        public volatile int lastLink = 0;
        public final ReentrantLock lock = new ReentrantLock();

        public static void test() {
            BedcraftFixesPremain.log.info("We were here!");
        }

        public void lockG(TileMotor motor) {
            try {
                if (!this.lock.tryLock(25L, TimeUnit.MILLISECONDS)) return;
                test();
            } catch (Exception e) {
                this.lock.unlock();
                throw new RuntimeException();
            }

        }
    }
}
