package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLRuleset;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.TimeTaskQueue;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ModuleTest extends CommandModule {

    private static final int DEFAULT_FADE_IN_TICKS = 10;
    private static final int DEFAULT_STAY_TICKS = 70;
    private static final int DEFAULT_FADE_OUT_TICKS = 20;

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getUsage() {
        return super.getUsage() + " <streamer>";
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(String[] moduleArgs) {
        if (moduleArgs.length == 1)
            return listOfCompletionsStartingWith(moduleArgs,
                    ConfigManager.RULESET_COLLECTION.getStreamers());

        return Collections.emptyList();
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String streamerNickname = getArgument(moduleArgs, 0);

        if (streamerNickname == null)
            throw new WrongUsageException(getUsage());

        if (!ConfigManager.RULESET_COLLECTION.hasStreamer(streamerNickname)) {
            TwitchSpawn.LOGGER.info("There are no ruleset associated with {}", streamerNickname);
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.test.not_found", streamerNickname));
            return;
        }

        EntityPlayerMP streamerPlayer = (EntityPlayerMP) commandSender.getCommandSenderEntity();
        TSLRuleset ruleset = ConfigManager.RULESET_COLLECTION.getRuleset(streamerNickname);
        TimeTaskQueue queue = ConfigManager.RULESET_COLLECTION.getQueue(streamerNickname);

        Collection<TSLEvent> events = ruleset.getEvents();
        Iterator<TSLEvent> eventIterator = events.iterator();
        TSLEvent event;
        int index = 0;

        while (eventIterator.hasNext()) {
            event = eventIterator.next();

            TSLEventPair eventPair = TSLEventKeyword.toPairs(event.getName()).iterator().next();
            EventArguments eventArguments = new EventArguments(eventPair);
            eventArguments.randomize();
            eventArguments.streamerNickname = streamerPlayer.getName();
            eventArguments.actorNickname = "TesterKid";

            for (TSLAction action : event.getActions()) {
                ITextComponent text = ITextComponent.Serializer.fromJsonLenient(
                        String.format("{text:\"Testing %s action\", color:\"dark_purple\"}", TSLActionKeyword.ofClass(action.getClass())));
                SPacketTitle packet = new SPacketTitle(SPacketTitle.Type.TITLE, text,
                        DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);

                ITextComponent subtext = ITextComponent.Serializer.fromJsonLenient(
                        String.format("{text:\"Rules traversed: %.02f%%\", color:\"dark_purple\"}", 100 * (index + 1f) / ruleset.getRulesRaw().size()));
                SPacketTitle subtitlePacket = new SPacketTitle(SPacketTitle.Type.SUBTITLE, subtext,
                        DEFAULT_FADE_IN_TICKS, DEFAULT_STAY_TICKS, DEFAULT_FADE_OUT_TICKS);

                queue.queue(() -> {
                    streamerPlayer.connection.sendPacket(packet);
                    streamerPlayer.connection.sendPacket(subtitlePacket);
                });
                queue.queue(() -> action.process(eventArguments));

                index++;
            }
        }

        TwitchSpawn.LOGGER.info("Tests queued for {}", streamerNickname);
        commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.test.success", streamerNickname));
    }

}
