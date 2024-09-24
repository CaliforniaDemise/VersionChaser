package surreal.versionchaser.core;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class VCHooks {

    public static void GameRegistry$addShapelessRecipe1710(ItemStack output, Object[] inputs) {
        ModContainer container = Loader.instance().activeModContainer();
        String modId;
        if (container != null) modId = container.getModId();
        else modId = "zup";
        Ingredient[] ins = new Ingredient[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            ins[i] = CraftingHelper.getIngredient(inputs[i]);
        }
        GameRegistry.addShapelessRecipe(new ResourceLocation(modId, "" + output.hashCode()), null, output, ins);
    }

}
