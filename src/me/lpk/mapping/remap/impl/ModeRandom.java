package me.lpk.mapping.remap.impl;

import java.util.HashSet;
import java.util.Set;

import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappedMember;
import me.lpk.mapping.remap.MappingMode;
import me.lpk.util.Characters;

public class ModeRandom extends MappingMode {
	private Set<String> used = new HashSet<String>();
	private int len;

	public ModeRandom(int len) {
		this.len = len;
	}

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
		while (len > sb.length() || used.contains(sb.toString())) {
			int randIndex = (int) (Math.random() * Characters.ALPHABET_BOTH.length);
			sb.append(Characters.ALPHABET_BOTH[randIndex]);
		}
		used.add(sb.toString());
		return sb.toString();
	}
}