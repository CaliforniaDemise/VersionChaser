package surreal.versionchaser.core.hooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.GameData;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("unused")
public class Hooks1710 {

    private static final Gson GSON_SHIT = new GsonBuilder().disableHtmlEscaping().create();
    public static final List<Item> ITEMS = new ArrayList<>(32); // For loading models of 1.7.10 items

    public static void GameRegistry$addRecipe(IRecipe recipe) {
        ModContainer container = Loader.instance().activeModContainer();
        String modId;
        if (container != null) modId = container.getModId();
        else modId = "zup";
        ItemStack stack = recipe.getRecipeOutput();
        String id = stack.isEmpty() ? "" + System.currentTimeMillis() : "" + stack.hashCode();
        recipe.setRegistryName(new ResourceLocation(modId, id));
        ForgeRegistries.RECIPES.register(recipe);
    }

    public static void GameRegistry$addShapelessRecipe(ItemStack output, Object[] inputs) {
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

    public static void GameRegistry$registerItem(Item item, String regName) {
        regName = regName.toLowerCase(Locale.US);
        String modId = "minecraft";
        ModContainer container = Loader.instance().activeModContainer();
        if (container != null) {
            modId = container.getModId();
        }
        item.setRegistryName(modId, regName);
        GameData.register_impl(item);
        ITEMS.add(item);

        if (FMLLaunchHandler.side() == Side.CLIENT && item instanceof ItemWithIcon) {
            ItemWithIcon iconItem = (ItemWithIcon) item;
            iconItem.func_94581_a(null);
            File modelsLoc = new File(Launch.minecraftHome.getAbsolutePath() + "/.vchaser_assets/assets/" + Objects.requireNonNull(item.getRegistryName()).getNamespace() + "/models");

            JsonObject model = new JsonObject();
            JsonArray overrides = new JsonArray();

            for (int iconIndex = 0; iconIndex < iconItem.icons.size(); iconIndex++) {
                String[] amonga = iconItem.icons.get(iconIndex).split(":");
                String modelLocation = "item/" + amonga[1];
                String textureName = amonga[0] + ":items/" + amonga[1];

                {
                    JsonObject iconModel = new JsonObject();
                    iconModel.addProperty("parent", "item/generated");
                    JsonObject textures = new JsonObject();
                    textures.addProperty("layer0", textureName);
                    iconModel.add("textures", textures);

                    File file = new File(modelsLoc, modelLocation + ".json");
                    file.getParentFile().mkdirs();
                    try {
                        FileWriter fw = new FileWriter(file);
                        String str = GSON_SHIT.toJson(iconModel);
                        fw.write(str);
                        fw.close();
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                JsonObject iconModel = new JsonObject();
                JsonObject predicate = new JsonObject();
                predicate.addProperty("icon_getter", iconIndex);
                iconModel.add("predicate", predicate);
                iconModel.addProperty("model", amonga[0] + ":" + modelLocation);
                overrides.add(iconModel);
            }

            model.addProperty("parent", "item/generated");
            model.add("overrides", overrides);

            File file = new File(modelsLoc, "item/" + regName + ".json");
            file.getParentFile().mkdirs();
            try {
                FileWriter fw = new FileWriter(file);
                String str = GSON_SHIT.toJson(model);
                fw.write(str);
                fw.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Biome[] BiomeGenBase$getBiomeArray() {
        return ForgeRegistries.BIOMES.getValuesCollection().toArray(new Biome[0]);
    }

    public static PositionedSoundRecord PositionedSoundRecord$getMasterRecord(ResourceLocation location, float f) {
        SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(location);
        if (event == null) event = SoundEvents.BLOCK_ANVIL_FALL;
        return PositionedSoundRecord.getMasterRecord(event, f);
    }

    public static Biome World$getBiomeGenForCoords(World world, int x, int z) {
        return world.getBiome(new BlockPos(x, 255, z));
    }

    public static int FontRenderer$drawString(FontRenderer renderer, String str, int x, int y, int color, boolean dropShadow) {
        return renderer.drawString(str, x, y, color, dropShadow);
    }

    public static String Property$getComment(Property property) {
        return property.getComment();
    }

    public static void Property$changeComment(Property property, String comment) {
        property.setComment(comment);
    }

    // TODO Make this actually work, for some reason it doesn't work
    // I don't know why mc doesn't handle it by default
    public static boolean GuiSlot$handleMouseInput(GuiSlot gui) {
        int dwheel = Mouse.getEventDWheel();
        System.out.println(dwheel);
        if (dwheel > 0) {
            gui.scrollBy(gui.getSlotHeight());
        }
        else if (dwheel < 0) gui.scrollBy(-gui.getSlotHeight());
        return false;
    }

    public static String IIconRegister$registerIcon(@Nullable Object iconRegistry, String iconName, Item item) {
        ItemWithIcon iconItem = (ItemWithIcon) item;
        iconItem.icons.add(iconName);
        return iconName;
    }

    public static class ItemWithIcon extends Item {

        protected static final IItemPropertyGetter ICON_GETTER = (stack, worldIn, entityIn) -> {
            String iconStr;
            ItemWithIcon iconItem = (ItemWithIcon) stack.getItem();
            if (entityIn instanceof EntityPlayer) {
                int useTime = 0;
                if (entityIn.getActiveItemStack().getItem() == iconItem) {
                    useTime = entityIn.getItemInUseCount();
                }
                iconStr = iconItem.getIcon(stack, 1, (EntityPlayer) entityIn, stack, useTime);
            }
            else iconStr = iconItem.getIcon(stack, 1);
            if (iconStr.equals(iconItem.field_77791_bV)) return 0F;
            int index = iconItem.icons.indexOf(iconStr);
            if (index == -1) index = 0;
            return index;
        };

        public List<String> icons = new ArrayList<>(1);

        public ItemWithIcon() {
            if (FMLLaunchHandler.side() == Side.CLIENT) {
                this.addPropertyOverride(new ResourceLocation("icon_getter"), ICON_GETTER);
            }
        }

        protected String field_77791_bV = ""; // Default texture

        public void func_94581_a(@Nullable Object iiconRegister) {} // Texture registry
        public String func_77617_a(int metadata) { return this.field_77791_bV; } // getTextureFromDamage
        public String func_77618_c(int metadata, int renderPass) { return this.func_77617_a(metadata); } // getTextureFromRenderPass
        public String getIcon(ItemStack stack, int renderPass) { return this.func_77617_a(stack.getMetadata()); }
        public String getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) { return this.getIcon(stack, renderPass); }
    }
}