package surreal.versionchaser.core.v1710.asm;

import org.objectweb.asm.AnnotationVisitor;
import surreal.versionchaser.asm.ClassVisitorBase;

import static org.objectweb.asm.Opcodes.*;

public class ClassVisitor1710 extends ClassVisitorBase {

    public ClassVisitor1710() {

    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }
}
