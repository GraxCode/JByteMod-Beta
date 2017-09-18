package org.objectweb.asm.optimizer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

/**
 * A {@link FieldVisitor} that collects the {@link Constant}s of the fields it
 * visits.
 * 
 * @author Eric Bruneton
 */
public class FieldConstantsCollector extends FieldVisitor {

    private final ConstantPool cp;

    public FieldConstantsCollector(final FieldVisitor fv, final ConstantPool cp) {
        super(Opcodes.ASM5, fv);
        this.cp = cp;
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
        return new AnnotationConstantsCollector(fv.visitAnnotation(desc,
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
        return new AnnotationConstantsCollector(fv.visitAnnotation(desc,
                visible), cp);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        // can do nothing
        fv.visitAttribute(attr);
    }

    @Override
    public void visitEnd() {
        fv.visitEnd();
    }
}
