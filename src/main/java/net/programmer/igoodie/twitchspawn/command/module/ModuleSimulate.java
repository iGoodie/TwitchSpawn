package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ModuleSimulate extends CommandModule {

    @Override
    public String getName() {
        return "simulate";
    }

    @Override
    public String getUsage() {
        return super.getUsage() + " <event_simulation_json> [<streamer_nick>]";
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(String[] moduleArgs) {
        if (moduleArgs.length == 1)
            return Collections.singletonList("{");

        String lastArgument = moduleArgs[moduleArgs.length - 1];

        if (!lastArgument.contains("{") && !lastArgument.contains("}"))
            return listOfCompletionsStartingWith(moduleArgs,
                    ConfigManager.RULESET_COLLECTION.getStreamers());

        return Collections.emptyList();
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String senderNickname = commandSender.getName();
        String rawNbt = getFirstNBT(moduleArgs);
        String streamerNickname = getStreamerUsername(moduleArgs);

        if (rawNbt == null)
            throw new WrongUsageException(getUsage());

        if (!ConfigManager.CREDENTIALS.hasPermission(senderNickname)) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.simulate.no_perm"));
            TwitchSpawn.LOGGER.info("{} tried to simulate an event, but no permission", senderNickname);
            return;
        }

        try {
            NBTTagCompound nbt = JsonToNBT.getTagFromJson(rawNbt);

            String eventName = nbt.getString("event");

            if (eventName.isEmpty()) {
                commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.simulate.missing"));
                return;
            }

            Set<TSLEventPair> eventPairs = TSLEventKeyword.toPairs(eventName);

            if (eventPairs == null) {
                commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.simulate.invalid_event", eventName));
                return;
            }

            TSLEventPair eventPair = eventPairs.iterator().next();

            boolean random = nbt.getBoolean("random");

            EventArguments simulatedEvent = new EventArguments(eventPair.getEventType(), eventPair.getEventAccount());
            simulatedEvent.streamerNickname = streamerNickname != null ? streamerNickname : senderNickname;

            if (random) {
                simulatedEvent.randomize("SimulatorDude", "Simulating a message");

            } else {
                simulatedEvent.actorNickname = nbt.getString("actor").isEmpty() ? "SimulatorDude" : nbt.getString("actor");
                simulatedEvent.message = nbt.hasKey("message") ? nbt.getString("message") : "Simulating a message";
                simulatedEvent.donationAmount = nbt.getDouble("amount");
                simulatedEvent.donationCurrency = nbt.getString("currency");
                simulatedEvent.subscriptionMonths = nbt.getInteger("months");
                simulatedEvent.raiderCount = nbt.getInteger("raiders");
                simulatedEvent.viewerCount = nbt.getInteger("viewers");
                simulatedEvent.subscriptionTier = nbt.hasKey("tier", 3) ? nbt.getInteger("tier") : -1;
                simulatedEvent.gifted = nbt.getBoolean("gifted");
                simulatedEvent.rewardTitle = nbt.getString("title");
            }

            ConfigManager.RULESET_COLLECTION.handleEvent(simulatedEvent);

            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.simulate.success", nbt));

        } catch (Exception e) {
            throw new WrongUsageException(getUsage());
        }
    }

    private String getFirstNBT(String[] moduleArgs) {
        int startIndex = -1;
        int finishIndex = -1;

        // Detect starting index by forward traversing
        for (int i = 0; i < moduleArgs.length; i++) {
            if (moduleArgs[i].contains("{")) {
                startIndex = i;
                break;
            }
        }

        // Detect finishing index by reverse traversing
        for (int i = moduleArgs.length - 1; i >= 0; i--) {
            if (moduleArgs[i].contains("}")) {
                finishIndex = i;
                break;
            }
        }

        if (startIndex == -1 || finishIndex == -1)
            return null;

        StringBuilder nbtRaw = new StringBuilder();

        for (int i = startIndex; i <= finishIndex; i++) {
            nbtRaw.append(moduleArgs[i]).append(" ");
        }

        return (nbtRaw.length() == 0) ? null : nbtRaw.toString();
    }

    private String getStreamerUsername(String[] moduleArgs) {
        if (moduleArgs.length == 0)
            return null;

        String lastArgument = moduleArgs[moduleArgs.length - 1];

        if (lastArgument.contains("{") || lastArgument.contains("}"))
            return null;

        return lastArgument;
    }

}
