package me.lpk.mapping;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;

public class MappingProcessor {

	/**
	 * Given a map of ClassNodes and mappings, returns a map of class names to
	 * class bytes.
	 * 
	 * @param nodes
	 * @param mappings
	 * @return
	 */
	public static Map<String, byte[]> process(Map<String, ClassNode> nodes, Map<String, MappedClass> mappings, boolean useMaxs) {
		Map<String, byte[]> out = new HashMap<String, byte[]>();
		SkidRemapper mapper = new SkidRemapper(mappings);
		try {
			for (ClassNode cn : nodes.values()) {
				ClassWriter cw = new MappingClassWriter(mappings, useMaxs ? ClassWriter.COMPUTE_MAXS : ClassWriter.COMPUTE_FRAMES);
				ClassVisitor remapper = new ClassRemapper(cw, mapper);
				cn.accept(remapper);
				out.put(mappings.containsKey(cn.name) ? mappings.get(cn.name).getNewName() : cn.name, cw.toByteArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
}
