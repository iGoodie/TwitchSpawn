package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class MCPHelpers {

    public static ITextComponent fromJsonLenient(String string) {
        return ITextComponent.Serializer.func_240644_b_(string);
    }

    public static ITextComponent merge(ITextComponent c1, ITextComponent c2) {
        IFormattableTextComponent deepCopy = c1.deepCopy();
        deepCopy.getSiblings().add(c2);
        return deepCopy;
    }

}
