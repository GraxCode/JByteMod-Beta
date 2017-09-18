package me.lpk.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

public class MappedClass extends MappedObject {
	/**
	 * Original name : Child
	 */
	private final Map<String, MappedClass> children = new HashMap<String, MappedClass>();
	/**
	 * Original name : Inner Class
	 */
	private final Map<String, MappedClass> inners = new HashMap<String, MappedClass>();
	/**
	 * A list of interfaces implemented by the class.
	 */
	private final List<MappedClass> interfaces = new ArrayList<MappedClass>();
	/**
	 * A list of fields belonging to the class.
	 */
	private final List<MappedMember> fields = new ArrayList<MappedMember>();
	/**
	 * A list of methods belonging to the class.
	 */
	private final List<MappedMember> methods = new ArrayList<MappedMember>();
	/**
	 * The ClassNode associated with the current MappedClass.
	 */
	private final ClassNode node;
	/**
	 * The super class.
	 */
	private MappedClass parent;
	/**
	 * The outer class, if any.
	 */
	private MappedClass outer;

	public MappedClass(ClassNode node, String nameNew) {
		super("CLASS", node.name, nameNew);
		this.node = node;
	}

	/**
	 * Finds a method given a name and description.
	 * 
	 * @param name
	 * @param useOriginalName
	 * @return
	 */
	public MappedMember findMethodByNameAndDesc(String name, String desc, boolean useOriginalName) {
		for (MappedMember mm : getMethods()) {
			if (mm.getDesc().equals(desc) && useOriginalName ? mm.getOriginalName().equals(name) : mm.getNewName().equals(name)) {
				return mm;
			}
		}
		return null;
	}

	/**
	 * Finds a field given a name and description.
	 * 
	 * @param name
	 * @param useOriginalName
	 * @return
	 */
	public MappedMember findFieldByNameAndDesc(String name, String desc, boolean useOriginalName) {
		for (MappedMember mm : getFields()) {
			if (mm.getDesc().equals(desc) && useOriginalName ? mm.getOriginalName().equals(name) : mm.getNewName().equals(name)) {
				return mm;
			}
		}
		return null;
	}

	/**
	 * Returns a list of fields matching a given name.
	 * 
	 * @param text
	 * @param useOriginalName
	 * @return
	 */
	public List<MappedMember> findFieldsByName(String text, boolean useOriginalName) {
		List<MappedMember> list = new ArrayList<MappedMember>();
		for (MappedMember mm : getFields()) {
			if (useOriginalName ? mm.getOriginalName().equals(text) : mm.getNewName().equals(text)) {
				list.add(mm);
			}
		}
		return list;
	}

	/**
	 * Returns a list of fields matching a given descriptor.
	 * 
	 * @param text
	 * @return
	 */
	public List<MappedMember> findFieldsByDesc(String text) {
		List<MappedMember> list = new ArrayList<MappedMember>();
		for (MappedMember mm : getFields()) {
			if (mm.getDesc().equals(text)) {
				list.add(mm);
			}
		}
		return list;
	}

	/**
	 * Returns a list of methods matching a given name.
	 * 
	 * @param text
	 * @param useOriginalName
	 * @return
	 */
	public List<MappedMember> findMethodsByName(String text, boolean useOriginalName) {
		List<MappedMember> list = new ArrayList<MappedMember>();
		for (MappedMember mm : getMethods()) {
			if (useOriginalName ? mm.getOriginalName().equals(text) : mm.getNewName().equals(text)) {
				list.add(mm);
			}
		}
		return list;
	}

	/**
	 * Returns a list of methods matching a given descriptor.
	 * 
	 * @param text
	 * @return
	 */
	public List<MappedMember> findMethodsByDesc(String text) {
		List<MappedMember> list = new ArrayList<MappedMember>();
		for (MappedMember mm : getMethods()) {
			if (mm.getDesc().equals(text)) {
				list.add(mm);
			}
		}
		return list;
	}

	/**
	 * Returns a map of field mappings. Keys are based on the index they appear
	 * in the class.
	 * 
	 * @return
	 */
	public List<MappedMember> getFields() {
		return fields;
	}

	/**
	 * Returns a collection of methods in the MappedClass.
	 * 
	 * @return
	 */
	public List<MappedMember> getMethods() {
		return methods;
	}

	/**
	 * Finds a field given an index.
	 * 
	 * @param key
	 * @return
	 */
	public MappedMember getField(int key) {
		return fields.get(key);
	}

	/**
	 * Finds a field given an index.
	 * 
	 * @param key
	 * @return
	 */
	public MappedMember getMethod(int key) {
		return methods.get(key);
	}

	/**
	 * Adds a child instance to the class.
	 * 
	 * @param child
	 */
	public void addChild(MappedClass child) {
		children.put(child.getOriginalName(), child);
	}


	/**
	 * Adds an interface to the class.
	 * 
	 * @param interfaze
	 */
	public void addInterface(MappedClass interfaze) {
		interfaces.add(interfaze);
	}

	/**
	 * Adds an inner class to this class.
	 * 
	 * @param child
	 */
	public void addInnerClass(MappedClass child) {
		inners.put(child.getOriginalName(), child);
	}

	/**
	 * Add a field to the class. Returns the field's index.
	 * 
	 * @param mm
	 * @return
	 */
	public void addField(MappedMember mm) {
		fields.add(mm);
	}

	/**
	 * Add a method to the class. Returns the method's index.
	 * 
	 * @param mm
	 * @return
	 */
	public void addMethod(MappedMember mm) {
		methods.add(mm);
	}

	/**
	 * Returns true if the given name is a child of this class.
	 * 
	 * @param childName
	 * @return
	 */
	public boolean hasChild(String childName) {
		return children.containsKey(childName);
	}

	/**
	 * Returns the map of child instances the class has.
	 * 
	 * @return
	 */
	public Map<String, MappedClass> getChildrenMap() {
		return children;
	}

	/**
	 * Returns the map of interfaces the class has.
	 * 
	 * @return
	 */
	public List<MappedClass> getInterfaces() {
		return interfaces;
	}

	/**
	 * Get's the ClassNode associated with the mapping.
	 * 
	 * @return
	 */
	public ClassNode getNode() {
		return node;
	}

	/**
	 * Get's the parent's mapped instance.
	 * 
	 * @return
	 */
	public MappedClass getParent() {
		return parent;
	}

	/**
	 * Set's the parent mapped instance.
	 * 
	 * @param parent
	 */
	public void setParent(MappedClass parent) {
		this.parent = parent;
	}

	/**
	 * Returns a map of inner classes.
	 * 
	 * @return
	 */
	public Map<String, MappedClass> getInnerClassMap() {
		return inners;
	}

	/**
	 * Returns the outer class.
	 * 
	 * @return
	 */
	public MappedClass getOuterClass() {
		return outer;
	}

	/**
	 * Returns true if this is an inner class.
	 * 
	 * @return
	 */
	public boolean isInnerClass() {
		return outer != null;
	}

	/**
	 * Set's the class's outer class.
	 * 
	 * @param outer
	 */
	public void setOuterClass(MappedClass outer) {
		this.outer = outer;
	}

	/**
	 * Obvious.
	 * 
	 * @return
	 */
	public boolean hasParent() {
		return parent != null;
	}
}
