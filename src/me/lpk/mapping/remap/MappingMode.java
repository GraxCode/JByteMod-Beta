package me.lpk.mapping.remap;

import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;

public abstract class MappingMode {
	/**
	 * Creates a new name for a given class
	 * 
	 * @param cn
	 * @return
	 */
	public abstract String getClassName(MappedClass cn);

	/**
	 * Creates a new name for a given method
	 * 
	 * @param mn
	 * @return
	 */
	public abstract String getMethodName(MappedMember mn);

	/**
	 * Creates a new name for a given field
	 * 
	 * @param fn
	 * @return
	 */
	public abstract String getFieldName(MappedMember fn);
}