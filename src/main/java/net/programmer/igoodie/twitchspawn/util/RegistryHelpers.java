package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

public class RegistryHelpers {

    private static HolderLookup.Provider vanillaLookup;

    /**
     * Registries used to parse items and text components. Uses the running server's registries
     * (including datapack content) when available, otherwise falls back to vanilla's built-in
     * ones so rules can still be validated before a world is loaded.
     */
    public static HolderLookup.Provider lookup() {
        if (TwitchSpawn.SERVER != null)
            return TwitchSpawn.SERVER.registryAccess();

        if (vanillaLookup == null)
            vanillaLookup = VanillaRegistries.createWorldLookup();

        return vanillaLookup;
    }

}
