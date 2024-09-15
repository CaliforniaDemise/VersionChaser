package surreal.versionchaser.core;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import surreal.versionchaser.asm.Patcher;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipFile;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.Name("VersionChaser")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class ModPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("VersionChaser");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public ModPlugin() {
        // Handle it here
        File mods = new File("mods"); // <instance>/mods
        streamMods(mods);
    }

    @Override
    public String getModContainerClass() {
        return "surreal.versionchaser.core.ModPlugin$Container";
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

                if (file.isFile() && !fileName.endsWith("-chased.jar") && !fileName.endsWith("-chased.zip")) {
                    ZipFile modFile = new ZipFile(file);
                    JsonObject modInfo = readInfoStream(modFile.getInputStream(modFile.getEntry("mcmod.info")));
                    Patcher.patch(file, modFile, modInfo.get("mcversion").getAsString());
                    modFile.close();
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
            InputStream stream = ModPlugin.class.getResourceAsStream("/mcmod.info");
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
            return true;
        }
    }
}
