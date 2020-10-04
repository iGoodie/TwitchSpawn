package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLRuleset;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLTokenizer;
import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;
import net.programmer.igoodie.twitchspawn.util.MCPHelpers;

import java.util.*;
import java.util.stream.Stream;

public class TwitchSpawnCommand {

    public static final String[] COMMAND_NAMES = {"twitchspawn", "ts"};

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        for (String commandName : COMMAND_NAMES) {
            LiteralArgumentBuilder<CommandSource> root = Commands.literal(commandName);

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

    public static int statusModule(CommandContext<CommandSource> context) {
        String translationKey = TwitchSpawn.TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        context.getSource().sendFeedback(new TranslationTextComponent(translationKey), false);

        return 1;
    }

    public static int startModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        // If has no permission
        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.start.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to run TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        try {
            TwitchSpawn.TRACE_MANAGER.start();
            return 1;

        } catch (IllegalStateException e) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.start.illegal_state"), true);
            return 0;
        }
    }

    public static int stopModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        // If has no permission
        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.stop.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to stop TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        try {
            TwitchSpawn.TRACE_MANAGER.stop(context.getSource(), "Command execution");
            return 1;

        } catch (IllegalStateException e) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.stop.illegal_state"), true);
            return 0;
        }
    }

    public static int reloadModule(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        String sourceNickname = source.getName();

        boolean isOp = TwitchSpawn.SERVER.isSinglePlayer()
                || Stream.of(TwitchSpawn.SERVER.getPlayerList().getOppedPlayerNames())
                .anyMatch(oppedPlayerName -> oppedPlayerName.equalsIgnoreCase(sourceNickname));

        // If is not OP or has no permission
        if (!isOp && !ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.reloadcfg.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to reload TwitchSpawn configs, but no permission", sourceNickname);
            return 0;
        }

        if (TwitchSpawn.TRACE_MANAGER.isRunning()) {
            source.sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.reloadcfg.already_started"), false);
            return 0;
        }

        try {
            ConfigManager.loadConfigs();
            source.sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.reloadcfg.success"), false);
            return 1;

        } catch (TwitchSpawnLoadingErrors e) {
            String errorLog = "• " + e.toString().replace("\n", "\n• ");
            source.sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.reloadcfg.invalid_syntax", errorLog), false);
            return 0;
        }
    }

    public static int quickRefreshModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent(
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

    public static int rulesModule(CommandContext<CommandSource> context, String rulesetName) {
        if (rulesetName == null) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.rules.list",
                    ConfigManager.RULESET_COLLECTION.getStreamers()), true);
            return 1;
        }

        TSLRuleset ruleset = ConfigManager.RULESET_COLLECTION.getRuleset(rulesetName);

        if (ruleset == null) {
            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.rules.one.fail",
                    rulesetName), true);
            return 0;
        }

        String translationKey = rulesetName.equalsIgnoreCase("default") ?
                "commands.twitchspawn.rules.default" : "commands.twitchspawn.rules.one";
        context.getSource().sendFeedback(new TranslationTextComponent(translationKey,
                rulesetName, ruleset.toString()), true);
        return 1;
    }

    /* ------------------------------------------------------------ */

    public static int simulateModule(CommandContext<CommandSource> context, String streamerNick) {
        try {
            String sourceName = context.getSource().getName();
            String streamerName = streamerNick != null ? streamerNick : sourceName;

            // If has no permission
            if (!ConfigManager.CREDENTIALS.hasPermission(sourceName)) {
                context.getSource().sendFeedback(new TranslationTextComponent(
                        "commands.twitchspawn.simulate.no_perm"), true);
                TwitchSpawn.LOGGER.info("{} tried to simulate an event, but no permission", sourceName);
                return 0;
            }

            CompoundNBT nbt = context.getArgument("event_simulation_json", CompoundNBT.class);
            String eventName = nbt.getString("event");

            if (eventName.isEmpty()) {
                context.getSource().sendFeedback(new TranslationTextComponent(
                        "commands.twitchspawn.simulate.missing"), true);
                return 0;
            }

            Set<TSLEventPair> eventPairs = TSLEventKeyword.toPairs(eventName);

            if (eventPairs == null) {
                context.getSource().sendFeedback(new TranslationTextComponent(
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

            context.getSource().sendFeedback(new TranslationTextComponent(
                    "commands.twitchspawn.simulate.success", nbt), true);

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int executeModule(CommandContext<CommandSource> context) throws CommandSyntaxException {
        try {
            String words = TSLWordsArgumentType.getWords(context, "tsl_action");

            List<String> wordTokens = TSLTokenizer.intoWords(words);
            String actionName = wordTokens.remove(0);

            TSLAction tslAction = TSLParser.parseAction(actionName, wordTokens);
            EventArguments eventArguments = EventArguments.createRandom(context.getSource().getName());
            tslAction.process(eventArguments);

        } catch (TSLSyntaxError e) {
            throw new CommandException(new StringTextComponent(e.getMessage()));
        }

        return 1;
    }

    private static final int DEFAULT_FADE_IN_TICKS = 10;
    private static final int DEFAULT_STAY_TICKS = 70;
    private static final int DEFAULT_FADE_OUT_TICKS = 20;

    public static int testModule(CommandContext<CommandSource> context, String streamerNick) throws CommandSyntaxException {
        if (!ConfigManager.RULESET_COLLECTION.hasStreamer(streamerNick)) {
            TwitchSpawn.LOGGER.info("There are no ruleset associated with {}", streamerNick);
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.test.not_found", streamerNick), true);
            return 0;
        }

        ServerPlayerEntity streamerPlayer = context.getSource().asPlayer();
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
                ITextComponent text = MCPHelpers.fromJsonLenient(
                        String.format("{text:\"Testing %s action\", color:\"dark_purple\"}", TSLActionKeyword.ofClass(action.getClass())));
                STitlePacket packet = new STitlePacket(STitlePacket.Type.TITLE, text,
                        DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);

                ITextComponent subtext = MCPHelpers.fromJsonLenient(
                        String.format("{text:\"Rules traversed: %.02f%%\", color:\"dark_purple\"}", 100 * (index + 1f) / ruleset.getRulesRaw().size()));
                STitlePacket subtitlePacket = new STitlePacket(STitlePacket.Type.SUBTITLE, subtext,
                        DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);

                eventQueue.queue(() -> {
                    streamerPlayer.connection.sendPacket(packet);
                    streamerPlayer.connection.sendPacket(subtitlePacket);
                });
                eventQueue.queueSleep();
                eventQueue.queue(() -> action.process(eventArguments));
                eventQueue.queueSleep();

                index++;
            }

            eventQueue.updateThread();
        }

        TwitchSpawn.LOGGER.info("Tests queued for {}", streamerNick);
        context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.test.success", streamerNick), true);
        return 1;
    }

}
