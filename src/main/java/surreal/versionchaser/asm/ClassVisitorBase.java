package surreal.versionchaser.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import static org.objectweb.asm.Opcodes.*;

public class ClassVisitorBase extends ClassVisitor {

    public ClassVisitorBase() {
        super(ASM5);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    protected static class AnnotationVisitorBase extends AnnotationVisitor {

        protected final ClassNode cls;

        public AnnotationVisitorBase(ClassNode cls) {
            super(ASM5);
            this.cls = cls;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            if (name.endsWith("fml/common/Mod;")) {

            }
        }
    }
}
