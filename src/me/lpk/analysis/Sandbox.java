package me.lpk.analysis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.util.AccessHelper;
import me.lpk.util.OpUtils;

public class Sandbox {
	/**
	 * Invokes the method through reflection. Other methods and fields are
	 * removed to prevent accidental execution.
	 * 
	 * @param owner
	 *            Decryption classnode
	 * @param min
	 *            Decryption method
	 * @param args
	 *            Decryption method args
	 * @return
	 */
	public static Object getIsolatedReturn(ClassNode owner, MethodInsnNode min, Object[] args) {
		if (owner == null) {
			return null;
		}
		ClassNode isolated = new ClassNode();
		isolated.version = 52;
		isolated.name = owner.name;
		isolated.superName = "java/lang/Object";
		int i = 0;
		for (MethodNode mn : owner.methods) {
			if (mn.name.equals(min.name) && mn.desc.equals(min.desc)) {
				isolated.methods.add(owner.methods.get(i));
			}
			i++;
		}
		return get(isolated, min.name, min.desc, args);
	}

	/**
	 * Simulates a method call from a given method. All methods are removed
	 * except the proxy call and the decrypt call.
	 * 
	 * @param src
	 *            The method origin.
	 * @param owner
	 *            Decryption classnode.
	 * @param min
	 *            Decryption method.
	 * @param args
	 *            Decryption method args.
	 * @return
	 */
	public static Object getProxyIsolatedReturn(MethodNode src, ClassNode owner, MethodInsnNode min, Object[] args) {
		if (owner == null) {
			return null;
		}
		// Creating the proxy class
		ClassNode proxy = new ClassNode();
		proxy.version = 52;
		proxy.name = src.owner;
		proxy.superName = "java/lang/Object";
		MethodNode methodCallProxy = new MethodNode();
		methodCallProxy.name = src.name;
		methodCallProxy.desc = "()Ljava/lang/String;";
		methodCallProxy.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		methodCallProxy.exceptions = new ArrayList<String>();
		methodCallProxy.owner = proxy.name;
		for (Object o : args) {
			boolean n = o instanceof Number;
			if (o instanceof String || (n && !(o instanceof Integer))) {
				methodCallProxy.instructions.add(new LdcInsnNode(o));
			} else if (n) {
				methodCallProxy.instructions.add(OpUtils.toInt(((Number) o).intValue()));
			}
		}
		methodCallProxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, min.owner, min.name, min.desc, false));
		methodCallProxy.instructions.add(new InsnNode(Opcodes.ARETURN));
		methodCallProxy.visitMaxs(args.length, 0);
		proxy.methods.add(methodCallProxy);
		// Now that the proxy call is done, lets see if the call is to the same
		// class.
		if (owner.name.equals(min.owner)) {
			// Adding method to be called
			for (MethodNode mn : owner.methods) {
				if (mn.name.equals(min.name) && mn.desc.equals(min.desc)) {
					proxy.methods.add(mn);
				}
			}
			// Get the value by calling the proxy.
		} else {
			// Creating the isolated class
			ClassNode isolated = new ClassNode();
			isolated.version = 52;
			isolated.name = min.owner;
			isolated.superName = "java/lang/Object";
			for (MethodNode mn : owner.methods) {
				if (mn.name.equals(min.name) && mn.desc.equals(min.desc)) {
					isolated.methods.add(mn);
				}
			}
			// Load the target isolated class
			load(isolated);
		}
		// Get the value by calling the proxy.
		return get(proxy, methodCallProxy.name, "()Ljava/lang/String;", new Object[] {});
	}

	/**
	 * Simulates a method call from a given method.
	 * 
	 * @param src
	 *            The method origin.
	 * @param owner
	 *            Decryption classnode.
	 * @param min
	 *            Decryption method.
	 * @param args
	 *            Decryption method args.
	 * @return
	 */
	public static Object getProxyReturn(MethodNode src, ClassNode owner, MethodInsnNode min, Object[] args) {
		if (owner == null) {
			return null;
		}
		// Creating the proxy class
		ClassNode proxy = new ClassNode();
		proxy.name = min.owner;
		proxy.superName = "java/lang/Object";
		proxy.version = 52;
		proxy.access = Opcodes.ACC_PUBLIC;
		MethodNode methodCallProxy = new MethodNode();
		methodCallProxy.name = src.name;
		methodCallProxy.desc = "()Ljava/lang/String;";
		methodCallProxy.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		methodCallProxy.exceptions = new ArrayList<String>();
		methodCallProxy.owner = proxy.name;
		for (Object o : args) {
			boolean n = o instanceof Number;
			if (o instanceof String || (n && !(o instanceof Integer))) {
				methodCallProxy.instructions.add(new LdcInsnNode(o));
			} else if (n) {
				methodCallProxy.instructions.add(OpUtils.toInt(((Number) o).intValue()));
			}
		}
		methodCallProxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, min.owner, min.name, min.desc, false));
		methodCallProxy.instructions.add(new InsnNode(Opcodes.ARETURN));
		methodCallProxy.visitMaxs(args.length, 0);
		proxy.methods.add(methodCallProxy);
		// Now that the proxy call is done
		// Sometimes classes/methods will have improper access, so we're gonna
		// have to fix that.
		owner.version = 52;
		owner.access = Opcodes.ACC_PUBLIC;
		for (MethodNode mn : owner.methods) {
			boolean isStatic = AccessHelper.isStatic(mn.access);
			mn.access = Opcodes.ACC_PUBLIC;
			if (isStatic) {
				mn.access |= Opcodes.ACC_STATIC;
			}
		}
		// lets see if the call is to the same class.
		if (owner.name.equals(min.owner)) {
			// Adding method to be called
			for (MethodNode mn : owner.methods) {
				// Copy over methods except the one we're calling from.
				if (!(mn.name.equals(src.name) && mn.desc.equals(src.desc))) {
					proxy.methods.add(mn);
				}
			}
			// And the fields
			for (FieldNode fn : owner.fields){
				proxy.fields.add(fn);
			}
		} else {
			// Load the target class
			load(owner);
		}
		// Get the value by calling the proxy.
		return get(proxy, methodCallProxy.name, "()Ljava/lang/String;", new Object[] {});
	}
	
	/**
	 * Fuck Stringer
	 * @param src
	 * @param owner
	 * @param min
	 * @param args
	 * @return
	 */
	public static Object getProxyReturnStringer(String callerCaller, MethodNode src, ClassNode owner, MethodInsnNode min, Object[] args) {
		if (owner == null) {
			return null;
		}
		// Creating the proxy class
		ClassNode proxy = new ClassNode();
		proxy.name = min.owner;
		proxy.superName = "java/lang/Object";
		proxy.version = 52;
		proxy.access = Opcodes.ACC_PUBLIC;
		MethodNode methodCallProxy = new MethodNode();
		methodCallProxy.name = src.name;
		methodCallProxy.desc = "()Ljava/lang/String;";
		methodCallProxy.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		methodCallProxy.exceptions = new ArrayList<String>();
		methodCallProxy.owner = proxy.name;
		for (Object o : args) {
			boolean n = o instanceof Number;
			if (o instanceof String || (n && !(o instanceof Integer))) {
				methodCallProxy.instructions.add(new LdcInsnNode(o));
			} else if (n) {
				methodCallProxy.instructions.add(OpUtils.toInt(((Number) o).intValue()));
			}
		}
		methodCallProxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, min.owner, min.name, min.desc, false));
		methodCallProxy.instructions.add(new InsnNode(Opcodes.ARETURN));
		methodCallProxy.visitMaxs(args.length, 0);
		proxy.methods.add(methodCallProxy);
		// Second proxy class because #Stringer
		ClassNode proxy2 = new ClassNode();
		proxy2.name = callerCaller;
		proxy2.superName = "java/lang/Object";
		proxy2.version = 52;
		proxy2.access = Opcodes.ACC_PUBLIC;
		MethodNode methodCallProxy2 = new MethodNode();
		methodCallProxy2.name = "sandbox";
		methodCallProxy2.desc = "()Ljava/lang/String;";
		methodCallProxy2.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		methodCallProxy2.exceptions = new ArrayList<String>();
		methodCallProxy2.owner = proxy2.name;
		methodCallProxy2.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, methodCallProxy.owner, methodCallProxy.name, methodCallProxy.desc, false));
		methodCallProxy2.instructions.add(new InsnNode(Opcodes.ARETURN));
		methodCallProxy2.visitMaxs(1, 0);
		proxy2.methods.add(methodCallProxy2);

		//
		// Now that the proxy call is done
		// Sometimes classes/methods will have improper access, so we're gonna
		// have to fix that.
		owner.version = 52;
		owner.access = Opcodes.ACC_PUBLIC;
		for (MethodNode mn : owner.methods) {
			boolean isStatic = AccessHelper.isStatic(mn.access);
			mn.access = Opcodes.ACC_PUBLIC;
			if (isStatic) {
				mn.access |= Opcodes.ACC_STATIC;
			}
		}
		// lets see if the call is to the same class.
		if (owner.name.equals(min.owner)) {
			// Adding method to be called
			for (MethodNode mn : owner.methods) {
				// Copy over methods except the one we're calling from.
				if (!(mn.name.equals(src.name) && mn.desc.equals(src.desc))) {
					proxy.methods.add(mn);
				}
			}
			// And the fields
			for (FieldNode fn : owner.fields){
				proxy.fields.add(fn);
			}
		} else {
			// Load the target class
			load(owner);
		}
		load(proxy);
		// Get the value by calling the proxy.
		return get(proxy2, methodCallProxy2.name, methodCallProxy2.desc, new Object[] {});
	}

	/**
	 * Invokes the method through reflection. The entire class is kept in-tact.
	 * 
	 * @param owner
	 *            Decryption class
	 * @param min
	 *            Decryption method
	 * @param args
	 *            Decryption method args
	 * @return
	 */
	public static Object getReturn(ClassNode owner, MethodInsnNode min, Object[] args) {
		if (owner == null) {
			return null;
		}
		return get(owner, min.name, min.desc, args);
	}
	



	/**
	 * Get the return value of a node's method given by it's name, description,
	 * and passed args.
	 * 
	 * @param cn
	 *            The class node to call.
	 * @param name
	 *            Name of the method to call.
	 * @param desc
	 *            Descriptor of the method to call.
	 * @param args
	 *            Arguments.
	 * @return
	 */
	private static Object get(ClassNode cn, String name, String desc, Object[] args) {
		try {
			Class<?> clazz = load(cn);
			for (Method m : clazz.getMethods()) {
				boolean b2 = m.getName().equals(name), b3 = Type.getMethodDescriptor(m).equals(desc);
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
		}
		return null;
	}

	/**
	 * Load a class by it's node.
	 * 
	 * @param cn
	 *            ClassNode to load. The static block is stripped so accidental
	 *            invocation won't occur.
	 * @return
	 */
	public static Class<?> load(ClassNode cn) {
		ClassWriter cw = new ClassWriter(0);
		cn.accept(new VisitorImpl(cw));
		return new ClassDefiner(ClassLoader.getSystemClassLoader()).get(cn.name.replace("/", "."), cw.toByteArray());
	}

	/**
	 * Classloader that loads a class from bytes.
	 */
	static class ClassDefiner extends ClassLoader {
		public ClassDefiner(ClassLoader parent) {
			super(parent);
		}

		public Class<?> get(String name, byte[] bytes) {
			Class<?> c = defineClass(name, bytes, 0, bytes.length);
			resolveClass(c);
			return c;
		}
	}

	/**
	 * Class visitor that strips information from a class. Makes the class and
	 * methods public.
	 */
	public static class VisitorImpl extends ClassVisitor {

		public VisitorImpl(ClassVisitor cv) {
			super(Opcodes.ASM5, cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (name.startsWith("<c")) {
				// We DO NOT want static blocks.
				return null;
			}
			access = AccessHelper.isPublic(access) ? access : access | Opcodes.ACC_PUBLIC;
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			access = AccessHelper.isPublic(access) ? access : access | Opcodes.ACC_PUBLIC;
			super.visit(version, access, name, signature, superName, interfaces);
		}
	}
}
