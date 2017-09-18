package me.lpk.mapping;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class MappedMember extends MappedObject {
	/**
	 * The member's owner.
	 */
	private final MappedClass owner;
	/**
	 * The FieldNode or MethodNode of the current MappedMember.
	 */
	private final Object memberNode;
	/**
	 * The index in the owner that the current member appears in.
	 */
	private final int index;
	/**
	 * The list of MappedMembers that this member overrides.
	 */
	private List<MappedMember> overrides = new ArrayList<MappedMember>();
	/**
	 * The list of MappedMembers that override this member.
	 * 
	 * TODO: Think of a better name
	 */
	private List<MappedMember> overridesMe = new ArrayList<MappedMember>();

	public MappedMember(MappedClass owner, Object memberNode, int index, String desc, String nameOriginal) {
		super(desc, nameOriginal, nameOriginal);
		this.memberNode = memberNode;
		this.owner = owner;
		this.index = index;
	}

	/**
	 * The class the member belongs to.
	 * 
	 * @return
	 */
	public MappedClass getOwner() {
		return owner;
	}

	/**
	 * The order in which the member was indexed (Ex: One field after another)
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the node of the member. Can be a Field/MethodNode.
	 * 
	 * @return
	 */
	public Object getMemberNode() {
		return memberNode;
	}

	/**
	 * Returns the node of the member as a FieldNode.
	 * 
	 * @return
	 */
	public FieldNode getFieldNode() {
		return (FieldNode) memberNode;
	}

	/**
	 * Returns the node of the member as a MethodNode.
	 * 
	 * @return
	 */
	public MethodNode getMethodNode() {
		return (MethodNode) memberNode;
	}

	/**
	 * Returns true if the memberNode is a FieldNode.
	 * 
	 * @return
	 */
	public boolean isField() {
		return memberNode == null ? false : memberNode instanceof FieldNode;
	}

	/**
	 * Returns true if the memberNode is a MethodNode.
	 * 
	 * @return
	 */
	public boolean isMethod() {
		return memberNode == null ? false : memberNode instanceof MethodNode;
	}

	/**
	 * Returns true if the member overrides another member.
	 * 
	 * @return
	 */
	public boolean doesOverride() {
		return overrides.size() > 0;
	}

	/**
	 * Returns true if this member is overriden by another.
	 * 
	 * @return
	 */
	public boolean isOverriden() {
		return overridesMe.size() > 0;
	}

	/**
	 * Gets the first MappedMember this member overrides. May be null.
	 * 
	 * @return
	 */
	public MappedMember getFirstOverride() {
		return doesOverride() ? overrides.get(0) : null;
	}

	/**
	 * Gets the list of MappedMembers that are overriden by this.
	 * 
	 * @return
	 */
	public List<MappedMember> getOverrides() {
		return overrides;
	}

	/**
	 * Gets the list of MappedMembers that override this.
	 * 
	 * TODO: think of a better name
	 * 
	 * @return
	 */
	public List<MappedMember> getMyOverrides() {
		return overridesMe;
	}

	/**
	 * Sets the overridden (method) member object.
	 * 
	 * @param override
	 */
	public void addOverride(MappedMember override) {
		overrides.add(override);
	}

	/**
	 * Adds a member to the list that shows methods overriding this. <br>
	 * TODO: Better name for this method
	 * 
	 * @param overrider
	 */
	public void addMemberThatOverridesMe(MappedMember overrider) {
		overridesMe.add(overrider);
	}

}
