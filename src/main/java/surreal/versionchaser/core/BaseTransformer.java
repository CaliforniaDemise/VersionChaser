package surreal.versionchaser.core;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public abstract class BaseTransformer implements Opcodes {

    protected static <T extends ClassVisitor> T read(byte[] basicClass, T visitor) {
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(visitor, 0);
        return visitor;
    }

    protected static ClassNode read(byte[] basicClass) {
        return read(basicClass, new ClassNode());
    }

    protected static byte[] write(ClassNode cls, int options) {
        ClassWriter writer = new ClassWriter(options);
        cls.accept(writer);
        return writer.toByteArray();
    }

    protected static byte[] write(ClassNode cls) {
        return write(cls, ClassWriter.COMPUTE_MAXS);
    }

    protected static void hook(MethodVisitor mv, String name, String desc) {
        mv.visitMethodInsn(INVOKESTATIC, "surreal/versionchaser/core/VCHooks", name, desc, false);
    }

    protected static MethodInsnNode hook(String name, String desc) {
        return new MethodInsnNode(INVOKESTATIC, "surreal/versionchaser/core/VCHooks", name, desc, false);
    }

    protected static String getName(String mcp, String srg) {
        return FMLLaunchHandler.isDeobfuscatedEnvironment() ? mcp : srg;
    }

    protected static void writeClass(ClassNode cls) {
        File file = new File("classOutputs", cls.name + ".class");
        file.getParentFile().mkdirs();
        try {
            OutputStream stream = Files.newOutputStream(file.toPath());
            stream.write(write(cls));
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
