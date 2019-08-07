package net.programmer.igoodie.twitchspawn.tslanguage.keyword;

import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;

public enum TSLEventKeyword {

    STREAMLABS_DONATION(
            "donation", "streamlabs",
            "Streamlabs Donation"
    ),
    TWITCH_FOLLOW(
            "follow", "twitch_account",
            "Twitch Follow"
    ),
    TWITCH_SUBSCRIPTION(
            "subscription", "twitch_account",
            "Twitch Subscription"
    ),
    TWITCH_HOST(
            "host", "twitch_account",
            "Twitch Host"
    ),
    TWITCH_RAID(
            "raid", "twitch_account",
            "Twitch Raid"
    ),
    TWITCH_BITS(
            "bits", "twitch_account",
            "Twitch Bits"
    ),
    YOUTUBE_FOLLOW(
            "follow", "youtube_account",
            "Youtube Follow"
    ),
    YOUTUBE_SPONSOR(
            "sponsor", "youtube_account",
            "Youtube Sponsor"
    ),
    YOUTUBE_SUPERCHAT(
            "superchat", "youtube_account",
            "Youtube Superchat"
    ),
    MIXER_FOLLOW(
            "follow", "mixer_account",
            "Mixer Follow"
    ),
    MIXER_SUBSCRIPTION(
            "subscription", "mixer_account",
            "Mixer Subscription"
    ),
    MIXER_HOST(
            "host", "mixer_account",
            "Mixer Host"
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
            if (keyword.eventPair.equals(eventPair))
                return keyword.eventName;
        return null;
    }

    public static TSLEventPair toPair(String eventName) {
        for (TSLEventKeyword keyword : values())
            if (keyword.eventName.equalsIgnoreCase(eventName))
                return keyword.eventPair;
        return null;
    }

    public static TSLEventPair randomPair() {
        TSLEventKeyword[] events = TSLEventKeyword.values();

        int randomIndex = (int) Math.floor(Math.random() * events.length);
        assert 0 <= randomIndex && randomIndex < events.length;

        return events[randomIndex].eventPair;
    }

    /* ----------------------------------- */

    public final TSLEventPair eventPair;
    public final String eventName;

    TSLEventKeyword(String eventType, String eventFor, String eventName) {
        this.eventPair = new TSLEventPair(eventType, eventFor);
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return String.format("%s %s", eventName, eventPair);
    }

}
