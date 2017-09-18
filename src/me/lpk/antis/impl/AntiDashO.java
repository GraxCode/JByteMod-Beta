package me.lpk.antis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.analysis.InsnValue;
import me.lpk.analysis.Sandbox;
import me.lpk.analysis.StackFrame;
import me.lpk.analysis.StackUtil;
import me.lpk.antis.AntiBase;
import me.lpk.util.OpUtils;

public class AntiDashO extends AntiBase {

	public AntiDashO(Map<String, ClassNode> nodes) {
		super(nodes);
	}

	@Override
	public ClassNode scan(ClassNode node) {
		for (MethodNode mnode : node.methods) {
			replace(mnode);
		}
		return node;
	}

	/**
	 * Iterates through Insns in a method. If a certain pattern matching DashO
	 * usage is met, the insns are reformatted to only contain the output
	 * string.
	 * 
	 * @param method
	 */
	private void replace(MethodNode method) {
		StackFrame[] frames = StackUtil.getFrames(method);
		AbstractInsnNode ain = method.instructions.getFirst();
		List<String> strings = new ArrayList<String>();
		List<Integer> argSizes = new ArrayList<Integer>();
		List<Integer> indecies = new ArrayList<Integer>();
		while (ain != null) {
			if (ain.getOpcode() == Opcodes.INVOKESTATIC) {
				String desc = ((MethodInsnNode) ain).desc;
				if (isDashDesc(desc)) {
					int opIndex = OpUtils.getIndex(ain);
					Type t = Type.getMethodType(desc);
					MethodInsnNode min = (MethodInsnNode) ain;
					ClassNode owner = getNodes().get(min.owner);
					Object[] args = new Object[t.getArgumentTypes().length];
					// DashO always has at least 2 args. 
					if (opIndex < 0 || opIndex >= frames.length || args.length <= 1) {
						ain = ain.getNext();
						continue;
					}
					StackFrame frame = frames[opIndex];
					if (frame == null) {
						ain = ain.getNext();
						continue;
					}
					if (frame.getStackSize() < args.length) {
						// This should never happen unless there's some weird
						// jump/flow obfuscation.
						ain = ain.getNext();
						continue;
					}
					boolean failed = false;
					for (int i = 0; i < args.length; i++) {
						InsnValue val = (InsnValue) frame.getStack(frame.getStackSize() - i - 1);
						if (val.getValue() == null) {
							failed = true;
							break;
						}
						args[args.length - i - 1] = val.getValue();
					}
					if (failed) {
						ain = ain.getNext();
						continue;
					}
					Object o = Sandbox.getIsolatedReturn(owner, min, args);
					if (o != null) {
						strings.add(o.toString());
						argSizes.add(args.length);
						indecies.add(opIndex);
					}
				}
			}
			ain = ain.getNext();
		}
		ain = method.instructions.getFirst();
		int offset = 0;
		while (ain != null) {
			if (ain.getOpcode() == Opcodes.INVOKESTATIC) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (isDashDesc(min.desc)) {
					int opIndex = OpUtils.getIndex(ain);
					if (indecies.size() > 0 && indecies.get(0) + offset == opIndex) {
						indecies.remove(0);
						int args = argSizes.remove(0);
						String string = strings.remove(0);
						for (int i = 0; i < args; i++) {
							method.instructions.insertBefore(min, new InsnNode(Opcodes.POP));
							offset++;
						}
						LdcInsnNode ldc = new LdcInsnNode(string);
						method.instructions.set(ain, ldc);
						ain = ldc;
					}
				}
			}
			ain = ain.getNext();
		}
	}

	private boolean isDashDesc(String desc) {
		String s = "Ljava/lang/String;";
		return desc.endsWith(s) && desc.replace("I", "").replace(s, "").length() == 2;
	}
}
