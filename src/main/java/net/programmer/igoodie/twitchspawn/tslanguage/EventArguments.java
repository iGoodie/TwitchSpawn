package net.programmer.igoodie.twitchspawn.tslanguage;

import java.lang.reflect.Field;

public class EventArguments {

    /**
     * Used by {@link net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLPredicate}
     * to map TSL predicate field keys to EventArguments class fields.
     * <b>NOT INTENDED FOR EXTERNAL USE</b>
     * @param fieldName Name of the field
     * @return Java Reflection field with given name
     */
    public static Field getField(String fieldName) {
        try {
            return EventArguments.class.getField(fieldName);

        } catch (NoSuchFieldException e) {
            throw new InternalError("Tried to fetch a non-existing argument field -> " + fieldName);
        }
    }

    /* ------------------------------ */

    public String eventType;
    public String eventFor;

    public String streamerNickname;
    public String actorNickname;
    public String message;

    public float donationAmount;
    public String donationCurrency;

    public int subscriptionMonths;

    public int viewerCount;
    public int raiderCount;

}
