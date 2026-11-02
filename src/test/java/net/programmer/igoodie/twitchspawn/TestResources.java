package net.programmer.igoodie.twitchspawn;

import com.google.common.io.Resources;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestResources {

    public static String loadAsString(String filename) {
        try {
            URL resourceURL = Resources.getResource(filename);
            List<String> lines = Resources.readLines(resourceURL, StandardCharsets.UTF_8);
            return String.join("\n", lines);

        } catch (Exception e) {
            throw new InternalError("Cannot load test resource -> " + filename, e);
        }
    }

    /**
     * Initializes vanilla registries (items, entities etc.),
     * required by actions that validate their parameters while parsing.
     */
    public static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

}
