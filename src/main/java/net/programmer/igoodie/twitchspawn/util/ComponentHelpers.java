package net.programmer.igoodie.twitchspawn.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class ComponentHelpers {

    public static Component fromJsonLenient(String string) {
        if (string == null || string.isBlank())
            return null;

        JsonElement json = JsonParser.parseString(string); // Lenient by default
        return ComponentSerialization.CODEC
                .parse(RegistryHelpers.lookup().createSerializationContext(JsonOps.INSTANCE), json)
                .getOrThrow(JsonParseException::new);
    }

    public static Component merge(Component c1, Component c2) {
        return c1.copy().append(c2);
    }

}
