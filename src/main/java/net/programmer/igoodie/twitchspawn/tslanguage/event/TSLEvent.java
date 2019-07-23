package net.programmer.igoodie.twitchspawn.tslanguage.event;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

import java.util.*;

public class TSLEvent implements TSLFlowNode {

    public static Map<TSLEventPair, String> EVENT_NAME_ALIASES;

    public static void loadEventAliases() {
        EVENT_NAME_ALIASES = new HashMap<>();

        registerEventName("donation", "streamlabs", "streamlabs donation");

        registerEventName("follow", "twitch_account", "twitch follow");
        registerEventName("subscription", "twitch_account", "twitch subscription");
        registerEventName("host", "twitch_account", "twitch host");
        registerEventName("bits", "twitch_account", "twitch bits");
        registerEventName("raids", "twitch_account", "twitch raid");

        registerEventName("follow", "youtube_account", "youtube subscription");
        registerEventName("subscription", "youtube_account", "youtube sponsor");
        registerEventName("superchat", "youtube_account", "youtube superchat");

        registerEventName("follow", "mixer_account", "mixer follow");
        registerEventName("subscription", "mixer_account", "mixer subscription");
        registerEventName("host", "mixer_account", "mixer host");
    }

    private static void registerEventName(String eventType, String eventFor, String tslAlias) {
        TSLEventPair pair = new TSLEventPair(eventType, eventFor);

        if (EVENT_NAME_ALIASES.containsKey(pair))
            throw new InternalError("Tried to register an already existing event pair -> "
                    + String.format("(%s, %s)", eventType, eventFor));

        EVENT_NAME_ALIASES.put(pair, tslAlias);

        TwitchSpawn.LOGGER.debug("Registered Event name alias {} -> {}", pair, tslAlias);
    }

    public static String getEventAlias(String eventType, String eventFor) {
        return EVENT_NAME_ALIASES.get(new TSLEventPair(eventType, eventFor));
    }

    /* ----------------------------------- */

    protected List<TSLFlowNode> nextNodes;

    public TSLEvent() {
        this.nextNodes = new LinkedList<>();
    }

    @Override
    public TSLFlowNode chain(TSLFlowNode next) {
        nextNodes.add(next);
        return next;
    }

    @Override
    public boolean process(EventArguments args) {
        Iterator<TSLFlowNode> iterator = nextNodes.iterator();
        boolean success = false;

        while (iterator.hasNext()) {
            success |= iterator.next().process(args);
        }

        return success;
    }

}

