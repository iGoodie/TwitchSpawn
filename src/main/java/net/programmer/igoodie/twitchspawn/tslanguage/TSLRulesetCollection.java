package net.programmer.igoodie.twitchspawn.tslanguage;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.time.TimeTaskQueue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TSLRulesetCollection {

    private static long TITLE_DURATION = 5 * 1000; // milliseconds

    private TSLRuleset defaultRuleset;
    private Map<String, TSLRuleset> streamerRulesets; // Maps lowercase nicks to TSLTree
    private TimeTaskQueue eventQueue;

    public TSLRulesetCollection(TSLRuleset defaultTree, List<TSLRuleset> streamerTrees) {
        if (defaultTree == null)
            throw new InternalError("Default tree must be not null");

        this.defaultRuleset = defaultTree;
        this.streamerRulesets = new HashMap<>();
        this.eventQueue = new TimeTaskQueue(TITLE_DURATION);

        for (TSLRuleset streamerTree : streamerTrees) {
            this.streamerRulesets.put(streamerTree.getStreamer().toLowerCase(), streamerTree);
            TwitchSpawn.LOGGER.debug("Loaded TSL tree for {}", streamerTree.getStreamer());
        }
    }

    public void handleEvent(EventArguments args) {
        // TODO: find a way to return boolean from the queue
        this.eventQueue.queue(() -> {
            TwitchSpawn.LOGGER.info("Handling event {} for {}",
                    args, args.streamerNickname);

            // Fetch associated Ruleset
            TSLRuleset ruleset = getRuleset(args.streamerNickname.toLowerCase());

            if (ruleset == defaultRuleset)
                TwitchSpawn.LOGGER.info("No associated ruleset for {} found. Handling with default rules", args.streamerNickname);
            else
                TwitchSpawn.LOGGER.info("Found associated ruleset for {}. Handling with their rules", args.streamerNickname);

            // Handle incoming event arguments
            ruleset.handleEvent(args);
        });
    }

    public Set<String> getStreamers() {
        return streamerRulesets.keySet();
    }

    public TSLRuleset getRuleset(String streamerNick) {
        if (streamerNick.equalsIgnoreCase("default"))
            return defaultRuleset;
        if (!streamerRulesets.containsKey(streamerNick))
            return defaultRuleset;
        return streamerRulesets.get(streamerNick);
    }

    public void queue(Runnable task) {
        this.eventQueue.queue(task);
    }

    public void cleanQueue() {
        eventQueue.cleanAll();
    }

}
