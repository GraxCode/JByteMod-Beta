package me.lpk.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.MethodNode;

import me.lpk.mapping.remap.MappingMode;
import me.lpk.util.ParentUtils;

public class MappingRenamer {
	private static final Set<String> whitelist = new HashSet<String>();
	private final List<String> remapped = new ArrayList<String>();

	/**
	 * Gives each MappedClass in a map new names based on rules defined in a
	 * given MappingMode.
	 * 
	 * @param mappings
	 * @param mode
	 * @return
	 */
	public Map<String, MappedClass> remapClasses(Map<String, MappedClass> mappings, MappingMode mode) {
		for (MappedClass mc : mappings.values()) {
			if (!mc.isLibrary()) {
				remapClass(mc, mappings, mode);
			}
		}
		return mappings;
	}

	/**
	 * Gives a MappedClass based on rules defined in a given MappingMode
	 * 
	 * @param mc
	 * @param mappings
	 * @param mode
	 * @return
	 */
	public Map<String, MappedClass> remapClass(MappedClass mc, Map<String, MappedClass> mappings, MappingMode mode) {
		// If already renamed or is a library, MappedClass should not be
		// renamed. Skip.
		if (mc.isLibrary() || remapped.contains(mc.getOriginalName())) {
			return mappings;
		}
		// Remap parents before remapping the target class.
		if (mc.hasParent()) {
			mappings = remapClass(mc.getParent(), mappings, mode);
		}
		// Remap interfaces before remapping the target class.
		for (MappedClass interfaze : mc.getInterfaces()) {
			mappings = remapClass(interfaze, mappings, mode);
		}
		// Remap outer classes before remapping the target class.
		if (mc.isInnerClass()) {
			mappings = remapClass(mc.getOuterClass(), mappings, mode);
		}
		if (!mc.isInnerClass()) {
			// Rename the class
			mc.setNewName(mode.getClassName(mc));
		} else {
			// Handling naming of inner class names
			// Syntax is: OuterClassName + $ + NewClassName
			MappedClass outter = mc.getOuterClass();
			String newName = mode.getClassName(mc);
			String post = newName.contains("/") ? newName.substring(newName.lastIndexOf("/") + 1, newName.length()) : newName;
			mc.setNewName(outter.getNewName() + "$" + post);
		}
		// Rename fields
		for (MappedMember mm : mc.getFields()) {
			mm.setNewName(mode.getFieldName(mm));
		}
		// Rename methods
		for (MappedMember mm : mc.getMethods()) {
			// Skip methods that should not be renamed.
			// Library is checked since in the generation phase, members
			// overriding library methods are in turn marked as library methods
			// themselves.
			if (keepName(mm) || mm.isLibrary()) {
				continue;
			}
			// Check and see if there is an overriding method to pull a name
			// from.
			MappedMember override = ParentUtils.findMethodOverride(mm);
			if (override.equals(mm)) {
				// No parent found. Give method a name
				mm.setNewName(mode.getMethodName(mm));
			} else {
				// Override found. Give method parent's name.
				mm.setNewName(override.getNewName());
				// Make sure if override structure is convoluted it's all named
				// correctly regardless.
				// This is only needed when a class extends and implements
				// multiple classes with shared method names.
				if (mm.doesOverride() && !mm.isOverriden()) {
					fixOverrideNames(mm, override);
				}
			}
			MethodNode mn = mm.getMethodNode();
			updateStrings(mn, mappings);
		}
		remapped.add(mc.getOriginalName());
		return mappings;
	}

	/**
	 * Ensures all methods in the override structure have the same name. This is
	 * only needed for cases like: http://pastebin.com/CpeD6wgN <br>
	 * TODO: Determine if this step is even needed for each input and ignore it
	 * if it's not needed.
	 * 
	 * @param mm
	 * @param override
	 */
	private static void fixOverrideNames(MappedMember mm, MappedMember override) {
		for (MappedMember mm2 : mm.getOverrides()) {
			fixOverrideNames(mm2, override);
		}
		mm.setNewName(override.getNewName());
	}

	/**
	 * Updates strings when they are used in situations such as Class.forName /
	 * Reflection.
	 * 
	 * @param mn
	 * @param mappings
	 */
	private static void updateStrings(MethodNode mn, Map<String, MappedClass> mappings) {
		// TODO: Check for Class.forName(String)
	}

	/**
	 * Checks if a given MappedMember should not be renamed.
	 * 
	 * @param mm
	 * @return
	 */
	public static boolean keepName(MappedMember mm) {
		// Main class
		if (mm.getDesc().equals("([Ljava/lang/String;)V") && mm.getOriginalName().equals("main")) {
			return true;
		}
		// <init> or <clinit>
		if (mm.getOriginalName().contains("<")) {
			return true;
		}
		// A method name that shan't be renamed!
		if (isNameWhitelisted(mm.getOriginalName())) {
			return true;
		}
		return false;
	}

	public static boolean isNameWhitelisted(String name) {
		return whitelist.contains(name);
	}

	static {

		// Should let user add additional names to the list
		// I guess classes like Enum don't have this as parent methods per say,
		// so this will be necessary.
		Collections.addAll(whitelist, "contains", "toString", "equals", "clone", "run", "start");
	}
}
