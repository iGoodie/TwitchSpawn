package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MCPHelpers {

    public static Component fromJsonLenient(String string) {
        return Component.Serializer.fromJsonLenient(string);
    }

    public static Component merge(Component c1, Component c2) {
        MutableComponent deepCopy = c1.copy();
        deepCopy.getSiblings().add(c2);
        return deepCopy;
    }

}
