package surreal.versionchaser.core.visitors;

import surreal.versionchaser.core.BaseClassNode;

public class ClassVisitor1710 extends BaseClassNode {

    public ClassVisitor1710() {}

    @Override
    protected String getDesc(String desc) {
        desc = super.getDesc(desc);
        String cpw = "Lcpw/mods";
        String forge = "Lnet/minecraftforge";
        if (desc.startsWith(cpw)) {
            return desc.replace(cpw, forge);
        }
        return desc;
    }
}
