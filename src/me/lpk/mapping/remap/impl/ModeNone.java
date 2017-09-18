package me.lpk.mapping.remap.impl;

import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;
import me.lpk.mapping.remap.MappingMode;

public class ModeNone extends MappingMode {
	@Override
	public String getClassName(MappedClass cn) {
		return cn.getOriginalName();
	}

	@Override
	public String getMethodName(MappedMember mn) {
		return mn.getOriginalName();
	}

	@Override
	public String getFieldName(MappedMember fn) {
		return fn.getOriginalName();
	}
}