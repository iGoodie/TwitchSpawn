package net.programmer.igoodie.twitchspawn.tslanguage;

import com.google.common.base.Defaults;

import java.lang.reflect.Field;

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

    /* ------------------------------ */

    public String eventType;
    public String eventFor;

    public String streamerNickname;
    public String actorNickname;
    public String message;

    public double donationAmount;
    public String donationCurrency;

    public int subscriptionMonths;

    public int viewerCount;
    public int raiderCount;

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
