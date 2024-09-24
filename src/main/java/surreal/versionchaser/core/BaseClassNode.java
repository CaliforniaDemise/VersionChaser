package surreal.versionchaser.core;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

public abstract class BaseClassNode extends ClassNode implements BaseTransformer {

    public BaseClassNode() {
        super(ASM5);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationNode an = new AnnotationReplacement(this, desc);
        if (visible) {
            if (this.visibleAnnotations == null) {
                this.visibleAnnotations = new ArrayList<>(1);
            }

            this.visibleAnnotations.add(an);
        } else {
            if (this.invisibleAnnotations == null) {
                this.invisibleAnnotations = new ArrayList<>(1);
            }

            this.invisibleAnnotations.add(an);
        }
        an.desc = this.getDesc(an, desc);
        return an;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldNode field = (FieldNode) super.visitField(access, name, desc, signature, value);
        field.desc = this.getDesc(field, desc);
        return field;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodNode method = (MethodNode) super.visitMethod(access, name, desc, signature, exceptions);
        method.desc = this.getDesc(method, name);
        return method;
    }

    // Default getters
    protected String getDesc(String desc) { return desc; }

    protected String getDesc(AnnotationNode node, String desc) { return this.getDesc(desc); }
    protected String getDesc(MethodNode node, String desc) { return this.getDesc(desc); }

    // Annotation
    protected String getDesc(FieldNode node, String desc) { return this.getDesc(desc); }
    protected String getName(AnnotationNode node, String name, Object value) { return name; }
    protected Object getValue(AnnotationNode node, String name, Object value) { return value; }

    public static class AnnotationReplacement extends AnnotationNode {

        protected final BaseClassNode node;

        public AnnotationReplacement(BaseClassNode node, String desc) {
            super(ASM5, desc);
            this.node = node;
        }

        @Override
        public void visit(String name, Object value) {
            super.visit(this.node.getName(this, name, value), this.node.getValue(this, name, value));
        }
    }
}
