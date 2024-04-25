package uk.bedcraft.bedcraftfixes;

import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.tree.AbstractInsnNode;
import nilloader.api.lib.asm.tree.InsnList;
import nilloader.api.lib.asm.tree.JumpInsnNode;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;

public abstract class BedcraftMiniTransformer extends MiniTransformer implements ClassRetransformer {

	protected final String hooks() {
		return getClass().getName().replace('.', '/')+"$Hooks";
	}

	/**
	 * Alias for {@link #IFEQ} with an alternate mnemonic that reflects its actual behavior: IF Zero.
	 */
	protected final JumpInsnNode IFZ(LabelNode label) {
		return IFEQ(label);
	}
	
	/**
	 * Alias for {@link #IFNE} with an alternate mnemonic that reflects its actual behavior: IF Not
	 * Zero.
	 */
	protected final JumpInsnNode IFNZ(LabelNode label) {
		return IFNE(label);
	}

	/**
	 * Alias for {@link #IFEQ} with an alternate mnemonic for booleans.
	 */
	protected final JumpInsnNode IF_FALSE(LabelNode label) {
		return IFEQ(label);
	}
	
	/**
	 * Alias for {@link #IFNE} with an alternate mnemonic for booleans.
	 */
	protected final JumpInsnNode IF_TRUE(LabelNode label) {
		return IFNE(label);
	}

	protected final InsnList build(AbstractInsnNode... insns) {
		InsnList li = new InsnList();
		for (AbstractInsnNode ain : insns) {
			li.add(ain);
		}
		return li;
	}
	
}
