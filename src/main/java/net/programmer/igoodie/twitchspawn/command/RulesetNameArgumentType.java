package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

import java.util.concurrent.CompletableFuture;

public class RulesetNameArgumentType implements ArgumentType<String> {

    public static RulesetNameArgumentType rulesetName() {
        return new RulesetNameArgumentType();
    }

    public static String getRulesetName(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    /* ------------------------------ */

    private RulesetNameArgumentType() { }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return null;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();

        if ("default".startsWith(input))
            builder.suggest("default");

        for (String streamer : ConfigManager.RULESET_COLLECTION.getStreamers()) {
            if (streamer.toLowerCase().startsWith(input))
                builder.suggest(streamer);
        }

        return builder.buildFuture();
    }

    @Override
    public String toString() {
        return "ruleset()";
    }
}
