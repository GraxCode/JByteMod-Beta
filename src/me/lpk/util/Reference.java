package me.lpk.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Reference {
	private final ClassNode node;
	private final MethodNode method;
	private final AbstractInsnNode ain;

	public Reference(ClassNode node, MethodNode method, AbstractInsnNode ain) {
		this.node = node;
		this.method = method;
		this.ain = ain;
	}

	public ClassNode getNode() {
		return node;
	}

	public MethodNode getMethod() {
		return method;
	}

	public AbstractInsnNode getAin() {
		return ain;
	}
}