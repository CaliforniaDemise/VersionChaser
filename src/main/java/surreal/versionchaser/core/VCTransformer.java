package surreal.versionchaser.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

public class VCTransformer extends BaseTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        switch (transformedName) {
            case "net.minecraftforge.fml.common.registry.GameRegistry": return transformGameRegistry(basicClass);
        }
        return basicClass;
    }

    public static byte[] transformGameRegistry(byte[] basicClass) {
        ClassNode cls = read(basicClass);
        { // addShapelessRecipe
            MethodVisitor m = cls.visitMethod(ACC_PUBLIC | ACC_STATIC, "addShapelessRecipe", "(Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V", null, null);
            m.visitVarInsn(ALOAD, 0);
            m.visitVarInsn(ALOAD, 1);
            hook(m, "GameRegistry$addShapelessRecipe1710", "(Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V");
            m.visitInsn(RETURN);
        }
        return write(cls);
    }
}
