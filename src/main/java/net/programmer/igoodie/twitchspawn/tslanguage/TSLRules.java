package net.programmer.igoodie.twitchspawn.tslanguage;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.time.TimeTaskQueue;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSLRules {

    private static long TITLE_DURATION = 5 * 1000; // milliseconds

    private TSLTree defaultTree;
    private Map<String, TSLTree> streamerTrees; // Maps lowercase nicks to TSLTree
    private TimeTaskQueue eventQueue;

    public TSLRules(TSLTree defaultTree, List<TSLTree> streamerTrees) {
        if (defaultTree == null)
            throw new InternalError("Default tree must be not null");

        this.defaultTree = defaultTree;
        this.streamerTrees = new HashMap<>();
        this.eventQueue = new TimeTaskQueue(TITLE_DURATION);

        for (TSLTree streamerTree : streamerTrees) {
            this.streamerTrees.put(streamerTree.getStreamer().toLowerCase(), streamerTree);
            TwitchSpawn.LOGGER.debug("Loaded TSL tree for {}", streamerTree.getStreamer());
        }
    }

    public void handleEvent(EventArguments args) {
        // TODO: find a way to return boolean from the queue
        this.eventQueue.queue(() -> {
            TwitchSpawn.LOGGER.info("Handling event {} for {}",
                    args, args.streamerNickname);

            // Fetch TSLTree associated with the streamer
            TSLTree responsibleTree = streamerTrees.get(args.streamerNickname.toLowerCase());

            // TODO: Collect predicate passing TSLAction nodes
            // TODO: Select random one to perform

            if (responsibleTree != null) {
                TwitchSpawn.LOGGER.info("Found associated tree for {}. Handling with their rules", args.streamerNickname);
                responsibleTree.handleEvent(args);
                return;
            }

            // No tree found for the streamer
            TwitchSpawn.LOGGER.info("No associated tree for {} found. Handling with default rules", args.streamerNickname);
            defaultTree.handleEvent(args);
        });
    }

}
