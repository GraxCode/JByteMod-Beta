package me.lpk.util;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class ReferenceUtils {

	/**
	 * Finds references to the target class in the second class.
	 * 
	 * @param target
	 * @param inThisNode
	 * @return
	 */
	public static List<Reference> getReferences(ClassNode target, ClassNode inThisNode) {
		List<Reference> references = new ArrayList<Reference>();
		for (MethodNode method : inThisNode.methods) {
			references.addAll(getReferences(target, inThisNode, method));
		}
		return references;
	}

	/**
	 * Finds references to the target class's field in the second class.
	 * 
	 * @param target
	 * @param targetField
	 * @param inThisNode
	 * @return
	 */
	public static List<Reference> getReferences(ClassNode target, FieldNode targetField, ClassNode inThisNode) {
		List<Reference> references = new ArrayList<Reference>();
		for (MethodNode method : inThisNode.methods) {
			references.addAll(getReferences(target, targetField, inThisNode, method));
		}
		return references;
	}

	/**
	 * Finds references to the target class's method in the second class.
	 * 
	 * @param target
	 * @param targetMethod
	 * @param inThisNode
	 * @return
	 */
	public static List<Reference> getReferences(ClassNode target, MethodNode targetMethod, ClassNode inThisNode) {
		List<Reference> references = new ArrayList<Reference>();
		for (MethodNode method : inThisNode.methods) {
			references.addAll(getReferences(target, targetMethod, inThisNode, method));
		}
		return references;
	}

	/**
	 * Finds references to the class in the given method.
	 * 
	 * @param target
	 * @param method
	 * @return
	 */
	public static List<Reference> getReferences(ClassNode target, ClassNode inThisNode, MethodNode method) {
		String targetDesc = target.name;
		List<Reference> references = new ArrayList<Reference>();
		for (AbstractInsnNode ain : method.instructions.toArray()) {
			switch (ain.getType()) {
			case AbstractInsnNode.METHOD_INSN:
				MethodInsnNode min = (MethodInsnNode) ain;
				if (min.desc.contains(targetDesc) || min.owner.contains(targetDesc)) {
					references.add(new Reference(inThisNode, method, ain));
				}
				break;
			case AbstractInsnNode.FIELD_INSN:
				FieldInsnNode fin = (FieldInsnNode) ain;
				if (fin.desc.contains(targetDesc) || fin.owner.contains(targetDesc)) {
					references.add(new Reference(inThisNode, method, ain));
				}
				break;
			case AbstractInsnNode.TYPE_INSN:
				TypeInsnNode tin = (TypeInsnNode) ain;
				if (tin.desc.contains(targetDesc)) {
					references.add(new Reference(inThisNode, method, ain));
				}
				break;
			}
		}
		return references;
	}

	/**
	 * Finds references to the class's field in the given method.
	 * 
	 * @param target
	 * @param targetField
	 * @param method
	 * @return
	 */
	public static List<Reference> getReferences(ClassNode target, FieldNode targetField, ClassNode inThisNode, MethodNode method) {
		List<Reference> references = new ArrayList<Reference>();
		for (AbstractInsnNode ain : method.instructions.toArray()) {
			if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
				FieldInsnNode fin = (FieldInsnNode) ain;
				if (fin.owner.contains(target.name) && fin.name.equals(targetField.name) && fin.desc.equals(targetField.desc)) {
					references.add(new Reference(inThisNode, method, ain));
				}
			}
		}
		return references;
	}

	/**
	 * Finds references to the class's method in the given method.
	 * 
	 * @param target
	 * @param targetMethod
	 * @param method
	 * @return
	 */
	public static List<Reference> getReferences(ClassNode target, MethodNode targetMethod, ClassNode inThisNode, MethodNode method) {
		List<Reference> references = new ArrayList<Reference>();
		for (AbstractInsnNode ain : method.instructions.toArray()) {
			if (ain.getType() == AbstractInsnNode.METHOD_INSN) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (min.owner.contains(target.name) && min.name.equals(targetMethod.name) && min.desc.equals(targetMethod.desc)) {
					references.add(new Reference(inThisNode, method, ain));
				}
			}
		}
		return references;
	}
}
