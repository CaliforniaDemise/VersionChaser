package surreal.versionchaser.core;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class BaseClassNode extends ClassNode implements Opcodes {

    public BaseClassNode() { super(ASM5); }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationNode an = new AnnotationReplacement(this, this.getAnnDesc(desc));
        if (visible) {
            if (this.visibleAnnotations == null) this.visibleAnnotations = new ArrayList<>(1);
            this.visibleAnnotations.add(an);
        } else {
            if (this.invisibleAnnotations == null) this.invisibleAnnotations = new ArrayList<>(1);
            this.invisibleAnnotations.add(an);
        }
        return an;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldReplacement fn = new FieldReplacement(this, access, name, this.getFieldDesc(desc), signature, value);
        this.fields.add(fn);
        return fn;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodReplacement mn = new MethodReplacement(this, access, name, this.getMethodDesc(desc), signature, exceptions);
        this.methods.add(mn);
        return mn;
    }

    protected abstract String getHookClass();

    // Default getters
    protected String getOwner(String owner) { return owner; }
    protected String getDesc(String desc) { return desc; }

    // Annotations
    protected String getAnnDesc(String desc) { return this.getDesc(desc); }
    protected String getAnnName(String name, Object value) { return name; }
    protected Object getAnnValue(String name, Object value) { return value; }

    // Methods
    protected String getMethodOwner(String owner) { return this.getOwner(owner); }
    protected String getMethodDesc(String desc) { return this.getDesc(desc); }

    // Fields
    protected String getFieldDesc(String desc) { return this.getDesc(desc); }

    // Insns
    @Nullable
    protected String getHookMethod(String owner, String name, String desc) { return null; }

    public static class AnnotationReplacement extends AnnotationNode {

        protected final BaseClassNode node;

        public AnnotationReplacement(BaseClassNode node, String desc) {
            super(ASM5, desc);
            this.node = node;
        }

        @Override
        public void visit(String name, Object value) {
            super.visit(this.node.getAnnName(name, value), this.node.getAnnValue(name, value));
        }
    }

    public static class FieldReplacement extends FieldNode {

        protected final BaseClassNode node;

        public FieldReplacement(BaseClassNode node, int access, String name, String desc, String signature, Object value) {
            super(ASM5, access, name, desc, signature, value);
            this.node = node;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return super.visitAnnotation(this.node.getAnnDesc(desc), visible);
        }
    }

    public static class MethodReplacement extends MethodNode {

        protected final BaseClassNode node;

        public MethodReplacement(BaseClassNode node, int access, String name, String desc, String signature, String[] exceptions) {
            super(ASM5, access, name, desc, signature, exceptions);
            this.node = node;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return super.visitAnnotation(this.node.getAnnDesc(desc), visible);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            String hook = this.node.getHookMethod(owner, name, desc);
            if (hook != null) {
                owner = this.node.getHookClass();
                name = hook;
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
            super.visitMethodInsn(opcode, this.node.getMethodOwner(owner), name, this.node.getMethodDesc(desc), itf);
        }
    }
}
