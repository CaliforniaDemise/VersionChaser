package surreal.versionchaser.asm;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import surreal.versionchaser.core.visitors.ClassVisitor1710;
import surreal.versionchaser.core.visitors.ClassVisitor189;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Patcher {

    public static void patch(File file, ZipFile zipFile, String mcVersion) {
        ClassNode cls = getVisitor(mcVersion);
        if (cls == null) return;

        boolean deobf = FMLLaunchHandler.isDeobfuscatedEnvironment();

        List<ZipEntry> entries = zipFile.stream().filter(entry -> !entry.isDirectory()).collect(Collectors.toList());

        String name = file.getAbsolutePath();

        try {
            JarOutputStream jos;
            if (name.endsWith(".disabled")) {
                jos = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(name.subSequence(0, name.length() - 13) + "-chased.jar"))));
            }
            else jos = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(name.subSequence(0, name.length() - 4) + "-chased.jar"))));

            for (ZipEntry entry : entries) {
                InputStream stream = zipFile.getInputStream(entry);
                if (entry.getName().endsWith(".class")) {
                    ClassReader reader = new ClassReader(stream);
                    reader.accept(cls, 0);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                    cls.accept(writer);

                    if (deobf) {
                        File classOut = new File(file.getParentFile().getParentFile(), "classOutputs/" + entry.getName());
                        Helper.writeClassToFile(classOut, writer);
                    }

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

                stream.close();
            }

            jos.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ClassNode getVisitor(String mcVersion) {
        switch (mcVersion) {
            case "1.7.10": return new ClassVisitor1710();
            case "1.8.9": return new ClassVisitor189();
        }

        return null;
    }
}
