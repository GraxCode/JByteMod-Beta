package me.lpk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;
import me.lpk.mapping.MappingRenamer;
import me.lpk.mapping.remap.MappingMode;
import me.lpk.util.ParentUtils;
import me.lpk.util.RegexUtils;

public class CorrelationMapper {
	/**
	 * Maps the correlations between target and clean classes.
	 * 
	 * @param targetClass
	 * @param cleanClass
	 * @param targetMap
	 * @param cleanMap
	 * @return
	 */
	public static Map<String, MappedClass> correlate(MappedClass targetClass, MappedClass cleanClass, Map<String, MappedClass> targetMap,
			Map<String, MappedClass> cleanMap) {
		if (targetClass.isRenamed()) {
			return targetMap;
		}
		// Verify classes are similiar
		if (!areSimiliar(targetClass, cleanClass)) {
			return targetMap;
		}
		// Hop to all parents.
		if (targetClass.hasParent() && cleanClass.hasParent()) {
			targetMap = correlate(targetClass.getParent(), cleanClass.getParent(), targetMap, cleanMap);
		}
		// Hop to all interfaces.
		int inters = targetClass.getInterfaces().size();
		if (inters > 0)
			for (int key = 0; key < inters; key++) {
				MappedClass interfaceClassTarget = targetClass.getInterfaces().get(key);
				MappedClass interfaceClassClean = cleanClass.getInterfaces().get(key);
				targetMap = correlate(interfaceClassTarget, interfaceClassClean, targetMap, cleanMap);
			}
		// Begin renaming
		targetClass.setNewName(cleanClass.getOriginalName());
		if (!targetClass.isTruelyRenamed()){
			targetClass.setRenamedOverride(true);
		}
		//targetClass.setRenamedOverride(true);
		// Fields
		List<MappedMember> targetFields = targetClass.getFields();
		List<MappedMember> cleanFields = cleanClass.getFields();
		int offsetField = 0;
		for (int key = 0; key < targetFields.size(); key++) {
			if (key >= cleanFields.size()) {
				continue;
			}
			MappedMember targetField = targetFields.get(key);
			MappedMember cleanField = cleanFields.get(key + offsetField);
			// Extra field?
			if (cleanField == null) {
				continue;
			}
			// Signatures do not match.
			if (!fix(targetField.getDesc()).equals(fix(cleanField.getDesc()))) {
				// Field insertion? Make next pass for the clean field (key - x)
				// x++
				offsetField -= 1;
				continue;
			} else {
				if (targetField.getDesc().length() > 4 && cleanField.getDesc().length() > 4) {
					String n1 = RegexUtils.matchDescriptionClasses(targetField.getDesc()).get(0);
					String n2 = RegexUtils.matchDescriptionClasses(cleanField.getDesc()).get(0);
					MappedClass c1 = targetMap.get(n1);
					MappedClass c2 = targetMap.get(n2);
					if (c1 != null && c2 != null) {
						boolean flag = areSimiliar(c1, c2);
						if (!flag) {
							offsetField -= 1;
							continue;
						}
					}
				}
			}
			targetField.setNewName(cleanField.getOriginalName());
			// Attempt to hop to target field's type
			List<MappedClass> targetFieldTypes = getTypesFromMember(targetField, targetMap);
			if (targetFieldTypes == null) {
				// Field types not found.
				continue;
			}
			List<MappedClass> cleanFieldTypes = getTypesFromMember(cleanField, cleanMap);
			if (cleanFieldTypes == null) {
				// Field types not found.
				continue;
			}
			// Map the field's type class
			for (int i = 0; i < targetFieldTypes.size(); i++) {
				MappedClass targetType = targetFieldTypes.get(i);
				if (i >= cleanFieldTypes.size()) {
					break;
				}
				MappedClass cleanType = cleanFieldTypes.get(i);
				if (targetType == null || cleanType == null) {
					continue;
				}
				targetMap = correlate(targetType, cleanType, targetMap, cleanMap);
			}
		}
		// Methods
		List<MappedMember> targetMethods = targetClass.getMethods();
		List<MappedMember> cleanMethods = cleanClass.getMethods();
		int offsetMethd = 0;
		for (int key = 0; key < targetMethods.size(); key++) {
			if (key >= cleanMethods.size()) {
				continue;
			}
			MappedMember targetMethod = targetMethods.get(key);
			MappedMember cleanMethod = cleanMethods.get(key + offsetMethd);
			// Extra method?
			if (cleanMethod == null) {
				continue;
			}
			// Signatures do not match.
			if (!fix(targetMethod.getDesc()).equals(fix(cleanMethod.getDesc()))) {
				// Method insertion? Next pass for the clean method (key - x)
				offsetMethd -= 1;

				continue;
			}
			targetMethod.setNewName(cleanMethod.getOriginalName());
			// Attempt to hop to target method's type
			List<MappedClass> targetMethodTypes = getTypesFromMember(targetMethod, targetMap);
			if (targetMethodTypes == null) {
				// Method types not found.
				continue;
			}
			List<MappedClass> cleanMethodTypes = getTypesFromMember(cleanMethod, cleanMap);
			if (cleanMethodTypes == null) {
				// Method types not found.
				continue;
			}
			// Map the method's class types
			int offset = 0;
			for (int i = 0; i < targetMethodTypes.size() - offset; i++) {
				if (i >= targetMethodTypes.size()) {
					break;
				}
				MappedClass targetType = targetMethodTypes.get(i);
				if (targetType.getOriginalName().equals(targetClass.getOriginalName())) {
					continue;
				}
				if (i + offset >= cleanMethodTypes.size()) {
					break;
				}
				MappedClass cleanType = cleanMethodTypes.get(i + offset);
				if (cleanType == null) {
					continue;
				}
				if (!areSimiliar(targetType, cleanType)) {
					// offset -= 1;
					continue;
				}
				//System.out.println(targetClass.getNewName() + ":" + targetType.getNewName());
				targetMap = correlate(targetType, cleanType, targetMap, cleanMap);
			}
		}
		targetMap.put(targetClass.getOriginalName(), targetClass);
		return targetMap;
	}

