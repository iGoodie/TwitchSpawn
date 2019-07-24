package net.programmer.igoodie.twitchspawn.tslanguage;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSLRules {

    private TSLTree defaultTree;
    private Map<String, TSLTree> streamerTrees; // Maps lowercase nicks to TSLTree

    public TSLRules(TSLTree defaultTree, List<TSLTree> streamerTrees) {
        if (defaultTree == null)
            throw new InternalError("Default tree must be not null");

        this.defaultTree = defaultTree;
        this.streamerTrees = new HashMap<>();

        for (TSLTree streamerTree : streamerTrees) {
            this.streamerTrees.put(streamerTree.getStreamer().toLowerCase(), streamerTree);
            TwitchSpawn.LOGGER.debug("Loaded TSL tree for {}", streamerTree.getStreamer());
        }
    }

    public boolean handleEvent(EventArguments args) {
        TwitchSpawn.LOGGER.info("Handling event {} for {}",
                args, args.streamerNickname);

        // Fetch TSLTree associated with the streamer
        TSLTree responsibleTree = streamerTrees.get(args.streamerNickname.toLowerCase());

        if (responsibleTree != null) {
            TwitchSpawn.LOGGER.info("Found associated tree for {}. Handling with their rules", args.streamerNickname);
            return responsibleTree.handleEvent(args);
        }

        // No tree found for the streamer
        TwitchSpawn.LOGGER.info("No associated tree for {} found. Handling with default rules", args.streamerNickname);
        return defaultTree.handleEvent(args);
    }

}
