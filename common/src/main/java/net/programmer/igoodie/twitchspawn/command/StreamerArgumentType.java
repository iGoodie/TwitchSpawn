package net.programmer.igoodie.twitchspawn.command;


import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

public class StreamerArgumentType implements ArgumentType<String> {

    public static StreamerArgumentType streamerNick() {
        return new StreamerArgumentType();
    }

    public static String getStreamer(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    /* ---------------------------------------- */


    public StreamerArgumentType() { }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString(); // Only 1 word is allowed
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();

        for (String streamer : ConfigManager.RULESET_COLLECTION.getStreamers()) {
            if (streamer.toLowerCase().startsWith(input))
                builder.suggest(streamer);
        }

        return builder.buildFuture();
    }

    @Override
    public String toString() {
        return "streamer()";
    }
}
