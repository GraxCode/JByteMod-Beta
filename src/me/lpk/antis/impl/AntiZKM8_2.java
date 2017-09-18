package me.lpk.antis.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.analysis.Sandbox;
import me.lpk.analysis.StackFrame;
import me.lpk.analysis.StackUtil;
import me.lpk.antis.AntiBase;
import me.lpk.util.OpUtils;

public class AntiZKM8_2 extends AntiBase {
	public static final String clinit = "init_zkm";
	private boolean canRemove = true;

	public AntiZKM8_2() {
		super(null);
	}

	@Override
	public ClassNode scan(ClassNode node) {
		MethodNode clinit = null;
		for (MethodNode mnode : node.methods) {
			if (mnode.name.startsWith("<c")) {
				clinit = mnode;
			}
		}
		if (clinit == null) {
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
		clinit.name = AntiZKM8_2.clinit;
		clinit.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		cn.methods.add(clinit);
		for (FieldNode fnode : node.fields) {
			fnode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
			cn.fields.add(fnode);
		}
		System.out.println("oke");
//		getZKMReturn(cn, null, null);
		for (MethodNode mnode : node.methods) {
			replace(cn, mnode);
		}
		if (canRemove) {
			cn.methods.remove(1);
		}
		clinit.name = "<clinit>";
		clinit.access = oldAcc;
		// TODO: Clean up clinit (remove ZKM blob)
		return node;
	}

	private void replace(ClassNode cn, MethodNode method) {
		FieldNode field;
		for (FieldNode fn : cn.fields) {
			if (fn.desc.equals("[Ljava/lang/String;")) {
				field = fn;
			}
		}
		Object o = getArrayReturn(cn);
		if (o != null) {
			System.out.println("WIN!");
			return;
		}
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
					Object[] args = new Object[] { OpUtils.getIntValue(ain.getPrevious().getPrevious()), OpUtils.getIntValue(ain.getPrevious()) };
					Object ob = getArrayReturn(cn);
					if (ob != null) {
						System.out.println("WIN!");
//						method.instructions.remove(min.getPrevious());
//						method.instructions.remove(min.getPrevious());
//						method.instructions.set(min, new LdcInsnNode(o));
//						ain = method.instructions.getFirst();
					} else {
//						canRemove = false;
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
	public static Object getArrayReturn(ClassNode cn) {
		try {
			for (MethodNode mn : cn.methods) {
				if (mn.name.startsWith("<cl")) {
					StackFrame[] stack = StackUtil.getFrames(mn);
					int i = 0;
					for (AbstractInsnNode ain : mn.instructions.toArray()) {
						if (ain.getOpcode() == Opcodes.PUTSTATIC && ain.getNext().getOpcode() == Opcodes.GOTO && ain.getPrevious().getOpcode() == Opcodes.ALOAD) {
							FieldInsnNode fin = (FieldInsnNode) ain;
							if (fin.desc.equals("[Ljava/lang/String;")) {
								mn.instructions.set(ain.getNext(), new InsnNode(Opcodes.RETURN));
							}
						}
						if (ain.getOpcode() == Opcodes.INVOKESTATIC) {
							MethodInsnNode min = (MethodInsnNode) ain;
							if (!min.owner.startsWith("java/") && !min.owner.startsWith("sun/")) {
								mn.instructions.insertBefore(ain, new InsnNode(Opcodes.POP)); //TODO: calc taken size
								mn.instructions.remove(ain);
							}
						}
						i++;
					}
				}
			}
			Class<?> clazz = Sandbox.load(cn);
			HashMap<Field, Object> possible = new HashMap<Field, Object>();
			for (Field f : clazz.getFields()) {
				if (f.getType().getName().equals("[Ljava.lang.String;")) {
					f.setAccessible(true);
					possible.put(f, f.get(null));
				}
			}
			for (Method m : clazz.getMethods()) {
				if (m.getName().equals(AntiZKM8_2.clinit) && m.getParameterCount() == 0) {
					m.setAccessible(true);
					try {
						System.out.println(m.getName() + " " + cn.name);
						m.invoke(null, new Object[] {});
					} catch (NoSuchMethodError e) {
						System.err.println("Couldn't decrypt " + cn.name + ", " + e.getClass().getName());
						return null;
					}
				}
			}
			for (Field f : clazz.getFields()) {
				if (f.getType().getName().equals("[Ljava.lang.String;")) {
					f.setAccessible(true);
					if (!possible.get(f).equals(f.get(null))) {
						return f.get(null);
					}
				}
			}
			System.err.println("noo");
			return null;
		} catch (InvocationTargetException ite) {
			ite.getTargetException().printStackTrace();
		} catch (IllegalAccessError iae) {
			iae.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (NoSuchMethodError e) {
			System.err.println("Couldn't decrypt " + cn.name + ", " + e.getClass().getName());
		} catch (Throwable e) {
			System.err.println("Couldn't decrypt " + cn.name + ", " + e.getClass().getName());
		}
		return null;
	}
}