	/**
	 * Given a map of already renamed classes, fill in the gaps for classes that
	 * were not reached, but have parents that can be pulled from.
	 * 
	 * @param mappedClasses
	 * @param mode
	 * @return
	 */
	public static Map<String, MappedClass> fillInTheGaps(Map<String, MappedClass> mappedClasses, MappingMode mode) {
		for (String originalName : mappedClasses.keySet()) {
			mappedClasses = fillGap(mappedClasses.get(originalName), mappedClasses, mode);
		}
		return mappedClasses;
	}

	/**
	 * Given a class, map of other classes, and a naming convention tries to
	 * link classes together. Classes that aren't renamed that aren't inner
	 * classes are renamed according to the given naming convention.
	 * 
	 * @param mappedClass
	 * @param mappedClasses
	 * @param mode
	 * @return
	 */
	private static Map<String, MappedClass> fillGap(MappedClass mappedClass, Map<String, MappedClass> mappedClasses, MappingMode mode) {
		// If already renamed, pass
		if (mappedClass.isTruelyRenamed()) {
			return mappedClasses;
		}
		// Map interfaces
		for (MappedClass interfaceClass : mappedClass.getInterfaces()) {
			mappedClasses = fillGap(interfaceClass, mappedClasses, mode);
		}
		// Map the parents
		MappedClass parent = mappedClass.getParent();
		if (parent != null && !parent.isRenamed()) {
			mappedClasses = fillGap(parent, mappedClasses, mode);
			// Update parent
			parent = mappedClasses.get(parent.getOriginalName());
		}
		// Map name for inner class
		if (mappedClass.isInnerClass()) {
			MappedClass outerClass = mappedClass.getOuterClass();
			mappedClasses = fillGap(outerClass, mappedClasses, mode);
			if (mappedClass.getOriginalName().contains("$")) {
				String post = mappedClass.getOriginalName().substring(mappedClass.getOriginalName().indexOf("$") + 1);
				mappedClass.setNewName(mappedClass.getOuterClass().getNewName() + "$" + post);
			} else {
				int index = 0;
				for (String name : mappedClass.getOuterClass().getInnerClassMap().keySet()) {
					index += 1;
					if (name.equals(mappedClass.getOriginalName())) {
						break;
					}
				}
				mappedClass.setNewName(mappedClass.getOuterClass().getNewName() + "$" + index);
			}
		} else {
			// Normal class
			String newNameClass = mode.getClassName(mappedClass);
			if (parent != null) {
				// Move next to parent. Organizes packages and is less likely to
				// cass AccessErrors for non-public methods.
				String newNamePackage = parent.getNewName().substring(0, parent.getNewName().lastIndexOf("/") + 1);
				if (newNameClass.contains("/")) {
					newNameClass = newNameClass.substring(newNameClass.lastIndexOf("/") + 1);
				}
				mappedClass.setNewName(newNamePackage + newNameClass);
			} else {
				// Check for interfaces. Put them in that package if there is
				// one or they all are in the same package.
				if (mappedClass.getInterfaces().size() > 0) {
					String s = null;
					boolean failed = false;
					for (MappedClass interfaceClass : mappedClass.getInterfaces()) {
						int index = interfaceClass.getNewName().lastIndexOf("/");
						if (index == -1) {
							continue;
						}
						if (s == null) {
							s = interfaceClass.getNewName().substring(0, index);
						} else {
							String temp = interfaceClass.getNewName().substring(0, interfaceClass.getNewName().lastIndexOf("/"));
							if (s != temp) {
								failed = true;
							}
						}
					}
					if (failed || s == null) {
						mappedClass.setNewName(newNameClass);
					} else {
						if (newNameClass.contains("/")) {
							newNameClass = newNameClass.substring(newNameClass.lastIndexOf("/") + 1);

						}
						mappedClass.setNewName(s + "/" + newNameClass);
					}
				} else if (!mappedClass.isRenamed()) {
					mappedClass.setNewName(newNameClass);
				}
			}
		}
		// Map fields since those don't inherit literally.
		for (MappedMember mm : mappedClass.getFields()) {
			mm.setNewName(mode.getFieldName(mm));
		}
		// Map methods
		for (int key = 0; key < mappedClass.getMethods().size(); key++) {
			MappedMember mm = mappedClass.getMethods().get(key);
			// Probably shouldn't touch this.
			if (MappingRenamer.keepName(mm)) {
				continue;
			}
			// Find the method in a parent class.
			if (mm.doesOverride()) {
				// mappedClasses = fillGap(mm.getOverride().getOwner(),
				// mappedClasses, mode);
				mm.setNewName(ParentUtils.findMethodOverride(mm).getNewName());
			} else {
				mm.setNewName(mode.getMethodName(mm));
			}
			mappedClass.getMethods().set(key, mm);
		}
		mappedClasses.put(mappedClass.getOriginalName(), mappedClass);
		return mappedClasses;
	}

