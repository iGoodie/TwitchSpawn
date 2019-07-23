package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tracer.StreamlabsSocketClient;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.action.DropAction;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;

import java.util.List;

public class TwitchSpawnCommand {

    private static final String COMMAND_NAME = "twitchspawn";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal(COMMAND_NAME);

        root.then(Commands.literal("start").executes(TwitchSpawnCommand::startModule));
        root.then(Commands.literal("stop").executes(TwitchSpawnCommand::stopModule));

        root.then(Commands.literal("reloadcfg").executes(TwitchSpawnCommand::reloadModule));

//        root.then(Commands.literal("test")
//                .then(Commands.literal("drop")
//                        .then(CommandArguments.string("action_arguments")
//                                .executes(TwitchSpawnCommand::testDropModule)))
//                .then(Commands.literal("summon")
//                        .then(CommandArguments.string("action_arguments")
//                                .executes(TwitchSpawnCommand::testSummonModule)))
//                .then(Commands.literal("command")
//                        .then(CommandArguments.string("action_arguments")
//                                .executes(TwitchSpawnCommand::testCommandModule)))
//        );

        root.then(Commands.literal("debug")
                .then(Commands.literal("random_event").executes(TwitchSpawnCommand::debugRandomEventModule)));

        dispatcher.register(root);
    }

    /* ------------------------------------------------------------ */

    public static int startModule(CommandContext<CommandSource> context) {
        TwitchSpawn.LOGGER.info("Start module done!");

        StreamlabsSocketClient.start();

        return 1;
    }

    public static int stopModule(CommandContext<CommandSource> context) {
        TwitchSpawn.LOGGER.info("Stop module done!");

        StreamlabsSocketClient.stop("Command execution");

        return 1;
    }

    public static int reloadModule(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        if (StreamlabsSocketClient.isStarted()) {
            source.sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.already_started"), false);
            return 0;
        }

        try {
            ConfigManager.loadConfigs();
            source.sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.success"), false);
            return 1;

        } catch (TSLSyntaxErrors e) {
            source.sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.invalid_syntax", e.getErrors()), false);
            return 0;
        }
    }

    /* ------------------------------------------------------------ */

    public static int debugRandomEventModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        EventArguments eventArguments = new EventArguments();
        eventArguments.streamerNickname = sourceNickname;
        eventArguments.actorNickname = "TestUsername123";
        eventArguments.eventType = "donation";
        eventArguments.eventFor = "streamlabs";
        eventArguments.donationAmount = 100;

        return ConfigManager.HANDLING_RULES.handleEvent(eventArguments) ? 1 : 0;
    }

}
