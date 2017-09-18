package me.lpk.mapping.remap.impl;
import java.util.HashSet;
import java.util.Set;

import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;
import me.lpk.mapping.remap.MappingMode;
import me.lpk.util.Characters;

public class ModeUnicodeEvil extends MappingMode {
	public static final int UNICODE_MAX_LENGTH = 166;
	private Set<String> used = new HashSet<String>();

	@Override
	public String getClassName(MappedClass cn) {
		return randName();
	}

	@Override
	public String getMethodName(MappedMember mn) {
		return randName();
	}

	@Override
	public String getFieldName(MappedMember fn) {
		return randName();
	}

	private String randName() {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < UNICODE_MAX_LENGTH || used.contains(sb.toString())) {
			int randIndex = (int) (Math.random() * Characters.UNICODE.length);
			sb.append(Characters.UNICODE[randIndex]);
		}
		used.add(sb.toString());
		return sb.toString();
	}
}