	/**
	 * Checks if two classes are similiar.
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	private static boolean areSimiliar(MappedClass c1, MappedClass c2) {
		if (c1 == null || c2 == null) {
			return false;
		}
		if (c1.hasParent() && !c2.hasParent()) {
			return false;
		}
		if (c1.getNode() == null || c2.getNode() == null) {
			return false;
		}
		if (c1.getInterfaces().size() != c2.getInterfaces().size()) {
			return false;
		}
		// Must have a similar # of fields. Change is porportionate to class
		// size.
		double f1 = c1.getFields().size();
		double f2 = c1.getFields().size();
		double percDiffFields = (Math.abs(f1 - f2) / ((f1 + f2) / 2)) * 100;
		double maxDiffLevelField = Math.min(25, 52 * (Math.pow(f2, -0.5)));
		if (percDiffFields > maxDiffLevelField) {
			return false;
		}

		// Must have a similar # of methods. Change is porportionate to class
		// size.
		double m1 = c1.getMethods().size();
		double m2 = c2.getMethods().size();
		double percDiffMethods = (Math.abs(m1 - m2) / ((m1 + m2) / 2)) * 100;
		double maxDiffLevelMethod = Math.min(25, 52 * (Math.pow(m2, -0.5)));
		if (percDiffMethods > maxDiffLevelMethod) {
			return false;
		}
		return true;
	}

	/**
	 * Get's a member's class types from its description. Best for methods.
	 * 
	 * @param member
	 * @param map
	 * @return
	 */
	private static List<MappedClass> getTypesFromMember(MappedMember member, Map<String, MappedClass> map) {
		List<String> names = RegexUtils.matchDescriptionClasses(member.getDesc());
		if (member.isMethod()) {
			for (AbstractInsnNode ain : member.getMethodNode().instructions.toArray()) {
				if (ain.getType() == AbstractInsnNode.METHOD_INSN) {
					MethodInsnNode min = (MethodInsnNode) ain;
					names.addAll(RegexUtils.matchDescriptionClasses(min.desc));
					names.add(min.owner);
				} else if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
					FieldInsnNode fin = (FieldInsnNode) ain;
					names.addAll(RegexUtils.matchDescriptionClasses(fin.desc));
					names.add(fin.owner);
				} else if (ain.getType() == AbstractInsnNode.TYPE_INSN) {
					TypeInsnNode tin = (TypeInsnNode) ain;
					names.addAll(RegexUtils.matchDescriptionClasses(tin.desc));
				} else if (ain.getType() == AbstractInsnNode.LDC_INSN) {
					LdcInsnNode ldc = (LdcInsnNode) ain;
					if (ldc.cst instanceof Type) {
						Type t = (Type) ldc.cst;
						names.add(t.getClassName().replace(".", "/"));
					}
				}
			}
		}
		if (names.size() == 0) {
			return null;
		}
		List<MappedClass> classes = new ArrayList<MappedClass>();
		for (String name : names) {
			if (!map.containsKey(name)) {
				continue;
			}
			classes.add(map.get(name));
		}
		return classes;
	}

	/**
	 * Quick way of making descriptions easier to compare. Replaces classes with
	 * a single character.
	 * 
	 * @param desc
	 * @return
	 */
	public static String fix(String desc) {
		// List<String> classesViaRegex = Regexr.matchDescriptionClasses(desc);
		while (desc.contains("L")) {
			int lIndex = desc.indexOf("L");
			int cIndex = desc.indexOf(";");
			desc = desc.substring(0, lIndex) + "_" + desc.substring(cIndex + 1);
		}
		return desc;
	}
}
