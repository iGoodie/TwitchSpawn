package net.programmer.igoodie.twitchspawn.tslanguage.keyword;

import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum TSLEventKeyword {

    DONATION(
            "Donation",
            new TSLEventPair("donation", ""), // This alias might be redundant (?)
            new TSLEventPair("donation", "streamlabs"),
            new TSLEventPair("tip", "twitch"),
            new TSLEventPair("tip", "youtube")
    ),
    LOYALTY_POINT_REDEMPTION(
            "Loyalty Point Redemption",
            new TSLEventPair("redemption", ""),
            new TSLEventPair("redemption", "streamlabs"),
            new TSLEventPair("loyalty_store_redemption", "streamlabs"),
            new TSLEventPair("redemption", "streamelements")
    ),
    TWITCH_FOLLOW(
            "Twitch Follow",
            new TSLEventPair("follow", "twitch")
    ),
    TWITCH_SUBSCRIPTION(
            "Twitch Subscription",
            new TSLEventPair("subscription", "twitch"),
            new TSLEventPair("subscriber", "twitch"),
            new TSLEventPair("resub", "twitch")
    ),
    TWITCH_SUBSCRIPTION_GIFT(
            "Twitch Subscription Gift",
            new TSLEventPair("subMysteryGift", "twitch")
    ),
    TWITCH_HOST(
            "Twitch Host",
            new TSLEventPair("host", "twitch")
    ),
    TWITCH_RAID(
            "Twitch Raid",
            new TSLEventPair("raid", "twitch")
    ),
    TWITCH_BITS(
            "Twitch Bits",
            new TSLEventPair("bits", "twitch"),
            new TSLEventPair("cheer", "twitch")
    ),
    TWITCH_CHANNEL_POINT_REWARD(
            "Twitch Channel Point Reward",
            new TSLEventPair("channelPointReward", "twitch")
    ),
    TWITCH_CHAT_MESSAGE(
            "Twitch Chat Message",
            new TSLEventPair("chat", "twitch")
    ),
    YOUTUBE_SUBSCRIPTION(
            "Youtube Subscription",
            new TSLEventPair("follow", "youtube"),
            new TSLEventPair("subscriber", "youtube")
    ),
    YOUTUBE_SPONSOR(
            "Youtube Sponsor",
            new TSLEventPair("subscription", "youtube"),
            new TSLEventPair("sponsor", "youtube")
    ),
    YOUTUBE_SUPERCHAT(
            "Youtube Superchat",
            new TSLEventPair("superchat", "youtube")
    ),
    MIXER_FOLLOW(
            "Mixer Follow",
            new TSLEventPair("follow", "mixer")
    ),
    MIXER_SUBSCRIPTION(
            "Mixer Subscription",
            new TSLEventPair("subscription", "mixer")
    ),
    MIXER_HOST(
            "Mixer Host",
            new TSLEventPair("host", "mixer")
    ),
    // Integration events
    JUSTGIVING_DONATION(
            "JustGiving Donation",
            new TSLEventPair("justgivingdonation", "justgiving")
    ),
    ;

    public static boolean exists(String eventName) {
        for (TSLEventKeyword keyword : values())
            if (keyword.eventName.equalsIgnoreCase(eventName))
                return true;
        return false;
    }

    public static String ofPair(String eventType, String eventFor) {
        return ofPair(new TSLEventPair(eventType, eventFor));
    }

    public static String ofPair(TSLEventPair eventPair) {
        for (TSLEventKeyword keyword : values())
            if (keyword.eventPairs.contains(eventPair))
                return keyword.eventName;
        return null;
    }

    public static Set<TSLEventPair> toPairs(String eventName) {
        for (TSLEventKeyword keyword : values())
            if (keyword.eventName.equalsIgnoreCase(eventName))
                return keyword.eventPairs;
        return null;
    }

    public static TSLEventPair randomPair() {
        TSLEventKeyword[] events = TSLEventKeyword.values();

        int randomIndex = (int) Math.floor(Math.random() * events.length);
        assert 0 <= randomIndex && randomIndex < events.length;

        return events[randomIndex].eventPairs.iterator().next();
    }

    /* ----------------------------------- */

    public final Set<TSLEventPair> eventPairs;
    public final String eventName;

    TSLEventKeyword(String eventName, TSLEventPair... eventPairs) {
        if (eventPairs.length == 0) throw new InternalError("Event keywords require at least one event pair!");

        this.eventPairs = new HashSet<>(Arrays.asList(eventPairs));
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return String.format("%s %s", eventName, eventPairs);
    }

}
