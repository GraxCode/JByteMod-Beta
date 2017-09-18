package me.lpk.mapping;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;

import me.lpk.util.ParentUtils;

/**
 * A ClassWriter that works off of MappedClasses. Does not require classes being
 * loaded into the JVM like a standard ClassWriter.
 */
public class MappingClassWriter extends ClassWriter {
	private final Map<String, MappedClass> mappings;
	private final Map<String, MappedClass> mappingsInv = new HashMap<String, MappedClass>();

	public MappingClassWriter(Map<String, MappedClass> mappings, int i) {
		super(i);
		this.mappings = mappings;
		for (MappedClass mc : mappings.values()) {
			mappingsInv.put(mc.getNewName(), mc);
		}
	}

	@Override
	protected String getCommonSuperClass(final String type1, final String type2) {
		MappedClass mc1 = mappings.getOrDefault(type1, mappingsInv.get(type1));
		MappedClass mc2 = mappings.getOrDefault(type2, mappingsInv.get(type2));
		if (mc1 == null || mc2 == null) {
			return "java/lang/Object";
		}
		MappedClass common = ParentUtils.findCommonParent(mc1, mc2);
		if (common == null) {
			return "java/lang/Object";
		}
		return common.getOriginalName();
	}
}