package me.lpk.util;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMUtils {
	/**
	 * Gets the bytes of a given ClassNode.
	 * 
	 * @param cn
	 * @param useMaxs
	 * @return
	 */
	public static byte[] getNodeBytes(ClassNode cn, boolean useMaxs) {
		ClassWriter cw = new ClassWriter(useMaxs ? ClassWriter.COMPUTE_MAXS : ClassWriter.COMPUTE_FRAMES);
		cn.accept(cw);
		byte[] b = cw.toByteArray();
		return b;
	}

	/**
	 * Gets a ClassNode based on given bytes
	 * 
	 * @param bytez
	 * @return
	 */
	public static ClassNode getNode(final byte[] bytez) {
		ClassReader cr = new ClassReader(bytez);
		ClassNode cn = new ClassNode();
		try {
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
		} catch (Exception e) {
			try {
				cr.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			} catch (Exception e2) {
				// e2.printStackTrace();
			}
		}
		cr = null;
		return cn;
	}

	/**
	 * Generates a getter method for the specified field
	 * 
	 * @author Xerces
	 * @param fieldName
	 *            the name of the field
	 * @param className
	 *            the internal class name
	 * @param fieldDesc
	 *            the field descriptor
	 * @param methodName
	 *            the name of the method to create
	 * @return the method as a {@link org.objectweb.asm.tree.MethodNode}
	 */
	public static MethodNode generateGetter(String methodName, String fieldName, String className, String fieldDesc) {
		MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName, "()" + fieldDesc, null, null);
		methodNode.instructions.insert(new VarInsnNode(ALOAD, 0));
		methodNode.instructions.insert(new FieldInsnNode(GETFIELD, className, fieldName, fieldDesc));
		methodNode.instructions.insert(new InsnNode(Type.getType(fieldDesc).getOpcode(IRETURN)));
		return methodNode;
	}

	/**
	 * Generates a setter method for the specified field
	 * 
	 * @author Xerces
	 * @param fieldName
	 *            the name of the field
	 * @param className
	 *            the internal class name
	 * @param fieldDesc
	 *            the field descriptor
	 * @param methodName
	 *            the name of the method to create
	 * @return the method as a {@link org.objectweb.asm.tree.MethodNode}
	 */
	public static MethodNode generateSetter(String methodName, String fieldName, String className, String fieldDesc) {
		MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName, "(" + fieldDesc + ")V", null, null);
		methodNode.instructions.insert(new VarInsnNode(ALOAD, 0));
		methodNode.instructions.insert(new VarInsnNode(Type.getType(fieldDesc).getOpcode(ILOAD), 1));
		methodNode.instructions.insert(new FieldInsnNode(PUTFIELD, className, fieldName, fieldDesc));
		methodNode.instructions.insert(new InsnNode(RETURN));
		return methodNode;
	}

	/**
	 * Adds interfaces to a class
	 * 
	 * @author Xerces
	 * @param classNode
	 *            the {@link org.objectweb.asm.tree.ClassNode} to add the
	 *            interfaces too
	 * @param interfaces
	 *            a {@link java.lang.Class} array of the interfaces to add
	 */
	public static void addInterfaces(ClassNode classNode, Class<?>[] interfaces) {
		for (Class<?> interfaceClass : interfaces) {
			if (interfaceClass.isInterface()) {
				classNode.interfaces.add(interfaceClass.getName().replaceAll(".", "/"));
			}
		}
	}
}