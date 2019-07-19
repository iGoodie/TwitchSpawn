package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.action.DropAction;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

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

        dispatcher.register(root);
    }

    /* ------------------------------------------------------------ */

    public static int startModule(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TwitchSpawn.LOGGER.info("Start module done!");
        return 1;
    }

    public static int stopModule(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TwitchSpawn.LOGGER.info("Stop module done!");
        return 1;
    }

    public static int reloadModule(CommandContext<CommandSource> context) throws CommandSyntaxException {
        CommandSource source = context.getSource();

        ConfigManager.loadConfigs();

        source.sendFeedback(new TranslationTextComponent("commands.streamspawn.reloadcfg.success"), false);
        return 1;
    }

    /* ------------------------------------------------------------ */


}
