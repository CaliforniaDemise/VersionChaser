package surreal.versionchaser.asm;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ClassVisitorBase extends ClassNode {

    private final Map<String, String> classMappings = new Object2ObjectOpenHashMap<>();

    public ClassVisitorBase() {
        super(ASM5);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String superNameMap = classMappings.get(superName);
        if (superNameMap != null) superName = superNameMap;
        else superName = replaceHardcoded(superName);
        for (int i = 0; i < interfaces.length; i++) {
            String interfaceMap = classMappings.get(interfaces[i]);
            if (interfaceMap != null) interfaces[i] = interfaceMap;
            else interfaces[i] = replaceHardcoded(interfaces[i]);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationNode an = new AnnotationVisitorBase(replaceDesc(desc));
        if (visible) {
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<>(1);
            }
            visibleAnnotations.add(an);
        } else {
            if (invisibleAnnotations == null) {
                invisibleAnnotations = new ArrayList<>(1);
            }
            invisibleAnnotations.add(an);
        }
        return an;
    }

    protected void addClass(String original, String replacement) {
        classMappings.put(original, replacement);
    }

    private String replaceDesc(String original) {
        String replacement = classMappings.get(original.substring(1, original.length() - 1));
        if (replacement != null) original = "L" + replacement + ";";
        else original = replaceHardcoded(original);
        return original;
    }

    private String replaceHardcoded(String original) {

        // Replace cpw to minecraftforge
        String withL = "Lcpw/mods/fml";
        String normal = "cpw/mods/fml";
        if (original.startsWith(withL)) original = "Lnet/minecraftforge/fml" + original.substring(withL.length());
        else if (original.startsWith(normal)) original = "net/minecraftforge/fml" + original.substring(normal.length());

        return original;
    }

    private static class AnnotationVisitorBase extends AnnotationNode {

        public AnnotationVisitorBase(String desc) {
            super(ASM5, desc);
        }

        @Override
        public void visit(String name, Object value) {
            if (desc.equals("Lnet/minecraftforge/fml/common/Mod;")) {
                if (name.equals("dependencies")) return;
                if (name.equals("modid")) value = ((String) value).toLowerCase(Locale.US);
            }
            super.visit(name, value);
        }
    }
}
