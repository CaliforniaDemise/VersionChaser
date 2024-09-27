package surreal.versionchaser.core;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public abstract class BaseClassNode extends ClassNode implements Opcodes {

    public BaseClassNode() { super(ASM5); }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        superName = this.getOwner(superName);
        if (interfaces.length > 0) {
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = this.getOwner(interfaces[i]);
            }
        }
        signature = signature == null ? null : this.getDesc(signature);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(this.getOwner(owner), name, this.getDesc(desc));
    }

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
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, this.getDesc(desc), visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldReplacement fn = new FieldReplacement(this, access, name, this.getFieldDesc(desc), signature == null ? null : this.getDesc(signature), value);
        this.fields.add(fn);
        return fn;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (exceptions != null && exceptions.length > 0) {
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = this.getDesc(exceptions[i]);
            }
        }
        MethodReplacement mn = new MethodReplacement(this, access, name, this.getMethodDesc(name, desc), signature == null ? null : this.getMethodDesc(name, signature), exceptions);
        this.methods.add(mn);
        return mn;
    }

    protected abstract String getHookClass();

    // Default getters
    protected String getOwner(String owner) { return owner; }
    protected String getDesc(String desc) { return desc; }

    // Annotations
    protected String getAnnDesc(String desc) { return this.getDesc(desc); }
    protected String getAnnName(AnnotationNode an, String name, Object value) { return name; }
    protected String getAnnDesc(AnnotationNode an, String name, String desc, Object value) { return this.getDesc(desc); }
    protected Object getAnnValue(AnnotationNode an, String name, Object value) { return value; }

    // Methods
    protected String getMethodOwner(String owner) { return this.getOwner(owner); }
    protected String getMethodName(String name) { return name; }
    protected String getMethodDesc(String name, String desc) { return this.getDesc(desc); }

    // Fields
    protected String getFieldOwner(String owner) { return this.getOwner(owner); }
    protected String getFieldName(String name) { return name; }
    protected String getFieldDesc(String desc) { return this.getDesc(desc); }

    // Insns
    protected boolean replaceMethodInsn(MethodNode method, int opcode, String owner, String name, String desc) {
        return false;
    }

    protected boolean replaceFieldInsn(MethodNode method, int opcode, String owner, String name, String desc) {
        return false;
    }

    protected void onMethodEnd(MethodNode method) {}

    public static class AnnotationReplacement extends AnnotationNode {

        protected final BaseClassNode node;

        public AnnotationReplacement(BaseClassNode node, String desc) {
            super(ASM5, desc);
            this.node = node;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            if (values == null) {
                values = new ArrayList<>(this.desc != null ? 2 : 1);
            }
            if (this.desc != null) {
                values.add(name);
            }
            AnnotationReplacement an = new AnnotationReplacement(this.node, desc);
            values.add(an);
            return an;
        }

        @Override
        public void visit(String name, Object value) {
            super.visit(this.node.getAnnName(this, name, value), this.node.getAnnValue(this, name, value));
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            super.visitEnum(this.node.getAnnName(this, name, value), this.node.getAnnDesc(this, name, desc, value), value);
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
            AnnotationNode an = new AnnotationReplacement(this.node, this.node.getAnnDesc(desc));
            if (visible) {
                if (this.visibleAnnotations == null) this.visibleAnnotations = new ArrayList<>(1);
                this.visibleAnnotations.add(an);
            } else {
                if (this.invisibleAnnotations == null) this.invisibleAnnotations = new ArrayList<>(1);
                this.invisibleAnnotations.add(an);
            }
            return an;
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
            AnnotationNode an = new AnnotationReplacement(this.node, this.node.getAnnDesc(desc));
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
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (this.node.replaceMethodInsn(this, opcode, owner, name, desc)) return;
            super.visitMethodInsn(opcode, this.node.getMethodOwner(owner), this.node.getMethodName(name), this.node.getMethodDesc(name, desc), itf);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (this.node.replaceFieldInsn(this, opcode, owner, name, desc)) return;
            super.visitFieldInsn(opcode, this.node.getFieldOwner(owner), this.node.getFieldName(name), this.node.getFieldDesc(desc));
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, this.node.getOwner(type));
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            if (local != null) {
                for (int i = 0; i < nLocal; i++) {
                    if (local[i] instanceof String) {
                        String l = (String) local[i];
                        if (l.charAt(0) == '[') local[i] = this.node.getDesc(l);
                        else local[i] = this.node.getOwner(l);
                    }
                }
            }
            if (stack != null) {
                for (int i = 0; i < nStack; i++) {
                    if (stack[i] instanceof String) {
                        String s = (String) stack[i];
                        if (s.charAt(0) == '[') stack[i] = this.node.getDesc(s);
                        else stack[i] = this.node.getOwner(s);
                    }
                }
            }
            super.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public void visitEnd() {
            this.node.onMethodEnd(this);
            super.visitEnd();
        }
    }

    // Helpers
    protected MethodInsnNode methodHook(String name, String desc) {
        return new MethodInsnNode(INVOKESTATIC, this.getHookClass(), name, desc, false);
    }

    protected FieldInsnNode fieldHook(String name, String desc) {
        return new FieldInsnNode(GETSTATIC, this.getHookClass(), name, desc);
    }

    protected void visitMethodHook(MethodVisitor method, String name, String desc) {
        method.visitMethodInsn(INVOKESTATIC, this.getHookClass(), name, desc, false);
    }

    protected void visitMethodHook(MethodVisitor method, int oldOpcode, String oldOwner, String name, String desc) {
        if (oldOpcode == INVOKESTATIC) visitMethodHook(method, name, desc);
        else {
            StringBuilder descBuilder = new StringBuilder("(");
            descBuilder.append('L').append(oldOwner).append(';');
            descBuilder.append(desc.substring(1));
            visitMethodHook(method, name, descBuilder.toString());
        }
    }

    protected void visitFieldHook(MethodVisitor method, String name, String desc) {
        method.visitFieldInsn(GETSTATIC, this.getHookClass(), name, desc);
    }

    protected static String getName(String mcp, String srg) {
        return FMLLaunchHandler.isDeobfuscatedEnvironment() ? mcp : srg;
    }

    protected static boolean isSuper(ClassNode cls, String superName) {
        if (cls.superName.equals("java/lang/Object")) return false;
        else if (cls.superName.equals(superName)) return true;
        try {
            Class<?> clazz = Class.forName(cls.superName.replace('/', '.'));
            return isSuper(clazz, superName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isSuper(Class<?> cls, String superName) {
        Class<?> superCls = cls.getSuperclass();
        if (superCls.getName().equals("java.lang.Object")) return false;
        if (superCls.getName().replace('.', '/').equals(superName)) return true;
        else return isSuper(superCls, superName);
    }
}
