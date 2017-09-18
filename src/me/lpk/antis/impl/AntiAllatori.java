package me.lpk.antis.impl;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.analysis.Sandbox;
import me.lpk.antis.AntiBase;
import me.lpk.util.AntiSynthetic;
import me.lpk.util.OpUtils;

public class AntiAllatori extends AntiBase {
	private final boolean callProxy;

	public AntiAllatori(Map<String, ClassNode> nodes, boolean callProxy) {
		super(nodes);
		this.callProxy = callProxy;
	}

	@Override
	public ClassNode scan(ClassNode node) {
		for (MethodNode mnode : node.methods) {
			replace(mnode);
		}
		return node;
	}

	private void replace(MethodNode method) {
		if(!method.name.startsWith("access")) {
			try {
				method.access = AntiSynthetic.inverseSynthetic(method.access);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		AbstractInsnNode ain = method.instructions.getFirst();
		L1:
		while (ain != null && ain.getNext() != null) {
			if(ain.getOpcode() == Opcodes.ICONST_0 || ain.getOpcode() == Opcodes.ICONST_1) {
				boolean eq = ain.getOpcode() == Opcodes.ICONST_0;
				if(ain.getNext().getOpcode() == Opcodes.IFEQ || ain.getNext().getOpcode() == Opcodes.IFNE) {
					boolean ifeq = ain.getNext().getOpcode() == Opcodes.IFEQ;
					if(eq != ifeq) {
						//never invoked
						method.instructions.remove(ain.getNext());
						method.instructions.remove(ain);
					} else {
						method.instructions.set(ain.getNext(), new JumpInsnNode(Opcodes.GOTO, ((JumpInsnNode)ain.getNext()).label));
					}
				}
			}
			AbstractInsnNode nod = ain.getNext();
			if(ain.getType() == AbstractInsnNode.LDC_INSN) {
				while(nod == null || nod.getType() == AbstractInsnNode.LABEL || nod.getType() == AbstractInsnNode.FRAME) {
					if(nod == null) {
						ain = ain.getNext();
						continue L1;
					}
					nod = nod.getNext();
				}
				if(nod.getOpcode() != Opcodes.INVOKESTATIC) {
					ain = ain.getNext();
					continue;
				}
			} else {
				ain = ain.getNext();
				continue;
			}
			MethodInsnNode min = (MethodInsnNode) nod;
			if (!min.desc.endsWith("(Ljava/lang/String;)Ljava/lang/String;")) {
				ain = ain.getNext();
				continue;
			}
			ClassNode owner = getNodes().get(min.owner);
			if (owner == null) {
				ain = ain.getNext();
				continue;
			}
			LdcInsnNode ldc = (LdcInsnNode) ain;
			Object o = ldc.cst;
			if (o instanceof String) {
				Object ret = callProxy ? Sandbox.getProxyIsolatedReturn(method, owner, min, new Object[] { o }) : Sandbox.getIsolatedReturn(owner, min, new Object[] { o });
				if (ret != null) {
					int index = OpUtils.getIndex(ain);
					LdcInsnNode newLdc = new LdcInsnNode(ret);
					method.instructions.remove(min);
					method.instructions.set(ldc, newLdc);
					ain = method.instructions.get(index).getNext();
				} else {
					ain = ain.getNext();
				}
			}
		}
	}
}
