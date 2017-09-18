package me.lpk.antis;

import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

public abstract class AntiBase {
	private final Map<String, ClassNode> nodes;

	public AntiBase(Map<String, ClassNode> nodes) {
		this.nodes = nodes;
	}
	
	public abstract ClassNode scan(ClassNode node);

	protected final Map<String, ClassNode> getNodes() {
		return nodes;
	}
}
