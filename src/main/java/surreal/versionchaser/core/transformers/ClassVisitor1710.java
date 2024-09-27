package surreal.versionchaser.core.transformers;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import surreal.versionchaser.core.BaseClassNode;

public class ClassVisitor1710 extends BaseClassNode {

    public ClassVisitor1710() { super(); }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    protected String getHookClass() {
        return "surreal/versionchaser/core/hooks/Hooks1710";
    }

    @Override
    protected Object getAnnValue(AnnotationNode an, String name, Object value) {
        Object obj = super.getAnnValue(an, name, value);
        if (name.equals("acceptedMinecraftVersions")) obj = "[1.12.2]";
        return obj;
    }

    @Override
    protected String getOwner(String owner) {
        owner = super.getOwner(owner);
        {
            String clientMP = "EntityClientPlayerMP";
            if (owner.endsWith(clientMP)) {
                return owner.replace(clientMP, "EntityPlayerSP");
            }
        }
        {
            String cpw = "cpw/mods";
            if (owner.startsWith(cpw)) {
                return owner.replace(cpw, "net/minecraftforge");
            }
        }
        {
            String iicon = "net/minecraft/util/IIcon";
            if (owner.endsWith(iicon)) {
                return owner.replace(iicon, "java/lang/String");
            }
        }
        {
            String iiconRegister = "net/minecraft/client/renderer/texture/IIconRegister";
            if (owner.equals(iiconRegister)) {
                return "java/lang/Object";
            }
        }
        {
            String biomeGenBase = "BiomeGenBase";
            if (owner.contains(biomeGenBase)) {
                return owner.replace(biomeGenBase, "Biome");
            }
        }
        {
            String item = "net/minecraft/item/Item";
            if (owner.equals(item)) {
                return "surreal/versionchaser/core/hooks/Hooks1710$ItemWithIcon";
            }
        }
        {
            String chunkCoordinates = "net/minecraft/util/ChunkCoordinates";
            if (owner.equals(chunkCoordinates)) {
                return "net/minecraft/util/math/BlockPos";
            }
        }
        {
            String mathHelper = "net/minecraft/util/MathHelper";
            if (owner.equals(mathHelper)) {
                return "net/minecraft/util/math/MathHelper";
            }
        }
        return owner;
    }

    @Override
    protected String getDesc(String desc) {
        desc = super.getDesc(desc);
        {
            String clientMP = "EntityClientPlayerMP;";
            if (desc.contains(clientMP)) {
                return desc.replace(clientMP, "EntityPlayerSP;");
            }
        }
        {
            String iicon = "net/minecraft/util/IIcon;";
            if (desc.contains(iicon)) {
                return desc.replace(iicon, "java/lang/String;");
            }
        }
        {
            String iiconRegister = "net/minecraft/client/renderer/texture/IIconRegister";
            if (desc.contains(iiconRegister)) {
                return desc.replace(iiconRegister, "java/lang/Object");
            }
        }
        {
            String cpw = "Lcpw/mods";
            if (desc.contains(cpw)) {
                return desc.replace(cpw, "Lnet/minecraftforge");
            }
        }
        {
            String biomeGenBase = "BiomeGenBase";
            if (desc.contains(biomeGenBase)) {
                return desc.replace(biomeGenBase, "Biome");
            }
        }
        {
            String chunkCoordinates = "net/minecraft/util/ChunkCoordinates";
            if (desc.contains(chunkCoordinates)) {
                return desc.replace(chunkCoordinates, "net/minecraft/util/math/BlockPos");
            }
        }
        {
            String mathHelper = "net/minecraft/util/MathHelper";
            if (desc.contains(mathHelper)) {
                return desc.replace(mathHelper, "net/minecraft/util/math/MathHelper");
            }
        }
        return desc;
    }

    @Override
    protected String getMethodName(String name) {
        name = super.getMethodName(name);
        if (name.equals("func_70694_bm")) name = getName("getHeldItemMainhand", "func_184614_ca");
        return name;
    }

