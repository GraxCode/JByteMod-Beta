package me.lpk.mapping.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

import me.lpk.mapping.MappedClass;

public abstract class MappingLoader {
	protected final Map<String, ClassNode> nodes;

	/**
	 * Instantiates the loader with a map of classnodes to be mapped.
	 * 
	 * @param nodes
	 */
	public MappingLoader(Map<String, ClassNode> nodes) {
		this.nodes = nodes;
	}

	public abstract Map<String, MappedClass> read(FileReader in);

	public abstract Map<String, MappedClass> read(BufferedReader fileReader) throws Exception;

	public abstract void save(Map<String, MappedClass> mappings, File file);

	/**
	 * Creates a fake node containing only a name attribute.
	 * 
	 * @param name
	 * @return
	 */
	protected ClassNode fakeNode(String name) {
		ClassNode cn = new ClassNode();
		cn.name = name;
		return cn;
	}
}
