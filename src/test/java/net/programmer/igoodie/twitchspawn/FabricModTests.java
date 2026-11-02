package net.programmer.igoodie.twitchspawn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModDependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Sanity checks for the Fabric side of the port: mod metadata, entrypoints and bundled assets.
 */
public class FabricModTests {

    private static JsonObject loadJson(String path) {
        return JsonParser.parseString(TestResources.loadAsString(path)).getAsJsonObject();
    }

    @Test
    @DisplayName("should be discovered and resolved by Fabric Loader.")
    public void modResolvedTest() {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(TwitchSpawn.MOD_ID);

        Assertions.assertTrue(mod.isPresent(), "Fabric Loader did not load " + TwitchSpawn.MOD_ID);
        Assertions.assertTrue(FabricLoader.getInstance().isModLoaded("fabric-api"), "Fabric API is not loaded");
    }

    @Test
    @DisplayName("should expand the mod version into fabric.mod.json.")
    public void versionExpandedTest() {
        String expected = System.getProperty("twitchspawn.test.modVersion");
        Assertions.assertNotNull(expected, "Run through Gradle so the mod version is passed in");

        String version = FabricLoader.getInstance().getModContainer(TwitchSpawn.MOD_ID)
                .orElseThrow().getMetadata().getVersion().getFriendlyString();

        Assertions.assertEquals(expected, version);
    }

    @Test
    @DisplayName("should depend on the Minecraft version it is built against.")
    public void minecraftDependencyTest() throws Exception {
        String minecraftVersion = System.getProperty("twitchspawn.test.minecraftVersion");
        Assertions.assertNotNull(minecraftVersion, "Run through Gradle so the Minecraft version is passed in");

        ModDependency minecraft = FabricLoader.getInstance().getModContainer(TwitchSpawn.MOD_ID)
                .orElseThrow().getMetadata().getDependencies().stream()
                .filter(dependency -> dependency.getKind() == ModDependency.Kind.DEPENDS)
                .filter(dependency -> dependency.getModId().equals("minecraft"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("fabric.mod.json must depend on minecraft"));

        Assertions.assertTrue(minecraft.matches(Version.parse(minecraftVersion)),
                "fabric.mod.json minecraft range " + minecraft.getVersionRequirements()
                        + " does not accept " + minecraftVersion);
    }

    @Test
    @DisplayName("should point entrypoints to the right initializer types.")
    public void entrypointsTest() throws ClassNotFoundException {
        JsonObject entrypoints = loadJson("fabric.mod.json").getAsJsonObject("entrypoints");

        String main = entrypoints.getAsJsonArray("main").get(0).getAsString();
        String client = entrypoints.getAsJsonArray("client").get(0).getAsString();

        Assertions.assertEquals(TwitchSpawn.class.getName(), main);
        Assertions.assertTrue(ModInitializer.class.isAssignableFrom(Class.forName(main, false, getClass().getClassLoader())));
        Assertions.assertTrue(ClientModInitializer.class.isAssignableFrom(Class.forName(client, false, getClass().getClassLoader())));
    }

    @Test
    @DisplayName("should ship the icon and every sound referenced by sounds.json.")
    public void assetsTest() {
        String icon = loadJson("fabric.mod.json").get("icon").getAsString();
        Assertions.assertNotNull(getClass().getClassLoader().getResource(icon), "Missing icon " + icon);

        JsonObject sounds = loadJson("assets/twitchspawn/sounds.json");
        Assertions.assertTrue(sounds.has("pop_in") && sounds.has("pop_out"), "Registered sounds missing from sounds.json");

        sounds.entrySet().forEach(entry -> entry.getValue().getAsJsonObject().getAsJsonArray("sounds").forEach(sound -> {
            String path = sound.getAsString().replace("twitchspawn:", "assets/twitchspawn/sounds/") + ".ogg";
            Assertions.assertNotNull(getClass().getClassLoader().getResource(path), "Missing sound file " + path);
        }));

        Assertions.assertNotNull(getClass().getClassLoader().getResource("assets/twitchspawn/textures/indicators.png"));
        Assertions.assertNotNull(getClass().getClassLoader().getResource("assets/twitchspawn/textures/cooldown.png"));
    }

    @Test
    @DisplayName("should not ship Forge leftovers.")
    public void noForgeLeftoversTest() {
        Assertions.assertNull(getClass().getClassLoader().getResource("META-INF/mods.toml"));
    }

}