    @Override
    protected boolean replaceMethodInsn(MethodNode method, int opcode, String owner, String name, String desc) {
        if (owner.endsWith("GameRegistry"))  {
            switch (name) {
                case "addShapelessRecipe": this.visitMethodHook(method, "GameRegistry$addShapelessRecipe", desc); return true;
                case "addRecipe": this.visitMethodHook(method, "GameRegistry$addRecipe", desc); return true;
                case "registerItem": this.visitMethodHook(method, "GameRegistry$registerItem", desc); return true;
            }
        }
        if (owner.endsWith("ShapedOreRecipe")) {
            AbstractInsnNode n = method.instructions.getLast();
            while (!(n instanceof TypeInsnNode) || !((TypeInsnNode) n).desc.endsWith("net/minecraftforge/oredict/ShapedOreRecipe")) n = n.getPrevious();
            n = n.getNext();
            method.instructions.insert(n, new InsnNode(ACONST_NULL));
            method.instructions.add(new MethodInsnNode(opcode, owner, name, "(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/Item;[Ljava/lang/Object;)V", false));
            return true;
        }
        switch (name) {
            case "func_150565_n": this.visitMethodHook(method, opcode, owner, "BiomeGenBase$getBiomeArray", desc); return true;
            case "func_147674_a": this.visitMethodHook(method, opcode, owner, "PositionedSoundRecord$getMasterRecord", desc); return true;
            case "func_72807_a": this.visitMethodHook(method, opcode, owner, "World$getBiomeGenForCoords", desc); return true;
            case "func_85187_a": this.visitMethodHook(method, opcode, owner, "FontRenderer$drawString", desc); return true;
            case "func_94245_a": {
                method.visitVarInsn(ALOAD, 0);
                this.visitMethodHook(method, "IIconRegister$registerIcon", "(Ljava/lang/Object;Ljava/lang/String;Lnet/minecraft/item/Item;)Ljava/lang/String;");
                return true;
            }
            case "func_72861_E": method.visitMethodInsn(INVOKEVIRTUAL, owner, getName("getSpawnPoint", "func_175694_M"), "()Lnet/minecraft/util/math/BlockPos;", false); return true;
        }
        return super.replaceMethodInsn(method, opcode, owner, name, desc);
    }

