package me.lpk.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.lpk.analysis.Sandbox;

public class InjectedClassLoader extends URLClassLoader {
	public static final HashMap<String, byte[]> extraClassDefs = new HashMap<String, byte[]>();
	public static final HashMap<String, ClassNode> nodes = new HashMap<String, ClassNode>();

	public InjectedClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			ClassNode cn = nodes.get(name);
			if (cn != null) {
				ClassWriter cw = new ClassWriter(0);
				cn.accept(new Sandbox.VisitorImpl(cw));
				byte[] b = cw.toByteArray();
				return defineClass(cn.name, b, 0, b.length);
			}
		} catch (Throwable e) {
			try {
				return super.findClass(name);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Class loadClass(String s) {
		try {
			ClassNode cn = nodes.get(s);
			if (cn != null) {
				ClassWriter cw = new ClassWriter(0);
				cn.accept(new Sandbox.VisitorImpl(cw));
				byte[] b = cw.toByteArray();
				return defineClass(cn.name, b, 0, b.length);
			}
		} catch (Throwable e) {
			try {
				return super.loadClass(s);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
}