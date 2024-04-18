package surreal.versionchaser.asm;

import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class Helper {

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
