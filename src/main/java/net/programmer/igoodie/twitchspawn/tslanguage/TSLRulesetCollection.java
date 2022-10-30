package net.programmer.igoodie.twitchspawn.tslanguage;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.log.TSLogger;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;
import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TSLRulesetCollection {

    private TSLRuleset defaultRuleset;
    private Map<String, TSLRuleset> streamerRulesets; // Maps lowercase nicks to TSLTree
    private Map<String, EventQueue> eventQueues; // Maps lowercase nicks to TimeTaskQueue

    public TSLRulesetCollection(TSLRuleset defaultTree, List<TSLRuleset> streamerTrees) {
        if (defaultTree == null)
            throw new InternalError("Default tree must be not null");

        this.defaultRuleset = defaultTree;
        this.streamerRulesets = new HashMap<>();
        this.eventQueues = new HashMap<>();

        for (TSLRuleset streamerTree : streamerTrees) {
            this.streamerRulesets.put(streamerTree.getStreamer().toLowerCase(), streamerTree);
            TwitchSpawn.LOGGER.debug("Loaded TSL tree for {}", streamerTree.getStreamer());
        }
    }

    public boolean handleEvent(EventArguments args) {
        return handleEvent(args, null);
    }

    public boolean handleEvent(EventArguments args, CooldownBucket cooldownBucket) {
        // Fetch associated Ruleset
        TSLRuleset ruleset = getRuleset(args.streamerNickname);

        TwitchSpawn.getStreamerLogger(args.streamerNickname)
                .info("Handling (for {}) arguments {}", args.streamerNickname, args);

        // Fetch event pair and keyword
        TSLEventPair eventPair = new TSLEventPair(args.eventType, args.eventAccount);
        String eventKeyword = TSLEventKeyword.ofPair(eventPair);

        // Event pair is not known by TSL
        if (eventKeyword == null) {
            TwitchSpawn.getStreamerLogger(args.streamerNickname)
                    .info("Event pair not known by TSL -> {}. Skipped handling", eventPair);
            return false;
        }

        TwitchSpawn.getStreamerLogger(args.streamerNickname)
                .info(ruleset == defaultRuleset
                                ? "No associated ruleset for {} found. Handling with default rules"
                                : "Found associated ruleset for {}. Handling with their rules",
                        args.streamerNickname);

        // Fetch event handler node
        TSLEvent eventNode = ruleset.getEventHandler(eventKeyword);

        // No handler was bound, skip handling
        if (eventNode == null) {
            TwitchSpawn.getStreamerLogger(args.streamerNickname)
                    .info("No rule was found for {}. Skipped handling", eventKeyword);
            return false;
        }

        // Perform immediately instead of queueing
        if (eventNode.willPerform(args)) {
            TwitchSpawn.getStreamerLogger(args.streamerNickname)
                    .info("Begin performing  {} event.", eventKeyword);
            boolean succeeded = eventNode.process(args);
            TwitchSpawn.getStreamerLogger(args.streamerNickname)
                    .info("Finished performing {} event. (Success: {})", eventKeyword, succeeded);
        }

//        // Queue incoming event arguments
//        EventQueue eventQueue = getQueue(args.streamerNickname);
//        eventQueue.queue(eventNode, args, cooldownBucket);
//        eventQueue.queueSleep();
//        eventQueue.updateThread();
//        TwitchSpawn.getStreamerLogger(args.streamerNickname)
//                .info("Queued handler for {} event.", eventKeyword);

        // Perform event

        return true;
    }

    public boolean hasStreamer(String streamerNick) {
        return streamerRulesets.containsKey(streamerNick.toLowerCase());
    }

    public Set<String> getStreamers() {
        return streamerRulesets.keySet();
    }

    public TSLRuleset getRuleset(String streamerNick) {
        if (streamerNick.equalsIgnoreCase("default"))
            return defaultRuleset;

        if (!streamerRulesets.containsKey(streamerNick.toLowerCase()))
            return defaultRuleset;

        return streamerRulesets.get(streamerNick.toLowerCase());
    }

    public EventQueue getQueue(String streamerNick) {
        EventQueue queue = eventQueues.get(streamerNick.toLowerCase());

        if (queue == null) { // Lazy init
            queue = new EventQueue(ConfigManager.PREFERENCES.notificationDelay);
            eventQueues.put(streamerNick.toLowerCase(), queue);
        }

        return queue;
    }

    public void clearQueue() {
        eventQueues.values().forEach(EventQueue::reset);
    }

}
