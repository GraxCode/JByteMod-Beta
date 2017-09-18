package me.lpk.mapping;

class MappedObject {
	private final String nameOriginal;
	private String desc, nameNew;
	private boolean isRenamedOverride, isLibrary;

	public MappedObject(String desc, String nameOriginal, String nameNew) {
		this.desc = desc;
		this.nameOriginal = nameOriginal;
		this.nameNew = nameNew;
	}

	/**
	 * Returns the bytecode description of the object.
	 * 
	 * @return
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Returns the original name of the object.
	 * 
	 * @return
	 */
	public String getOriginalName() {
		return nameOriginal;
	}

	/**
	 * Returns the new name of the object.
	 * 
	 * @return
	 */
	public String getNewName() {
		return nameNew;
	}

	/**
	 * Updates the new name.
	 * 
	 * @param nameNew
	 */
	public void setNewName(String nameNew) {
		if (isLibrary){
			return;
		}
		this.nameNew = nameNew;
	}

	/**
	 * Updates the new desc.
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Returns if the object has been renamed.
	 * 
	 * @return
	 */
	public boolean isRenamed() {
		return isTruelyRenamed() || isRenamedOverride;
	}

	public boolean isTruelyRenamed() {
		return !nameOriginal.equals(nameNew) || isLibrary;
	}

	/**
	 * Sets the override for isRenamed().
	 * 
	 * @param isRenamedOverride
	 */
	public void setRenamedOverride(boolean isRenamedOverride) {
		this.isRenamedOverride = isRenamedOverride;
	}
	
	/**
	 * Returns true if the Mapped object is considered to be a library object.
	 * Library objects are not to be modified.
	 * 
	 * @return
	 */
	public boolean isLibrary() {
		return isLibrary;
	}

	/**
	 * Sets isLibrary. See {@link #isLibrary()} for details.
	 * 
	 * @param isLibrary
	 */
	public void setIsLibrary(boolean isLibrary) {
		this.isLibrary = isLibrary;
	}
}