    @Override
    protected boolean replaceFieldInsn(MethodNode method, int opcode, String owner, String name, String desc) {
        if (owner.equals("net/minecraftforge/common/config/Property") && name.equals("comment")) {
            if (opcode == GETFIELD) this.visitMethodHook(method, "Property$getComment", "(Lnet/minecraftforge/common/config/Property;)Ljava/lang/String;");
            else if (opcode == PUTFIELD) this.visitMethodHook(method, "Property$changeComment", "(Lnet/minecraftforge/common/config/Property;Ljava/lang/String;)V");
            return true;
        }
        if (owner.equals("net/minecraft/world/biome/BiomeGenBase")) {
            String biome = "net/minecraft/world/biome/Biome";
            switch (name) {
                case "field_76756_M": method.instructions.add(new MethodInsnNode(INVOKESTATIC, biome, getName("getIdForBiome", "func_185362_a"), "(Lnet/minecraft/world/biome/Biome;)I", false)); return true;
                case "field_76751_G": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, biome, getName("getRainfall", "func_76727_i"), "()F", false)); return true;
                case "field_76748_D": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, biome, getName("getBaseHeight", "func_185355_j"), "()F", false)); return true;
                case "field_76749_E": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, biome, getName("getHeightVariation", "field_76749_E"), "()F", false)); return true;
                case "field_76750_F": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, biome, getName("getDefaultTemperature", "func_185353_n"), "()F", false)); return true;
            }
        }
        if (owner.equals("net/minecraft/util/ChunkCoordinates")) {
            switch (name) {
                case "field_71574_a": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", getName("getX", "func_177958_n"), "()I", false)); return true;
                case "field_71572_b": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", getName("getY", "func_177956_o"), "()I", false)); return true;
                case "field_71573_c": method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", getName("getZ", "func_177952_p"), "()I", false)); return true;
            }
        }
        return super.replaceFieldInsn(method, opcode, owner, name, desc);
    }

    @Override
    protected void onMethodEnd(MethodNode method) {
        { // Item$onItemRightClick
            if (method.name.equals("func_77659_a")) {
                MethodVisitor m = this.visitMethod(ACC_PUBLIC, getName("onItemRightClick", method.name), "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;", null, null);
                m.visitVarInsn(ALOAD, 2);
                m.visitVarInsn(ALOAD, 3);
                m.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/EntityLivingBase", getName("getHeldItem", "func_184586_b"), "(Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;", false);
                m.visitVarInsn(ASTORE, 4);
                m.visitTypeInsn(NEW, "net/minecraft/util/ActionResult");
                m.visitInsn(DUP);
                m.visitFieldInsn(GETSTATIC, "net/minecraft/util/EnumActionResult", "SUCCESS", "Lnet/minecraft/util/EnumActionResult;");
                m.visitVarInsn(ALOAD, 0);
                m.visitVarInsn(ALOAD, 4);
                m.visitVarInsn(ALOAD, 1);
                m.visitVarInsn(ALOAD, 2);
                m.visitMethodInsn(INVOKEVIRTUAL, this.name, method.name, method.desc, false);
                m.visitMethodInsn(INVOKESPECIAL, "net/minecraft/util/ActionResult", "<init>", "(Lnet/minecraft/util/EnumActionResult;Ljava/lang/Object;)V", false);
                m.visitInsn(ARETURN);
            }
        }
        {
            if (method.name.equals("func_148279_a")) {
                { // GuiListExtended.IGuiListEntry$drawEntry
                    MethodVisitor m = this.visitMethod(ACC_PUBLIC, getName("drawEntry", "func_192634_a"), "(IIIIIIIZF)V", null, null);
                    m.visitVarInsn(ALOAD, 0);
                    m.visitVarInsn(ILOAD, 1);
                    m.visitVarInsn(ILOAD, 2);
                    m.visitVarInsn(ILOAD, 3);
                    m.visitVarInsn(ILOAD, 4);
                    m.visitVarInsn(ILOAD, 5);
                    m.visitMethodInsn(INVOKESTATIC, "net/minecraft/client/renderer/Tessellator", getName("getInstance", "func_178181_a"), "()Lnet/minecraft/client/renderer/Tessellator;", false);
                    m.visitVarInsn(ILOAD, 6);
                    m.visitVarInsn(ILOAD, 7);
                    m.visitVarInsn(ILOAD, 8);
                    m.visitMethodInsn(INVOKEVIRTUAL, this.name, "func_148279_a", "(IIIIILnet/minecraft/client/renderer/Tessellator;IIZ)V", false);
                    m.visitInsn(RETURN);
                }
                { // GuiListExtended.IGuiListEntry$updatePosition
                    MethodVisitor m = this.visitMethod(ACC_PUBLIC, getName("updatePosition", "func_192633_a"), "(IIIF)V", null, null);
                    m.visitInsn(RETURN);
                }
            }
        }
//        if (method.name.equals("<init>") && (superName.equals("net/minecraft/client/gui/GuiSlot") || superName.equals("net/minecraft/client/gui/GuiListExtended"))) {
//            { // handleMouseInput | For scrolling
//                MethodVisitor m = this.visitMethod(ACC_PROTECTED, getName("handleMouseInput", "func_178039_p"), "()V", null, null);
//                m.visitVarInsn(ALOAD, 0);
//                this.visitMethodHook(m, "GuiSlot$handleMouseInput", "(Lnet/minecraft/client/gui/GuiSlot;)Z");
//                m.visitInsn(RETURN);
//            }
//        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}