package me.lpk.antis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.analysis.Sandbox;
import me.lpk.antis.AntiBase;
import me.lpk.util.OpUtils;

public class AntiStringer extends AntiBase {
	private final boolean callProxy;

	public AntiStringer(Map<String, ClassNode> nodes, boolean callProxy) {
		super(nodes);
		this.callProxy = callProxy;
	}

	@Override
	public ClassNode scan(ClassNode node) {
		// TODO: Fix new stringer
		// final int hashCode =
		// (SharedSecrets.getJavaLangAccess().getConstantPool(Class.forName(stackTrace[2].getClassName())).getSize()
		// + stackTrace[2].getClassName() + stackTrace[2].getMethodName() +
		// SharedSecrets.getJavaLangAccess().getConstantPool(Class.forName(stackTrace[2].getClassName())).getSize()).hashCode();
		//
		// New stringer does this. Find out what that turns into and edit the
		// method THEN invoke it.
		for (MethodNode mnode : node.methods) {
			replace(mnode);
		}
		return node;
	}

	private void replace(MethodNode method) {
		String callerCaller = null;
		AbstractInsnNode ain = method.instructions.getFirst();
		List<String> strings = new ArrayList<String>();
		List<Integer> argSizes = new ArrayList<Integer>();
		List<Integer> indecies = new ArrayList<Integer>();
		while (ain != null) {
			if (ain.getPrevious() != null && ain.getPrevious().getType() == AbstractInsnNode.LDC_INSN && ain.getOpcode() == Opcodes.INVOKESTATIC) {
				String desc = ((MethodInsnNode) ain).desc;
				if (isStringerDesc(desc)) {
					MethodInsnNode min = (MethodInsnNode) ain;
					ClassNode owner = getNodes().get(min.owner);
					String text = ((LdcInsnNode) ain.getPrevious()).cst.toString();
					if (text.length() == 0 || owner == null) {
						ain = ain.getNext();
						continue;
					}
					Object o = null;
					if (callProxy) {
						if (callerCaller == null) {
							callerCaller = findCaller(method);
							if (callerCaller == null) {
								return;
							}
						}
						o = Sandbox.getProxyReturnStringer(callerCaller, method, owner, min, new Object[] { text });
					} else {
						o = Sandbox.getProxyReturn(method, owner, min, new Object[] { text });
					}
					if (o != null) {
						// System.out.println("\t" + text + " : " + o);

						strings.add(o.toString());
						argSizes.add(1);
						indecies.add(OpUtils.getIndex(ain));
					}
				}
			}
			ain = ain.getNext();
		}
		if (strings.size() == 0) {
			return;
		}
		ain = method.instructions.getFirst();
		while (ain != null) {
			if (ain.getPrevious() != null && ain.getPrevious().getType() == AbstractInsnNode.LDC_INSN && ain.getOpcode() == Opcodes.INVOKESTATIC) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (isStringerDesc(min.desc)) {
					int opIndex = OpUtils.getIndex(ain);
					if (indecies.size() > 0 && indecies.get(0) == opIndex) {
						indecies.remove(0);
						String string = strings.remove(0);
						method.instructions.set(ain.getPrevious(), new InsnNode(Opcodes.NOP));

						LdcInsnNode ldc = new LdcInsnNode(string);
						method.instructions.set(ain, ldc);
						ain = ldc;
					}
				}
			}
			ain = ain.getNext();
		}
	}

	private String findCaller(MethodNode method) {
		for (ClassNode cn : getNodes().values()) {
			for (MethodNode mn : cn.methods) {
				for (AbstractInsnNode ain : mn.instructions.toArray()) {
					if (ain.getType() == AbstractInsnNode.METHOD_INSN) {
						MethodInsnNode min = (MethodInsnNode) ain;
						if (min.owner.equals(method.owner) && min.name.equals(method.name) && min.desc.equals(method.desc)) {
							return cn.name;
						}
					}
				}
			}
		}
		return null;
	}

	private boolean isStringerDesc(String desc) {
		return desc.equals("(Ljava/lang/String;)Ljava/lang/String;");
	}
}
