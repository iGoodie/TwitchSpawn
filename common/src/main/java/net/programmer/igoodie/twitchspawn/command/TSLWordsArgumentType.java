package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class TSLWordsArgumentType implements ArgumentType<String> {

    public static TSLWordsArgumentType tslWords() {
        return new TSLWordsArgumentType();
    }

    public static String getWords(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    /* ------------------------------------ */

    private TSLWordsArgumentType() { }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        StringBuilder tslWords = new StringBuilder();

        while (reader.canRead())
            tslWords.append(reader.read());

        return tslWords.toString();
    }

    @Override
    public String toString() {
        return "tslWords()";
    }
}
