package net.programmer.igoodie.twitchspawn.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.data.registries.VanillaRegistries;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

public class RegistryHelpers {

    private static final SimpleCommandExceptionType TRAILING_ITEM_INPUT = new SimpleCommandExceptionType(
            Component.literal("Unexpected text after item. Item NBT like {display:{...}} is no longer supported, "
                    + "use components instead, e.g. stick[custom_name=\"Name\"]"));

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

    /**
     * Parses an item with its components. Unlike {@link ItemParser#parse(StringReader)},
     * rejects leftover input, so outdated NBT syntax is not silently ignored.
     */
    public static ItemInput parseItem(String input) throws CommandSyntaxException {
        StringReader reader = new StringReader(input);
        ItemInput itemInput = new ItemParser(lookup()).parse(reader);

        if (reader.canRead())
            throw TRAILING_ITEM_INPUT.createWithContext(reader);

        return itemInput;
    }

}
