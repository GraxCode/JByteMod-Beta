package org.objectweb.asm.optimizer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

/**
 * An {@link MethodVisitor} that collects the {@link Constant}s of the methods
 * it visits.
 * 
 * @author Eric Bruneton
 */
public class MethodConstantsCollector extends MethodVisitor {

    private final ConstantPool cp;

    public MethodConstantsCollector(final MethodVisitor mv,
            final ConstantPool cp) {
        super(Opcodes.ASM5, mv);
        this.cp = cp;
    }

    @Override
    public void visitParameter(String name, int access) {
        cp.newUTF8("MethodParameters");
        if (name != null) {
            cp.newUTF8(name);
        }
        mv.visitParameter(name, access);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        cp.newUTF8("AnnotationDefault");
        return new AnnotationConstantsCollector(mv.visitAnnotationDefault(), cp);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc,
            final boolean visible) {
        cp.newUTF8(desc);
        if (visible) {
            cp.newUTF8("RuntimeVisibleAnnotations");
        } else {
            cp.newUTF8("RuntimeInvisibleAnnotations");
        }
        return new AnnotationConstantsCollector(mv.visitAnnotation(desc,
                visible), cp);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        cp.newUTF8(desc);
        if (visible) {
            cp.newUTF8("RuntimeVisibleTypeAnnotations");
        } else {
            cp.newUTF8("RuntimeInvisibleTypeAnnotations");
        }
        return new AnnotationConstantsCollector(mv.visitAnnotation(desc,
                visible), cp);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter,
            final String desc, final boolean visible) {
        cp.newUTF8(desc);
        if (visible) {
            cp.newUTF8("RuntimeVisibleParameterAnnotations");
        } else {
            cp.newUTF8("RuntimeInvisibleParameterAnnotations");
        }
        return new AnnotationConstantsCollector(mv.visitParameterAnnotation(
                parameter, desc, visible), cp);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        cp.newClass(type);
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        cp.newField(owner, name, desc);
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc, final boolean itf) {
        cp.newMethod(owner, name, desc, itf);
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        cp.newInvokeDynamic(name, desc, bsm, bsmArgs);
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        cp.newConst(cst);
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        cp.newClass(desc);
        mv.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        cp.newUTF8(desc);
        if (visible) {
            cp.newUTF8("RuntimeVisibleTypeAnnotations");
        } else {
            cp.newUTF8("RuntimeInvisibleTypeAnnotations");
        }
        return new AnnotationConstantsCollector(mv.visitInsnAnnotation(typeRef,
                typePath, desc, visible), cp);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end,
            final Label handler, final String type) {
        if (type != null) {
            cp.newClass(type);
        }
        mv.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        cp.newUTF8(desc);
        if (visible) {
            cp.newUTF8("RuntimeVisibleTypeAnnotations");
        } else {
            cp.newUTF8("RuntimeInvisibleTypeAnnotations");
        }
        return new AnnotationConstantsCollector(mv.visitTryCatchAnnotation(
                typeRef, typePath, desc, visible), cp);
    }

    @Override
    public void visitLocalVariable(final String name, final String desc,
            final String signature, final Label start, final Label end,
            final int index) {
        if (signature != null) {
            cp.newUTF8("LocalVariableTypeTable");
            cp.newUTF8(name);
            cp.newUTF8(signature);
        }
        cp.newUTF8("LocalVariableTable");
        cp.newUTF8(name);
        cp.newUTF8(desc);
        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
            TypePath typePath, Label[] start, Label[] end, int[] index,
            String desc, boolean visible) {
        cp.newUTF8(desc);
        if (visible) {
            cp.newUTF8("RuntimeVisibleTypeAnnotations");
        } else {
            cp.newUTF8("RuntimeInvisibleTypeAnnotations");
        }
        return new AnnotationConstantsCollector(
                mv.visitLocalVariableAnnotation(typeRef, typePath, start, end,
                        index, desc, visible), cp);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        cp.newUTF8("LineNumberTable");
        mv.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        cp.newUTF8("Code");
        mv.visitMaxs(maxStack, maxLocals);
    }
}