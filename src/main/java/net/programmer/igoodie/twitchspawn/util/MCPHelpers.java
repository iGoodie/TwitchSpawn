package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.network.chat.MutableComponent;

public class MCPHelpers {

    public static MutableComponent fromJsonLenient(String string) {
        return MutableComponent.Serializer.fromJsonLenient(string);
    }

    public static MutableComponent merge(MutableComponent c1, MutableComponent c2) {
        MutableComponent deepCopy = c1.copy();
        deepCopy.getSiblings().add(c2);
        return deepCopy;
    }

}
