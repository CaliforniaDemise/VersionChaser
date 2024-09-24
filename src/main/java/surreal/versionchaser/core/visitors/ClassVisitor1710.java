package surreal.versionchaser.core.visitors;

import surreal.versionchaser.core.BaseClassNode;

public class ClassVisitor1710 extends BaseClassNode {

    public ClassVisitor1710() {}

    @Override
    protected String getOwner(String owner) {
        owner = super.getOwner(owner);
        String cpw = "cpw/mods";
        String forge = "net/minecraftforge";
        if (owner.startsWith(cpw)) {
            return owner.replace(cpw, forge);
        }
        return owner;
    }

    @Override
    protected String getDesc(String desc) {
        desc = super.getDesc(desc);
        String cpw = "Lcpw/mods";
        String forge = "Lnet/minecraftforge";
        if (desc.contains(cpw)) {
            return desc.replace(cpw, forge);
        }
        return desc;
    }
}