package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ReflectAction extends TSLAction {

    private boolean onlyReflectedPlayers;
    private boolean reflectEveryone;
    private int reflectRandomN;
    private List<String> reflectedUsers;
    private TSLAction action;

    public ReflectAction(List<String> words) throws TSLSyntaxError {
        this.silent = true;

        if (words.size() < 2)
            throw new TSLSyntaxError("Invalid length of words: " + words);

        // "REFLECT %% REFLECT %% ..." is not allowed
        String reflectActionName = TSLActionKeyword.ofClass(getClass());
        if (words.stream().anyMatch(word -> word.equalsIgnoreCase(reflectActionName)))
            throw new TSLSyntaxError("Cannot have a cyclic REFLECT rule.");

        LinkedList<String> wordsCloned = new LinkedList<>(words);

        if (wordsCloned.get(0).equalsIgnoreCase("ONLY")) {
            this.onlyReflectedPlayers = true;
            wordsCloned.remove(0);
        }

        String usersRaw = wordsCloned.get(0);

        if (usersRaw.equals("*")) {
            this.reflectEveryone = true;
            this.reflectRandomN = 0;
            this.reflectedUsers = null;

        } else if (usersRaw.matches("\\d+")) {
            this.reflectEveryone = false;
            this.reflectRandomN = parseInt(usersRaw);
            this.reflectedUsers = null;

        } else {
            this.reflectEveryone = false;
            this.reflectRandomN = 0;
            this.reflectedUsers = Arrays.asList(usersRaw.split(",\\s*"));
        }

        this.action = TSLParser.parseAction(wordsCloned.get(1), wordsCloned.size() > 2
                ? wordsCloned.subList(2, wordsCloned.size())
                : Collections.emptyList());
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        action.reflectedUser = null;

        if (onlyReflectedPlayers) {
            EventQueue eventQueue = ConfigManager.RULESET_COLLECTION.getQueue(args.streamerNickname);
            eventQueue.cancelUpcomingSleep();

        } else {
            action.process(args);
        }

        // Select where to reflect
        List<String> reflectedUsers = getReflectedPlayers(player);

        // Reflect to selected users
        reflectedUsers.forEach(username -> {
            if (username.equalsIgnoreCase(args.streamerNickname)) {
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

    private List<String> getReflectedPlayers(ServerPlayerEntity player) {
        if (this.reflectEveryone) {
            return Arrays.asList(player.getServer().getOnlinePlayerNames());
        }

        if (this.reflectRandomN != 0) {
            List<String> reflectedPlayers = new LinkedList<>();
            List<String> onlinePlayers = new LinkedList<>(Arrays.asList(player.getServer().getOnlinePlayerNames()));

            for (int i = 0; i < reflectRandomN; i++) {
                if (onlinePlayers.size() == 0) break;

                int index = (int) (Math.random() * onlinePlayers.size());
                String selectedPlayer = onlinePlayers.remove(index);
                reflectedPlayers.add(selectedPlayer);
            }

            return reflectedPlayers;
        }

        return this.reflectedUsers;
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
