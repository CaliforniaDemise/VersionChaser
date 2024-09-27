package surreal.versionchaser.core;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import surreal.versionchaser.asm.Patcher;
import surreal.versionchaser.core.hooks.Hooks1710;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.Name("VersionChaser")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class VCModPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("VersionChaser");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public VCModPlugin() {
        // Handle it here
        File mods = new File("mods"); // <instance>/mods
        streamMods(mods);
    }

    @Override
    public String getModContainerClass() {
        return "surreal.versionchaser.core.VCModPlugin$Container";
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    // Stream mods to patch them
    private static void streamMods(File modsDir) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir.toPath());

            for (Path path : stream) {
                File file = path.toFile();
                String fileName = file.getName();
                if (file.isFile() && !fileName.contains("-chased")) {
                    if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
                        ZipFile modFile = new ZipFile(file);
                        ZipEntry entry = modFile.getEntry("mcmod.info");
                        if (entry == null) { // TODO A fallback
                            LOGGER.error("{} doesn't have mcmod.info file. If this mod isn't a 1.12.2 mod the game is most likely to crash.", fileName);
                            continue;
                        }
                        JsonObject modInfo = readInfoStream(modFile.getInputStream(entry));
                        if (!modInfo.has("mcversion")) {
                            LOGGER.error("{} didn't set mcversion property in mcmod.info, skipping. If this mod isn't a 1.12.2 mod the game is most likely to crash.", fileName);
                        }
                        Patcher.patch(file, modFile, modInfo.get("mcversion").getAsString());
                        modFile.close();
                        IOUtils.copy(Files.newInputStream(file.toPath()), Files.newOutputStream(Paths.get(file.getAbsolutePath() + ".disabled")));
                        file.delete();
                    }
                    else if (FMLLaunchHandler.isDeobfuscatedEnvironment() && fileName.endsWith(".disabled")) {
                        ZipFile modFile = new ZipFile(file);
                        JsonObject modInfo = readInfoStream(modFile.getInputStream(modFile.getEntry("mcmod.info")));
                        Patcher.patch(file, modFile, modInfo.get("mcversion").getAsString());
                        modFile.close();
                    }
                }
            }

            stream.close();

        }
        catch (IOException e) {
            LOGGER.error("Could not stream mods directory");
        }
    }

    private static JsonObject readInfoStream(InputStream stream) {
        return GSON.fromJson(new InputStreamReader(stream), JsonArray.class).get(0).getAsJsonObject();
    }

    public static class Container extends DummyModContainer {
        public Container() {
            super(new ModMetadata());
            readInfoFile(getMetadata());
        }

        private static void readInfoFile(ModMetadata metadata) {
            InputStream stream = VCModPlugin.class.getResourceAsStream("/mcmod.info");
            if (stream == null) {
                LOGGER.error("Could not read mcmod.info");
                return;
            }

            JsonObject object =  readInfoStream(stream);

            metadata.modId = object.get("modid").getAsString();
            metadata.name = object.get("name").getAsString();
            metadata.description = object.get("description").getAsString();
            metadata.version = object.get("version").getAsString();
            metadata.url = object.get("url").getAsString();
            metadata.authorList = Collections.singletonList("Surreal");
        }

        @Override
        @SuppressWarnings("UnstableApiUsage")
        public boolean registerBus(EventBus bus, LoadController controller) {
            bus.register(this);
            MinecraftForge.EVENT_BUS.register(this);
            return true;
        }

        @Override
        public Class<?> getCustomResourcePackClass() {
            return VCResourcePack.class;
        }

        @SubscribeEvent
        public void registerModels(ModelRegistryEvent event) {
            Hooks1710.ITEMS.forEach(item -> {
                ModelResourceLocation location = new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory");
                ModelLoader.setCustomMeshDefinition(item, i -> location);
            });
        }
    }

    public static class VCResourcePack extends FolderResourcePack implements FMLContainerHolder {

        private final ModContainer container;

        public VCResourcePack(ModContainer container) {
            super(new File(Launch.minecraftHome, ".vchaser_assets"));
            this.container = container;
        }

        @Override
        public ModContainer getFMLContainer() {
            return this.container;
        }

        @Nonnull
        @Override
        public String getPackName() {
            return "ResourceChaser";
        }

        @Override
        @ParametersAreNonnullByDefault
        public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
            JsonObject metadata = new JsonObject();
            JsonObject packObj = new JsonObject();
            metadata.add("pack", packObj);
            packObj.addProperty("description", "Includes assets moved by VersionChaser.");
            packObj.addProperty("pack_format", 2);
            return metadataSerializer.parseMetadataSection(metadataSectionName, metadata);
        }
    }
}
