package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReflectAction extends TSLAction {

    private List<String> reflectedUsers;
    private TSLAction action;

    public ReflectAction(List<String> words) throws TSLSyntaxError {
        this.silent = true;

        if (words.size() < 2)
            throw new TSLSyntaxError("Invalid length of words: " + words);

        this.reflectedUsers = Arrays.asList(words.get(0).split(",\\s*"));
        this.action = TSLParser.parseAction(words.get(1), words.size() > 2
                ? words.subList(2, words.size())
                : Collections.emptyList());
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        action.reflectedUser = null;
        action.process(args);

        reflectedUsers.forEach(username -> {
            if(username.equalsIgnoreCase(args.streamerNickname)) {
                TwitchSpawn.LOGGER.warn("Tried to reflect back to the streamer. Skipping reflection for them.");
                return;
            }

            ServerPlayerEntity reflectedPlayer = getPlayer(username);

            if (reflectedPlayer == null) {
                TwitchSpawn.LOGGER.info("{} was not online on the server. Skipping reflection for them", username);
                return;
            }

            // If user is streamer, queue. Otherwise, do instantly
            if (ConfigManager.RULESET_COLLECTION.hasStreamer(username)) {
                ConfigManager.RULESET_COLLECTION.getQueue(username)
                        .queue(() -> reflectAction(reflectedPlayer, args));

            } else {
                reflectAction(reflectedPlayer, args);
            }
        });
    }

    private void reflectAction(ServerPlayerEntity player, EventArguments args) {
        action.reflectedUser = player;

        String title = action.titleMessage(args);
        String subtitle = action.subtitleMessage(args);

        action.notifyPlayer(player, title, subtitle);
        action.performAction(player, args);

        TwitchSpawn.LOGGER.info("Reflected {} action to {}", TSLActionKeyword.ofClass(action.getClass()), player);
    }

}
