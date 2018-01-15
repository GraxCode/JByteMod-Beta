package org.objectweb.asm.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * A {@link ClassVisitor} that renames fields and methods, and removes debug
 * info.
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class ClassOptimizer extends ClassRemapper {

  @SuppressWarnings("unused")
  private String pkgName;
  String clsName;

  boolean isInterface = false;
  boolean hasClinitMethod = false;
  List<String> syntheticClassFields = new ArrayList<String>();

  public ClassOptimizer(final ClassVisitor cv, final Remapper remapper) {
    super(Opcodes.ASM5, cv, remapper);
  }

  FieldVisitor syntheticFieldVisitor(final int access, final String name, final String desc) {
    return super.visitField(access, name, desc, null, null);
  }

  // ------------------------------------------------------------------------
  // Overridden methods
  // ------------------------------------------------------------------------

  @Override
  public void visit(final int version, final int access, final String name, final String signature, final String superName,
      final String[] interfaces) {
    super.visit(Opcodes.V1_8, access, name, null, superName, interfaces);
    int index = name.lastIndexOf('/');
    if (index > 0) {
      pkgName = name.substring(0, index);
    } else {
      pkgName = "";
    }
    clsName = name;
    isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
  }

  @Override
  public void visitSource(final String source, final String debug) {
    // remove debug info
  }

  @Override
  public void visitOuterClass(final String owner, final String name, final String desc) {
    // remove debug info
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    // remove annotations
    return null;
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    // remove annotations
    return null;
  }

  @Override
  public void visitAttribute(final Attribute attr) {
    // remove non standard attributes
  }

  @Override
  public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
    // remove debug info
  }

  @Override
  public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
    String s = remapper.mapFieldName(className, name, desc);
    if ("-".equals(s)) {
      return null;
    }
    if ((access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0) {
      if ((access & Opcodes.ACC_FINAL) != 0 && (access & Opcodes.ACC_STATIC) != 0 && desc.length() == 1) {
      }
      super.visitField(access, name, desc, null, value);
    } else {
      if (!s.equals(name)) {
        throw new RuntimeException("The public or protected field " + className + '.' + name + " must not be renamed.");
      }
      super.visitField(access, name, desc, null, value);
    }
    return null; // remove debug info
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
    String s = remapper.mapMethodName(className, name, desc);

    if ("-".equals(s)) {
      return null;
    }
    if (name.equals("<clinit>") && !isInterface) {
      hasClinitMethod = true;
      MethodVisitor mv =

          super.visitMethod(access, name, desc, null, exceptions);
      return new MethodVisitor(Opcodes.ASM5, mv) {
        @Override
        public void visitCode() {
          super.visitCode();
          mv.visitMethodInsn(Opcodes.INVOKESTATIC, clsName, "_clinit_", "()V", false);
        }
      };
    }

    if ((access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0) {
      return super.visitMethod(access, name, desc, null, exceptions);
    } else {
      if (!s.equals(name)) {
        throw new RuntimeException("The public or protected method " + className + '.' + name + desc + " must not be renamed.");
      }
      return super.visitMethod(access, name, desc, null, exceptions);
    }
  }

  @Override
  protected MethodVisitor createMethodRemapper(MethodVisitor mv) {
    return new MethodOptimizer(this, mv, remapper);
  }

  @Override
  public void visitEnd() {
    if (syntheticClassFields.isEmpty()) {
      if (hasClinitMethod) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, "_clinit_", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
      }
    } else {
      MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, "class$", "(Ljava/lang/String;)Ljava/lang/Class;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      Label l1 = new Label();
      Label l2 = new Label();
      mv.visitTryCatchBlock(l0, l1, l2, "java/lang/ClassNotFoundException");
      mv.visitLabel(l0);
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
      mv.visitLabel(l1);
      mv.visitInsn(Opcodes.ARETURN);
      mv.visitLabel(l2);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/ClassNotFoundException", "getMessage", "()Ljava/lang/String;", false);
      mv.visitVarInsn(Opcodes.ASTORE, 1);
      mv.visitTypeInsn(Opcodes.NEW, "java/lang/NoClassDefFoundError");
      mv.visitInsn(Opcodes.DUP);
      mv.visitVarInsn(Opcodes.ALOAD, 1);
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V", false);
      mv.visitInsn(Opcodes.ATHROW);
      mv.visitMaxs(3, 2);
      mv.visitEnd();

      if (hasClinitMethod) {
        mv = cv.visitMethod(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE, "_clinit_", "()V", null, null);
      } else {
        mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
      }
      for (String ldcName : syntheticClassFields) {
        String fieldName = "class$" + ldcName.replace('/', '$');
        mv.visitLdcInsn(ldcName.replace('/', '.'));
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, clsName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, clsName, fieldName, "Ljava/lang/Class;");
      }
      mv.visitInsn(Opcodes.RETURN);
      mv.visitMaxs(1, 0);
      mv.visitEnd();
    }
    super.visitEnd();
  }
}