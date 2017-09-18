package me.lpk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;

public class ParentUtils {
	/**
	 * Returns true if the method has called it's super method.
	 * 
	 * @param mn
	 * @return
	 */
	public static boolean callsSuper(MethodNode mn) {
		for (AbstractInsnNode ain : mn.instructions.toArray()) {
			if (ain.getOpcode() == Opcodes.INVOKESPECIAL) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (min.name.equals(mn.name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the field in the given class with the name and description. If
	 * it's not in the given class, parents are checked. Returns null if nothing
	 * is found.
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	public static MappedMember findMethod(MappedClass owner, String name, String desc, boolean originalNames) {
		// Check the class itself
		for (MappedMember mm : owner.getMethods()) {
			if (matches(mm, name, desc, originalNames)) {
				return mm;
			}
		}
		return null;
	}

	/**
	 * Returns the method in the given class's parent with the name and
	 * description. If it's not in the given class, further parents are checked.
	 * Returns null if nothing is found.
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	public static List<MappedMember> findMethodParent(MappedClass owner, String name, String desc, boolean originalNames) {
		List<MappedMember> list = new ArrayList<MappedMember>();
		// Check for interfaces in the method's class.
		for (MappedClass interfaceClass : owner.getInterfaces()) {
			MappedMember mm = findMethodInParentInclusive(interfaceClass, name, desc,originalNames);
			if (mm != null) {
				list.add(mm);
			}
		}
		// Check the parents
		if (owner.getParent() != null) {
			MappedMember mm = findMethodInParentInclusive(owner.getParent(), name, desc,originalNames);
			if (mm != null) {
				list.add(mm);
			}
		}
		return list;
	}

	public static MappedMember findMethodInParentInclusive(MappedClass owner, String name, String desc, boolean originalNames) {
		for (MappedMember mm : owner.getMethods()) {
			if (matches(mm, name, desc, originalNames)) {
				return mm;
			}
		}
		// Check for interfaces in the method's class.
		for (MappedClass interfaceClass : owner.getInterfaces()) {
			MappedMember mm = findMethodInParentInclusive(interfaceClass, name, desc, originalNames);
			if (mm != null) {
				return mm;
			}
		}
		// Check the parents
		if (owner.getParent() != null) {
			MappedMember mm = findMethodInParentInclusive(owner.getParent(), name, desc, originalNames);
			if (mm != null) {
				return mm;
			}
		}
		return null;
	}

	/**
	 * Finds the parent-most overridden member.
	 * 
	 * @param mm
	 * @return
	 */
	public static MappedMember findMethodOverride(MappedMember mm) {
		if (mm.doesOverride()) {
			// Overridden method's parent == given method's parent.
			for (MappedMember mm2 : mm.getOverrides()) {
				return findMethodOverride(mm2);
			}
		}
		return mm;
	}

	/**
	 * Returns the field in the given class's parent with the name and
	 * description. If it's not in the given class, further parents are checked.
	 * Returns null if nothing is found.
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	public static MappedMember findFieldInParent(MappedClass owner, String name, String desc, boolean originalNames) {

		// Check for interfaces in the field's class.
		for (MappedClass interfaceClass : owner.getInterfaces()) {
			MappedMember mm = findFieldInParentInclusive(interfaceClass, name, desc, originalNames);
			if (mm != null) {
				return mm;
			}
		}
		// Check the parents
		if (owner.getParent() != null) {
			MappedMember mm = findFieldInParentInclusive(owner.getParent(), name, desc, originalNames);
			if (mm != null) {
				return mm;
			}
		}
		return null;
	}

	public static MappedMember findFieldInParentInclusive(MappedClass owner, String name, String desc, boolean originalNames) {
		// Check the class itself
		for (MappedMember mm : owner.getFields()) {
			if (matches(mm, name, desc, originalNames)) {
				return mm;
			}
		}
		// Check for interfaces in the field's class.
		for (MappedClass interfaceClass : owner.getInterfaces()) {
			MappedMember mm = findFieldInParentInclusive(interfaceClass, name, desc, originalNames);
			if (mm != null) {
				return mm;
			}
		}
		// Check the parents
		if (owner.getParent() != null) {
			MappedMember mm = findFieldInParentInclusive(owner.getParent(), name, desc, originalNames);
			if (mm != null) {
				return mm;
			}
		}
		return null;
	}

	/**
	 * Returns the method in the given class with the name and description. If
	 * it's not in the given class, parents are checked. Returns null if nothing
	 * is found.
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	public static MappedMember findField(MappedClass owner, String name, String desc) {
		// Check the class itself
		for (MappedMember mm : owner.getFields()) {
			if (matches(mm, name, desc, true)) {
				return mm;
			}
		}
		return null;
	}

	/**
	 * For some reason MappedMember.findNameAndDescWhatever(name,desc) doesn't
	 * exactly work. This is an external implemtation which does the same thing
	 * but somehow works.
	 * 
	 * @param mm
	 * @param name
	 * @param desc
	 * @return
	 */
	public static boolean matches(MappedMember mm, String name, String desc, boolean old) {
		if (name.equals(old ? mm.getOriginalName() : mm.getNewName()) && mm.getDesc().equals(desc)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if two mapped members are the same.
	 * 
	 * @param mm
	 * @param mm2
	 * @param orig
	 *            Whether to check original names or the updated names.
	 * @return
	 */
	public static boolean matches(MappedMember mm, MappedMember mm2, boolean orig) {
		return matches(mm, orig ? mm2.getOriginalName() : mm2.getNewName(), mm2.getDesc(), true);
	}

	public static boolean isLoop(ClassNode node, Map<String, ClassNode> nodes, int i) {
		ClassNode parentNode = nodes.get(node.superName);
		if (parentNode == null) {
			return false;
		}
		if (node.name.equals(parentNode.superName)) {
			return true;
		}
		return false;
	}

	public static MappedClass findCommonParent(MappedClass mc1, MappedClass mc2) {
		// Are they the same?
		if (mc1.getNewName().equals(mc2.getNewName())) {
			return mc1;
		}
		// Does m1 extend or implement anything m2 does?
		for (String parentNames1 : getParents(mc1)) {
			for (String parentNames2 : getParents(mc2)) {
				if (parentNames1.equals(parentNames2)) {
					return getParent(parentNames1, mc1);
				}
			}
		}
		return null;
	}

	private static MappedClass getParent(String n, MappedClass m) {
		while (m != null) {
			if (m.getOriginalName().equals(n)) {
				return m;
			}
			for (MappedClass i : m.getInterfaces()) {
				MappedClass p = getParent(n, i);
				if (p != null) {
					return p;
				}
			}
			m = m.getParent();
		}
		return null;
	}

	private static List<String> getParents(MappedClass m) {
		List<String> list = new ArrayList<String>();
		while (m != null) {
			list.add(m.getOriginalName());
			for (MappedClass i : m.getInterfaces()) {
				list.addAll(getParents(i));
			}
			m = m.getParent();
		}
		return list;
	}

	public static boolean isChild(MappedClass parent, MappedClass mc) {
		return findCommonParent(mc, parent) == parent;
	}
}
