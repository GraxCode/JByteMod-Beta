package me.lpk.antis.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.analysis.Sandbox;
import me.lpk.antis.AntiBase;
import me.lpk.util.Classpather;
import me.lpk.util.JarClassLoader;
import me.lpk.util.OpUtils;

public class AntiZKM8 extends AntiBase {
	public static final String clinit = "<clinit>";
	private boolean canRemove = true;

	public AntiZKM8(File jar) {
		super(null);
		try {
			Classpather.addFile(jar);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		try {
//			Thread.currentThread().setContextClassLoader(new JarClassLoader(jar.getAbsolutePath()));
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public ClassNode scan(ClassNode node) {
		MethodNode clinit = null;
		MethodNode decrypt = null;
		for (MethodNode mnode : node.methods) {
			if (mnode.name.startsWith("<c")) {
				clinit = mnode;
			} else if (mnode.desc.endsWith("(II)Ljava/lang/String;")) {
				// TODO: Check if valid decrypt method
				decrypt = mnode;
			}
		}
		if (clinit == null || decrypt == null) {
			return node;
		}
		ClassNode cn = new ClassNode();
		cn.name = node.name;
		cn.superName = "java/lang/Object";
		cn.version = 52;
		cn.access = Opcodes.ACC_PUBLIC;
		// TODO: Edit the clinit so that anything extra isn't called (cut up to
		// certain point)
		// TODO: Make check for single-string decrypt.
		int oldAcc = clinit.access;
		clinit.name = AntiZKM8.clinit;
		clinit.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		decrypt.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		cn.methods.add(clinit);
		cn.methods.add(decrypt);
		for (FieldNode fnode : node.fields) {
			fnode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
			cn.fields.add(fnode);
		}
		for (MethodNode mnode : node.methods) {
			replace(cn, mnode);
		}
		if (canRemove) {
			cn.methods.remove(1);
		}
		clinit.name ="<clinit>";
		clinit.access = oldAcc;
		// TODO: Clean up clinit (remove ZKM blob)
		return node;
	}

	private void replace(ClassNode cn, MethodNode method) {
		AbstractInsnNode ain = method.instructions.getFirst();
		while (ain != null) {
			if (ain.getOpcode() == Opcodes.INVOKESTATIC) {
				MethodInsnNode min = ((MethodInsnNode) ain);
				String desc = min.desc;
				if (desc.equals("(II)Ljava/lang/String;")) {
					int opIndex = OpUtils.getIndex(ain);
					if (opIndex < 0 || opIndex >= method.instructions.size()) {
						ain = ain.getNext();
						continue;
					}
					try {
					Object[] args = new Object[] { OpUtils.getIntValue(ain.getPrevious().getPrevious()), OpUtils.getIntValue(ain.getPrevious()) };
					Object o = getZKMReturn(cn, min, args);
					if (o != null) {
						System.out.println(o.toString());
						method.instructions.remove(min.getPrevious());
						method.instructions.remove(min.getPrevious());
						method.instructions.set(min, new LdcInsnNode(o));
						ain = method.instructions.getFirst();
					} else {
						canRemove = false;
					}
					} catch(Error e) {
						e.printStackTrace();
						canRemove = false;
					}
				}
			}
			ain = ain.getNext();
		}
	}

	/**
	 * Loads the ZKM class and decrypts a string based on the given arguments.
	 * 
	 * @param cn
	 * @param decrypt
	 * @param min
	 * @param args
	 * @return
	 */
	public static Object getZKMReturn(ClassNode cn, MethodInsnNode min, Object[] args) {
		try {
			Class<?> clazz = Sandbox.load(cn);
			for (Method m : clazz.getMethods()) {
				if (m.getName().equals(AntiZKM8.clinit) && m.getParameterCount() == 0) {
					m.setAccessible(true);
					m.invoke(null, new Object[] {});
				}
			}
			for (Method m : clazz.getMethods()) {
				boolean b2 = m.getName().equals(min.name), b3 = Type.getMethodDescriptor(m).equals(min.desc);
				if (b2 && b3) {
					m.setAccessible(true);
					return m.invoke(null, args);
				}
			}
		} catch (InvocationTargetException ite) {
			ite.getTargetException().printStackTrace();
		} catch (IllegalAccessError iae) {
			iae.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}
}
