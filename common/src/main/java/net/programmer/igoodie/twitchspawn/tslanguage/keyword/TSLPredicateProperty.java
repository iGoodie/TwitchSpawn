package net.programmer.igoodie.twitchspawn.tslanguage.keyword;

import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum TSLPredicateProperty {

    ACTOR(
            "actorNickname",
            "actor"
    ),
    MESSAGE(
            "message",
            "message"
    ),
    CURRENCY(
            "donationCurrency",
            "currency", "donation_currency"
    ),
    AMOUNT(
            "donationAmount",
            "amount", "donation_amount"
    ),
    MONTHS(
            "subscriptionMonths",
            "months", "subscription_months"
    ),
    TIER(
            "subscriptionTier",
            "tier", "subscription_tier"
    ),
    GIFTED(
            "gifted",
            "gifted"
    ),
    VIEWERS(
            "viewerCount",
            "viewers", "viewer_count"
    ),
    RAIDERS(
            "raiderCount",
            "raiders", "raider_count"
    ),
    REWARD_TITLE(
            "rewardTitle",
            "title", "reward_title"
    ),
    CHAT_BADGES(
            "chatBadges",
            "badges", "chat_badges"
    );

    public static boolean exists(String tslField) {
        for (TSLPredicateProperty property : values()) {
            if (property.tslAliases.contains(tslField.toLowerCase()))
                return true;
        }
        return false;
    }

    public static Field ofTSLField(String tslField) {
        for (TSLPredicateProperty property : values()) {
            if (property.tslAliases.contains(tslField.toLowerCase()))
                return property.eventArgumentField;
        }
        return null;
    }

    public static Object extractFrom(EventArguments args, String tslField) {
        Field field = ofTSLField(tslField);

        if (field == null)
            throw new InternalError("Unknown tsl predicate field -> " + tslField);

        try {
            return field.get(args);

        } catch (IllegalAccessException e) {
            throw new InternalError("Unknown tsl predicate field -> " + tslField);
        }
    }

    /* ------------------------- */

    public final Set<String> tslAliases;
    public final Field eventArgumentField;

    TSLPredicateProperty(String eventArgumentFieldName, String... tslAliases) {
        this.eventArgumentField = getEventArgumentField(eventArgumentFieldName);
        this.tslAliases = new HashSet<>(Arrays.asList(tslAliases));
    }

    private Field getEventArgumentField(String fieldName) {
        try {
            return EventArguments.class.getField(fieldName);

        } catch (NoSuchFieldException e) {
            throw new InternalError("Tried to fetch a non-existing argument field -> " + fieldName);
        }
    }


}
