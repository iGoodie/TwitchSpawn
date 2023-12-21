package net.programmer.igoodie.twitchspawn.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.BuiltInExceptions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLRuleset;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLTokenizer;
import net.programmer.igoodie.twitchspawn.util.MCPHelpers;

public class TwitchSpawnCommand {

    public static final String[] COMMAND_NAMES = {"twitchspawn", "ts"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String commandName : COMMAND_NAMES) {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(commandName);

            root.then(Commands.literal("status").executes(TwitchSpawnCommand::statusModule));
            root.then(Commands.literal("start").executes(TwitchSpawnCommand::startModule));
            root.then(Commands.literal("stop").executes(TwitchSpawnCommand::stopModule));

            root.then(Commands.literal("reloadcfg").executes(TwitchSpawnCommand::reloadModule));

            root.then(Commands.literal("quickrefresh").executes(TwitchSpawnCommand::quickRefreshModule));

            root.then(Commands.literal("rules")
                    .executes(context -> rulesModule(context, null))
                    .then(CommandArguments.rulesetName("ruleset_name")
                            .executes(context -> rulesModule(context, RulesetNameArgumentType.getRulesetName(context, "ruleset_name"))))
            );

            root.then(Commands.literal("simulate")
                    .then(CommandArguments.nbtCompound("event_simulation_json")
                            .executes(context -> simulateModule(context, null))
                            .then(CommandArguments.streamer("streamer_nick")
                                    .executes(context -> simulateModule(context, StreamerArgumentType.getStreamer(context, "streamer_nick")))))
            );

            root.then(Commands.literal("test")
                    .then(CommandArguments.streamer("streamer_nick")
                            .executes(context -> testModule(context, StreamerArgumentType.getStreamer(context, "streamer_nick"))))
            );

            root.then(Commands.literal("execute")
                    .then(CommandArguments.tslWords("tsl_action")
                            .executes(TwitchSpawnCommand::executeModule))
            );

            dispatcher.register(root);
        }
    }

    /* ------------------------------------------------------------ */

    public static int statusModule(CommandContext<CommandSourceStack> context) {
        String translationKey = TwitchSpawn.TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        context.getSource().sendSuccess(() -> Component.translatable(translationKey), false);

        return 1;
    }

    public static int startModule(CommandContext<CommandSourceStack> context) {
        String sourceNickname = context.getSource().getTextName();

        // If has no permission
        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.start.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to run TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        try {
            TwitchSpawn.TRACE_MANAGER.start();
            return 1;

        } catch (IllegalStateException e) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.start.illegal_state"), true);
            return 0;
        }
    }

    public static int stopModule(CommandContext<CommandSourceStack> context) {
        String sourceNickname = context.getSource().getTextName();

        // If has no permission
        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.stop.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to stop TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        try {
            TwitchSpawn.TRACE_MANAGER.stop(context.getSource(), "Command execution");
            return 1;

        } catch (IllegalStateException e) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.stop.illegal_state"), true);
            return 0;
        }
    }

    public static int reloadModule(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String sourceNickname = source.getTextName();

        boolean isOp = TwitchSpawn.SERVER.isSingleplayer()
                || Stream.of(TwitchSpawn.SERVER.getPlayerList().getOpNames())
                .anyMatch(oppedPlayerName -> oppedPlayerName.equalsIgnoreCase(sourceNickname));

        // If is not OP or has no permission
        if (!isOp && !ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.reloadcfg.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to reload TwitchSpawn configs, but no permission", sourceNickname);
            return 0;
        }

        if (TwitchSpawn.TRACE_MANAGER.isRunning()) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.reloadcfg.already_started"), false);
            return 0;
        }

        try {
            ConfigManager.loadConfigs();
            source.sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.reloadcfg.success"), false);
            return 1;

        } catch (TwitchSpawnLoadingErrors e) {
            String errorLog = "• " + e.toString().replace("\n", "\n• ");
            source.sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.reloadcfg.invalid_syntax", errorLog), false);
            return 0;
        }
    }

    public static int quickRefreshModule(CommandContext<CommandSourceStack> context) {
        String sourceNickname = context.getSource().getTextName();

        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.reloadcfg.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to run TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        if (TwitchSpawn.TRACE_MANAGER.isRunning()) {
            TwitchSpawn.TRACE_MANAGER.stop(context.getSource(), "Quick refreshing");
        }

        reloadModule(context);
        startModule(context);
        return 1;
    }

    /* ------------------------------------------------------------ */

    public static int rulesModule(CommandContext<CommandSourceStack> context, String rulesetName) {
        if (rulesetName == null) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.rules.list",
                    ConfigManager.RULESET_COLLECTION.getStreamers()), true);
            return 1;
        }

        TSLRuleset ruleset = ConfigManager.RULESET_COLLECTION.getRuleset(rulesetName);

        if (ruleset == null) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.rules.one.fail",
                    rulesetName), true);
            return 0;
        }

        String translationKey = rulesetName.equalsIgnoreCase("default") ?
                "commands.twitchspawn.rules.default" : "commands.twitchspawn.rules.one";
        context.getSource().sendSuccess(() -> Component.translatable(translationKey,
                rulesetName, ruleset.toString()), true);
        return 1;
    }

    /* ------------------------------------------------------------ */

    public static int simulateModule(CommandContext<CommandSourceStack> context, String streamerNick) {
        try {
            String sourceName = context.getSource().getTextName();
            String streamerName = streamerNick != null ? streamerNick : sourceName;

            // If has no permission
            if (!ConfigManager.CREDENTIALS.hasPermission(sourceName)) {
                context.getSource().sendSuccess(() -> Component.translatable(
                        "commands.twitchspawn.simulate.no_perm"), true);
                TwitchSpawn.LOGGER.info("{} tried to simulate an event, but no permission", sourceName);
                return 0;
            }

            CompoundTag nbt = context.getArgument("event_simulation_json", CompoundTag.class);
            String eventName = nbt.getString("event");

            if (eventName.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.translatable(
                        "commands.twitchspawn.simulate.missing"), true);
                return 0;
            }

            Set<TSLEventPair> eventPairs = TSLEventKeyword.toPairs(eventName);

            if (eventPairs == null) {
                context.getSource().sendSuccess(() -> Component.translatable(
                        "commands.twitchspawn.simulate.invalid_event", eventName), true);
                return 0;
            }

            TSLEventPair eventPair = eventPairs.iterator().next();

            boolean random = nbt.getBoolean("random");
            EventArguments simulatedEvent = new EventArguments(eventPair.getEventType(), eventPair.getEventAccount());
            simulatedEvent.streamerNickname = streamerName;

            if (random) {
                simulatedEvent.randomize("SimulatorDude", "Simulating a message");

            } else {
                simulatedEvent.actorNickname = nbt.getString("actor").isEmpty() ? "SimulatorDude" : nbt.getString("actor");
                simulatedEvent.message = nbt.contains("message") ? nbt.getString("message") : "Simulating a message";
                simulatedEvent.donationAmount = nbt.getDouble("amount");
                simulatedEvent.donationCurrency = nbt.getString("currency");
                simulatedEvent.subscriptionMonths = nbt.getInt("months");
                simulatedEvent.raiderCount = nbt.getInt("raiders");
                simulatedEvent.viewerCount = nbt.getInt("viewers");
                simulatedEvent.subscriptionTier = nbt.contains("tier", 3) ? nbt.getInt("tier") : -1;
                simulatedEvent.gifted = nbt.getBoolean("gifted");
                simulatedEvent.rewardTitle = nbt.getString("title");
            }

            ConfigManager.RULESET_COLLECTION.handleEvent(simulatedEvent);

            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.twitchspawn.simulate.success", nbt), true);

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int executeModule(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            String words = TSLWordsArgumentType.getWords(context, "tsl_action");

            List<String> wordTokens = TSLTokenizer.intoWords(words);
            String actionName = wordTokens.remove(0);

            TSLAction tslAction = TSLParser.parseAction(actionName, wordTokens);
            EventArguments eventArguments = EventArguments.createRandom(context.getSource().getTextName());
            tslAction.process(eventArguments);

        } catch (TSLSyntaxError e) {
            throw new CommandSyntaxException(new BuiltInExceptions().dispatcherParseException(), Component.translatable(e.getMessage()));
        }

        return 1;
    }

    private static final int DEFAULT_FADE_IN_TICKS = 10;
    private static final int DEFAULT_STAY_TICKS = 70;
    private static final int DEFAULT_FADE_OUT_TICKS = 20;

    public static int testModule(CommandContext<CommandSourceStack> context, String streamerNick) throws CommandSyntaxException {
        if (!ConfigManager.RULESET_COLLECTION.hasStreamer(streamerNick)) {
            TwitchSpawn.LOGGER.info("There are no ruleset associated with {}", streamerNick);
            context.getSource().sendSuccess(() -> Component.translatable("commands.twitchspawn.test.not_found", streamerNick), true);
            return 0;
        }

        ServerPlayer streamerPlayer = context.getSource().getPlayerOrException();
        TSLRuleset ruleset = ConfigManager.RULESET_COLLECTION.getRuleset(streamerNick);
        EventQueue eventQueue = ConfigManager.RULESET_COLLECTION.getQueue(streamerNick);

        Collection<TSLEvent> events = ruleset.getEvents();
        Iterator<TSLEvent> eventIterator = events.iterator();
        TSLEvent event;
        int index = 0;

        while (eventIterator.hasNext()) {
            event = eventIterator.next();

            TSLEventPair eventPair = TSLEventKeyword.toPairs(event.getName()).iterator().next();
            EventArguments eventArguments = new EventArguments(eventPair);
            eventArguments.randomize();
            eventArguments.streamerNickname = streamerPlayer.getName().getString();
            eventArguments.actorNickname = "TesterKid";

            for (TSLAction action : event.getActions()) {
                Component text = MCPHelpers.fromJsonLenient(
                        String.format("{text:\"Testing %s action\", color:\"dark_purple\"}", TSLActionKeyword.ofClass(action.getClass())));
                ClientboundSetTitleTextPacket packet = new ClientboundSetTitleTextPacket(text);

                Component subtext = MCPHelpers.fromJsonLenient(
                        String.format("{text:\"Rules traversed: %.02f%%\", color:\"dark_purple\"}", 100 * (index + 1f) / ruleset.getRulesRaw().size()));
                ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(subtext);

                ClientboundSetTitlesAnimationPacket animationTimePacket = new ClientboundSetTitlesAnimationPacket(DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);

                eventQueue.queue(() -> {
                    streamerPlayer.connection.send(packet);
                    streamerPlayer.connection.send(subtitlePacket);
                    streamerPlayer.connection.send(animationTimePacket);
                });
                eventQueue.queueSleep();
                eventQueue.queue(() -> action.process(eventArguments));
                eventQueue.queueSleep();

                index++;
            }

            eventQueue.updateThread();
        }

        TwitchSpawn.LOGGER.info("Tests queued for {}", streamerNick);
        context.getSource().sendSuccess(() -> Component.translatable("commands.twitchspawn.test.success", streamerNick), true);
        return 1;
    }

}
