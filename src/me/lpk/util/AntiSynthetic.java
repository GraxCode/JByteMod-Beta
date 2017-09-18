package me.lpk.util;


import java.lang.reflect.Field;

import org.objectweb.asm.Opcodes;

public class AntiSynthetic implements Opcodes {
	public static int inverseSynthetic(int access) throws Exception {
		int i = 0;
		for (Field f : Opcodes.class.getFields()) {
			if (f.getName().startsWith("ACC_") && !f.getName().equals("ACC_SYNTHETIC")) {
				int accval = (int) f.get(null);
				if(hasAccess(access, accval)) {
					i |= accval;
				}
			}
		}
		return i;
	}
	
	private static boolean hasAccess(int mod, int access) {
		return (mod & access) != 0;
	}
}
