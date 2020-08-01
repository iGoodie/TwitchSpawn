package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WaitAction extends TSLAction {

    public static final Map<String, Long> UNIT_COEF = Stream.of(
            new AbstractMap.SimpleEntry<>("milliseconds", 1L),
            new AbstractMap.SimpleEntry<>("seconds", 1_000L),
            new AbstractMap.SimpleEntry<>("minutes", 60 * 1_000L)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private long waitTime;

    /*
     * WAIT <number> <time_unit>
     * 10 milliseconds
     * 10 seconds
     * 10 minutes
     */
    public WaitAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 2)
            throw new TSLSyntaxError("Expected two words, found %d instead", actionWords.size());

        int number = parseInt(actionWords.get(0));
        Long timeCoef = UNIT_COEF.get(actionWords.get(1));

        if (timeCoef == null)
            throw new TSLSyntaxError("Unexpected time unit -> %s", actionWords.get(1));

        this.waitTime = number * timeCoef;
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        EventQueue queue = ConfigManager.RULESET_COLLECTION.getQueue(args.streamerNickname);
        queue.queueSleepFirst(this.waitTime);
    }

}
