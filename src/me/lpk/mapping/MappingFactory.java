package me.lpk.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.mapping.loaders.EnigmaLoader;
import me.lpk.mapping.loaders.MappingLoader;
import me.lpk.mapping.loaders.ProguardLoader;
import me.lpk.mapping.loaders.SRGLoader;
import me.lpk.util.AccessHelper;
import me.lpk.util.JarUtils;
import me.lpk.util.ParentUtils;
import me.lpk.util.RegexUtils;

/**
 * Factory for generating Mappings from various sources such as:
 * <ul>
 * <li>Mappings
 * <ul>
 * <li>Proguard
 * <li>Enigma
 * <li>SRG
 * </ul>
 * <li>Files
 * <ul>
 * <li>Jar
 * </ul>
 * <li>Java Objects
 * <ul>
 * <li>Map&lt;String, ClassNode&gt;
 * </ul>
 * </ul>
 */
public class MappingFactory {
	/**
	 * Returns a map of class names to MappedClasses given an SRG mapping file.
	 * 
	 * @param map
	 * @param nodes
	 * @return
	 */
	public static Map<String, MappedClass> mappingsFromSRG(File file, Map<String, ClassNode> nodes) {
		Map<String, MappedClass> base = mappingsFromNodes(nodes);
		MappingLoader loader = new SRGLoader(nodes);
		try {
			Map<String, MappedClass> newMappings = loader.read(new FileReader(file));
			for (MappedClass mappedClass : newMappings.values()) {
				newMappings = linkMappings(mappedClass, newMappings);
			}
			base = fixFromMappingsText(base, newMappings);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return base;
	}

	/**
	 * Returns a map of class names to MappedClasses given an Engima mapping
	 * file.
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, MappedClass> mappingsFromEnigma(File file, Map<String, ClassNode> nodes) {
		Map<String, MappedClass> base = mappingsFromNodes(nodes);
		MappingLoader loader = new EnigmaLoader(nodes);
		try {
			Map<String, MappedClass> newMappings = loader.read(new FileReader(file));
			for (MappedClass mappedClass : newMappings.values()) {
				newMappings = linkMappings(mappedClass, newMappings);
			}
			base = fixFromMappingsText(base, newMappings);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return base;
	}

	/**
	 * Returns a map of class names to mapped classes given a Proguard mapping
	 * file.
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, MappedClass> mappingsFromProguard(File file, Map<String, ClassNode> nodes) {
		Map<String, MappedClass> base = mappingsFromNodes(nodes);
		MappingLoader loader = new ProguardLoader(nodes);
		try {
			Map<String, MappedClass> newMappings = loader.read(new FileReader(file));
			for (MappedClass mappedClass : newMappings.values()) {
				newMappings = linkMappings(mappedClass, newMappings);
			}
			base = fixFromMappingsText(base, newMappings);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return base;
	}

	/**
	 * Given two maps of mappings, applies the data from the new map to the base
	 * map.
	 * 
	 * @param base
	 * @param newMappings
	 * @return
	 */
	public static Map<String, MappedClass> fixFromMappingsText(Map<String, MappedClass> base, Map<String, MappedClass> newMappings) {
		for (String className : newMappings.keySet()) {
			MappedClass baseClass = base.get(className);
			MappedClass newClass = newMappings.get(className);
			if (baseClass == null) {
				continue;
			}
			baseClass.setNewName(newClass.getNewName());
			for (MappedMember newMember : newClass.getFields()) {
				MappedMember baseMember = ParentUtils.findField(baseClass, newMember.getOriginalName(), newMember.getDesc());
				if (baseMember != null && ParentUtils.matches(baseMember, newMember.getOriginalName(), newMember.getDesc(), true)) {
					baseMember.setNewName(newMember.getNewName());
				}
			}
			for (MappedMember newMember : newClass.getMethods()) {
				MappedMember baseMember = ParentUtils.findMethod(baseClass, newMember.getOriginalName(), newMember.getDesc(), true);
				if (baseMember != null && ParentUtils.matches(baseMember, newMember.getOriginalName(), newMember.getDesc(), true)) {
					baseMember.setNewName(newMember.getNewName());
				}
			}
			base.put(className, baseClass);
		}
		return base;
	}

	/**
	 * Returns a map of class names to mapped classes given a jar file.
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, MappedClass> mappingsFromJar(File file) {
		Map<String, ClassNode> nodes = null;
		try {
			nodes = JarUtils.loadClasses(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mappingsFromNodes(nodes);
	}

	/**
	 * Returns a map of class names to mapped classes given a map of class names
	 * to ClassNodes.
	 * 
	 * @param nodes
	 * @return
	 */
	public static Map<String, MappedClass> mappingsFromNodes(Map<String, ClassNode> nodes) {
		Map<String, MappedClass> mappings = new HashMap<String, MappedClass>();
		for (ClassNode node : nodes.values()) {
			mappings = generateClassMapping(node, nodes, mappings);
		}
		for (String name : mappings.keySet()) {
			mappings = linkMappings(mappings.get(name), mappings);
		}
		return mappings;
	}

	/**
	 * Returns a map of class names to mapped classes given a map of class names
	 * to ClassNodes. Does not link the classes once the mappings are generated.
	 * 
	 * @param nodes
	 * @return
	 */
	public static Map<String, MappedClass> mappingsFromNodesNoLinking(Map<String, ClassNode> nodes) {
		Map<String, MappedClass> mappings = new HashMap<String, MappedClass>();
		for (ClassNode node : nodes.values()) {
			mappings = generateClassMapping(node, nodes, mappings);
		}
		return mappings;
	}

	/**
	 * Generates mapping for the given node and it's parents / interfaces.
	 * 
	 * @param node
	 * @param nodes
	 * @param mappings
	 */
	private static Map<String, MappedClass> generateClassMapping(ClassNode node, Map<String, ClassNode> nodes, Map<String, MappedClass> mappings) {
		// If the node isn't Object, it has parents.
		boolean hasParents = !node.name.equals("java/lang/Object");
		boolean hasInterfaces = node.interfaces.size() > 0;
		if (hasParents) {
			// Since we're in the generation phase, parents won't be created
			// yet.
			boolean parentRenamed = mappings.containsKey(node.superName);
			ClassNode parentNode = nodes.get(node.superName);
			if (parentNode != null && !parentRenamed) {
				boolean conflict = ParentUtils.isLoop(node, nodes, 0);
				if (conflict) {
					// Really ugly hack for when class super names loop.
					// Only happens on a few obfuscated samples
					parentNode.superName = "java/lang/Object";
				} else {
					generateClassMapping(parentNode, nodes, mappings);
				}
			}
		}
		if (hasInterfaces) {
			// For each interface if it has a ClassNode, map it.
			for (String interfaze : node.interfaces) {
				boolean interfaceRenamed = mappings.containsKey(interfaze);
				ClassNode interfaceNode = nodes.get(interfaze);
				if (interfaceNode != null && !interfaceRenamed) {
					generateClassMapping(interfaceNode, nodes, mappings);
				}
			}
		}
		// Now map the ClassNode given by the parameter 'node'
		if (!mappings.containsKey(node.name)) {
			MappedClass mappedClass = new MappedClass(node, node.name);
			for (FieldNode fn : node.fields) {
				mappedClass.addField(new MappedMember(mappedClass, fn, mappedClass.getFields().size(), fn.desc, fn.name));
			}
			for (MethodNode mn : node.methods) {
				mappedClass.addMethod(new MappedMember(mappedClass, mn, mappedClass.getMethods().size(), mn.desc, mn.name));
			}
			mappings.put(node.name, mappedClass);
		}
		return mappings;
	}

	/**
	 * Iterates through entries in the given map and matches together parent and
	 * child classes.
	 * 
	 * @param mappings
	 * @return
	 */
	public static Map<String, MappedClass> linkMappings(MappedClass mc, Map<String, MappedClass> mappings) {
		// Setting up parent structure
		if (!mc.hasParent()) {
			// No parent, check to see if one can be found
			MappedClass parentMappedClass = mappings.get(mc.getNode().superName);
			if (parentMappedClass != null) {
				mappings = linkMappings(parentMappedClass, mappings);
				parentMappedClass.addChild(mc);
				mc.setParent(parentMappedClass);
			}
		}
		// Adding interfaces
		if (mc.getInterfaces().size() == 0) {
			for (String interfaze : mc.getNode().interfaces) {
				MappedClass mappedInterface = mappings.get(interfaze);
				if (mappedInterface != null) {
					mappings = linkMappings(mappedInterface, mappings);
					mc.addInterface(mappedInterface);
					mappedInterface.addChild(mc);
				}
			}
		}
		// Setting up outer/inner class structure
		if (mc.getOuterClass() == null) {
			boolean outerClassASM = mc.getNode().outerClass != null;
			boolean outerClassName = mc.getOriginalName().contains("$");
			String outerClass = null;
			if (outerClassASM) {
				outerClass = mc.getNode().outerClass;
			} else if (outerClassName) {
				outerClass = mc.getOriginalName().substring(0, mc.getOriginalName().indexOf("$"));
				if (outerClass.endsWith("/")) {
					// TODO: Do this better, account for obfuscations that
					// purposefully put $'s in names
					// The name starts with the $ so probably not actually an
					// outer class. Just obfuscation.
					outerClass = null;
				}
			} else {
				int synths = 0, synthID = -1;
				for (int fieldKey = 0; fieldKey < mc.getFields().size(); fieldKey++) {
					// Check for synthetic fields
					FieldNode fn = mc.getFields().get(fieldKey).getFieldNode();
					if (fn == null) {
						continue;
					}
					int access = fn.access;
					if (AccessHelper.isSynthetic(access) && AccessHelper.isFinal(access) && !AccessHelper.isPublic(access) && !AccessHelper.isPrivate(access)
							&& !AccessHelper.isProtected(access)) {
						synths++;
						synthID = fieldKey;
					}
				}
				if (synths == 1) {
					// If there is a single synthetic field referencing a class,
					// it's probably an anonymous inner class.
					FieldNode fn = mc.getFields().get(synthID).getFieldNode();
					if (fn != null && fn.desc.contains(";")) {
						List<String> matches = RegexUtils.matchDescriptionClasses(fn.desc);
						if (matches.size() > 0) {
							outerClass = matches.get(0);
						}
					}
				}
			}
			// Adding inner classes
			if (outerClass != null) {
				MappedClass outer = mappings.get(outerClass);
				if (outer != null) {
					outer.addInnerClass(mc);
					mc.setOuterClass(outer);
					mappings = linkMappings(outer, mappings);
				}
			}
		}
		// Adding method overrides
		for (MappedMember method : mc.getMethods()) {
			// Already checked for overrides. Skip.

			addOverrides(method);
		}
		mappings.put(mc.getOriginalName(), mc);
		return mappings;
	}

	/**
	 * Given a MappedMember searches for members in parent classes that match
	 * the same name & desc.
	 * 
	 * @param method
	 */
	private static void addOverrides(MappedMember method) {
		// This method has already been searched. Skip.
		if (method.getFirstOverride() != null) {
			return;
		}
		MappedClass mappedClass = method.getOwner();
		// Skip if already searched for methods
		List<MappedMember> methodOverridens = new ArrayList<MappedMember>();
		MappedClass parent = mappedClass.getParent();
		// Search the parents (search is inclusive from parent all the way up
		// the class hierarchy) for matching methods.
		if (parent != null) {
			MappedMember parentMethod = ParentUtils.findMethodInParentInclusive(parent, method.getOriginalName(), method.getDesc(), true);
			if (parentMethod != null) {
				methodOverridens.add(parentMethod);
			}
		}
		// Search the interfaces (search is inclusive from parent all the way up
		// the class hierarchy) for matching methods.
		for (MappedClass interfacee : mappedClass.getInterfaces()) {
			MappedMember interfaceMethod = ParentUtils.findMethodInParentInclusive(interfacee, method.getOriginalName(), method.getDesc(), true);
			if (interfaceMethod != null) {
				methodOverridens.add(interfaceMethod);
			}
		}
		// Add each override.
		for (MappedMember override : methodOverridens) {
			// Check the overriden member for additional overrides.
			addOverrides(override);
			// Don't add duplicates. Set up override structure for the given
			// method and the override.
			if (!method.getOverrides().contains(override)) {
				method.addOverride(override);
				override.addMemberThatOverridesMe(method);
				method.setIsLibrary(override.isLibrary());
			}
		}
	}
}
