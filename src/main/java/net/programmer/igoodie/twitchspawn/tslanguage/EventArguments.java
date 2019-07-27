package net.programmer.igoodie.twitchspawn.tslanguage;

import com.google.common.base.Defaults;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLPredicate;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.Set;

public class EventArguments {

    /**
     * Used by {@link net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLPredicate}
     * to map TSL predicate field keys to EventArguments class fields.
     * <b>NOT INTENDED FOR EXTERNAL USE</b>
     *
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

    public static EventArguments createRandom(String streamerNickname) {
        Random random = new Random();

        EventArguments eventArguments = new EventArguments(randomPair());
        eventArguments.streamerNickname = streamerNickname;
        eventArguments.actorNickname = "RandomActor";
        eventArguments.message = "Random event message";
        eventArguments.donationAmount = random.nextDouble();
        eventArguments.donationCurrency = new String[]{"USD", "TRY", "EUR"}[random.nextInt(3)];
        eventArguments.subscriptionMonths = random.nextInt();
        eventArguments.viewerCount = random.nextInt();
        eventArguments.raiderCount = random.nextInt();

        return eventArguments;
    }

    private static TSLEventPair randomPair() {
        Set<TSLEventPair> eventPairs = TSLEvent.EVENT_NAME_ALIASES.keySet();
        int index = (int) Math.floor(Math.random() * eventPairs.size());

        assert 0 <= index && index < eventPairs.size();

        for (TSLEventPair pair : eventPairs) {
            if ((index--) == 0)
                return pair;
        }

        return null; // Impossible to reach here
    }

    /* ------------------------------ */

    public final String eventType;
    public final String eventFor;
    public final String eventAlias;

    public String streamerNickname;
    public String actorNickname;
    public String message;

    public double donationAmount;
    public String donationCurrency;

    public int subscriptionMonths;

    public int viewerCount;
    public int raiderCount;

    public EventArguments(String eventType, String eventFor) {
        this.eventType = eventType;
        this.eventFor = eventFor;
        this.eventAlias = TSLEvent.getEventAlias(eventType, eventFor);
    }

    public EventArguments(TSLEventPair eventPair) {
        this(eventPair.getEventType(), eventPair.getEventFor());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";

        try {
            for (Field field : getClass().getFields()) {
                Object value = field.get(this);
                Object defaultValue = Defaults.defaultValue(field.getType());

                if (value != null && !value.equals(defaultValue)) {
                    sb.append(delimiter);
                    sb.append(field.getName() + "=" + value);
                    delimiter = ", ";
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        sb.append("}");

        return sb.toString();
    }

}
