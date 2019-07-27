package net.programmer.igoodie.twitchspawn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tracer.StreamlabsSocketClient;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;

public class TwitchSpawnCommand {

    private static final String COMMAND_NAME = "twitchspawn";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal(COMMAND_NAME);

        root.then(Commands.literal("status").executes(TwitchSpawnCommand::statusModule));
        root.then(Commands.literal("start").executes(TwitchSpawnCommand::startModule));
        root.then(Commands.literal("stop").executes(TwitchSpawnCommand::stopModule));

        root.then(Commands.literal("reloadcfg").executes(TwitchSpawnCommand::reloadModule));

        // TODO: rule testing command
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

    public static int statusModule(CommandContext<CommandSource> context) {
        if (StreamlabsSocketClient.isRunning())
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.status.on"), false);
        else
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.status.off"), false);

        return 1;
    }

    public static int startModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        // If not command block and has no permission
        if (!sourceNickname.equals("@") && !ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.start.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to run TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        try {
            StreamlabsSocketClient.start();

        } catch (IllegalStateException e) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.start.illegal_state"), true);
            return 0;
        }

        return 1;
    }

    public static int stopModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        // If not command block and has no permission
        if (!sourceNickname.equals("@") && !ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.stop.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to stop TwitchSpawn, but no permission", sourceNickname);
            return 0;
        }

        try {
            StreamlabsSocketClient.stop(context.getSource(), "Command execution");

        } catch (IllegalStateException e) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.stop.illegal_state"), true);
            return 0;
        }

        return 1;
    }

    public static int reloadModule(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        String sourceNickname = source.getName();

        // If not command block and has no permission
        if (sourceNickname.equals("@") && !ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.no_perm"), true);
            TwitchSpawn.LOGGER.info("{} tried to reload TwitchSpawn configs, but no permission", sourceNickname);
            return 0;
        }

        if (StreamlabsSocketClient.isRunning()) {
            source.sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.already_started"), false);
            return 0;
        }

        try {
            ConfigManager.loadConfigs();
            source.sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.success"), false);
            return 1;

        } catch (TSLSyntaxErrors e) {
            source.sendFeedback(new TranslationTextComponent("commands.twitchspawn.reloadcfg.invalid_syntax",
                    e.getMessage()), false);
            return 0;
        }
    }

    /* ------------------------------------------------------------ */

    public static int debugRandomEventModule(CommandContext<CommandSource> context) {
        String sourceNickname = context.getSource().getName();

        // TODO: randomize event args
        EventArguments eventArguments = new EventArguments("donation", "streamlabs");
        eventArguments.streamerNickname = sourceNickname;
        eventArguments.actorNickname = "TestUsername123";
        eventArguments.donationAmount = 100;

        ConfigManager.HANDLING_RULES.handleEvent(eventArguments);
        return 1;
    }

}
