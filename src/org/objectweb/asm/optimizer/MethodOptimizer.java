package org.objectweb.asm.optimizer;

import java.util.HashMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.MethodRemapper;

/**
 * A {@link MethodVisitor} that renames fields and methods, and removes debug
 * info.
 * 
 * @author Eugene Kuleshov
 */
public class MethodOptimizer extends MethodRemapper implements Opcodes {

    private final ClassOptimizer classOptimizer;

    public MethodOptimizer(ClassOptimizer classOptimizer, MethodVisitor mv,
            Remapper remapper) {
        super(Opcodes.ASM5, mv, remapper);
        this.classOptimizer = classOptimizer;
    }

    // ------------------------------------------------------------------------
    // Overridden methods
    // ------------------------------------------------------------------------

    @Override
    public void visitParameter(String name, int access) {
        // remove parameter info
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        // remove annotations
        return null;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // remove annotations
        return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        return null;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter,
            final String desc, final boolean visible) {
        // remove annotations
        return null;
    }

    @Override
    public void visitLocalVariable(final String name, final String desc,
            final String signature, final Label start, final Label end,
            final int index) {
        // remove debug info
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        // remove debug info
    }

    @Override
    public void visitFrame(int type, int local, Object[] local2, int stack,
            Object[] stack2) {
        // remove frame info
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // remove non standard attributes
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if (!(cst instanceof Type)) {
            super.visitLdcInsn(cst);
            return;
        }

        // transform Foo.class for 1.2 compatibility
        String ldcName = ((Type) cst).getInternalName();
        String fieldName = "class$" + ldcName.replace('/', '$');
        if (!classOptimizer.syntheticClassFields.contains(ldcName)) {
            classOptimizer.syntheticClassFields.add(ldcName);
            FieldVisitor fv = classOptimizer.syntheticFieldVisitor(ACC_STATIC
                    | ACC_SYNTHETIC, fieldName, "Ljava/lang/Class;");
            fv.visitEnd();
        }

        String clsName = classOptimizer.clsName;
        mv.visitFieldInsn(GETSTATIC, clsName, fieldName, "Ljava/lang/Class;");
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        // rewrite boxing method call to use constructor to keep 1.3/1.4
        // compatibility
        String[] constructorParams;
        if (opcode == INVOKESTATIC && name.equals("valueOf")
                && (constructorParams = BOXING_MAP.get(owner + desc)) != null) {
            String type = constructorParams[0];
            String initDesc = constructorParams[1];
            super.visitTypeInsn(NEW, type);
            super.visitInsn(DUP);
            super.visitInsn((initDesc == "(J)V" || initDesc == "(D)V") ? DUP2_X2
                    : DUP2_X1);
            super.visitInsn(POP2);
            super.visitMethodInsn(INVOKESPECIAL, type, "<init>", initDesc,
                    false);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    private static final HashMap<String, String[]> BOXING_MAP;
    static {
        String[][] boxingNames = {
                // Boolean.valueOf is 1.4 and is used by the xml package, so no
                // rewrite
                { "java/lang/Byte", "(B)V" }, { "java/lang/Short", "(S)V" },
                { "java/lang/Character", "(C)V" },
                { "java/lang/Integer", "(I)V" }, { "java/lang/Long", "(J)V" },
                { "java/lang/Float", "(F)V" }, { "java/lang/Double", "(D)V" }, };
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        for (String[] boxingName : boxingNames) {
            String wrapper = boxingName[0];
            String desc = boxingName[1];
            String boxingMethod = wrapper + '(' + desc.charAt(1) + ")L"
                    + wrapper + ';';
            map.put(boxingMethod, boxingName);
        }
        BOXING_MAP = map;
    }
}