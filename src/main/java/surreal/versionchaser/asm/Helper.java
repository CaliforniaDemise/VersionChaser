package surreal.versionchaser.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class Helper implements Opcodes {

    public static int getReturn(MethodNode method) {
        char c = method.desc.charAt(method.desc.length() - 1);
        switch (c) {
            case ';': return ARETURN;
            case 'B':
            case 'S':
            case 'Z':
            case 'I': return IRETURN;
            case 'J': return LRETURN;
            case 'F': return FRETURN;
            case 'D': return DRETURN;
            default: return RETURN;
        }
    }

    public static void writeClassToFile(File file, ClassWriter writer) {
        file.getParentFile().mkdirs();
        try {
            OutputStream stream = Files.newOutputStream(file.toPath());
            stream.write(writer.toByteArray());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
