package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StreamerNickType implements ArgumentType<String> {

    public static StreamerNickType streamerNick() {
        return new StreamerNickType(false);
    }

    public static StreamerNickType streamerNickOrDefault() {
        return new StreamerNickType(true);
    }

    /* ---------------------------------------- */

    private boolean includeDefault;

    private StreamerNickType(boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString(); // Only 1 word is allowed
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();

        if (includeDefault && "default".startsWith(input))
            builder.suggest("default");

        for (String streamer : ConfigManager.RULESET_COLLECTION.getStreamers()) {
            if (streamer.toLowerCase().startsWith(input))
                builder.suggest(streamer);
        }

        return builder.buildFuture();
    }

}
