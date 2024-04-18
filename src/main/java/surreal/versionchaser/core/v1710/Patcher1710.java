package surreal.versionchaser.core.v1710;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import surreal.versionchaser.asm.Helper;
import surreal.versionchaser.core.v1710.asm.ClassVisitor1710;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Patcher1710 {

    public static void patch(File file, ZipFile zipFile) {
        List<ZipEntry> entries = zipFile.stream().filter(entry -> !entry.isDirectory()).collect(Collectors.toList());

        String name = file.getAbsolutePath();

        try {
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(name.subSequence(0, name.length() - 4) + "-chased.jar"))));

            for (ZipEntry entry : entries) {
                InputStream stream = zipFile.getInputStream(entry);

                if (entry.getName().endsWith(".class")) {
                    ClassReader reader = new ClassReader(stream);

                    ClassVisitor1710 visitor = new ClassVisitor1710();
                    reader.accept(visitor, 0);

                    ClassWriter writer = new ClassWriter(0);
                    visitor.accept(writer);

                    File classOut = new File(file.getParentFile().getParentFile(), "classOutputs/" + entry.getName());
                    Helper.writeClassToFile(classOut, writer);

                    jos.putNextEntry(new JarEntry(entry.getName()));
                    jos.write(writer.toByteArray());
                    jos.closeEntry();
                }
                else {
                    jos.putNextEntry(new JarEntry(entry.getName()));
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    int i;
                    while ((i = bis.read()) != -1) {
                        jos.write(i);
                    }
                    jos.closeEntry();
                }
            }

            jos.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
