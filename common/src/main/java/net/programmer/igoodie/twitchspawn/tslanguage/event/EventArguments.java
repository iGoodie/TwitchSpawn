package net.programmer.igoodie.twitchspawn.tslanguage.event;

import com.google.common.base.Defaults;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class EventArguments {

    public static EventArguments createRandom(String streamerNickname) {
        EventArguments eventArguments = new EventArguments(TSLEventKeyword.randomPair());

        eventArguments.streamerNickname = streamerNickname;
        eventArguments.randomize();

        return eventArguments;
    }

    /* ------------------------------ */

    public final String eventType;
    public final String eventAccount;
    public final String eventName;

    public String streamerNickname;
    public String actorNickname;
    public String message;

    public double donationAmount;
    public String donationCurrency;

    public int subscriptionMonths;
    public int subscriptionTier = -1; // 0=Prime, 1=T1, 2=T2, 3=T3
    public boolean gifted;

    public int viewerCount;
    public int raiderCount;

    public String rewardTitle;
    public Set<String> chatBadges;

    public EventArguments(String eventType, String eventAccount) {
        this.eventType = eventType;
        this.eventAccount = eventAccount;
        this.eventName = TSLEventKeyword.ofPair(eventType, eventAccount);
        this.chatBadges = new HashSet<>();
    }

    public EventArguments(TSLEventPair eventPair) {
        this(eventPair.getEventType(), eventPair.getEventAccount());
    }

    public void randomize() {
        randomize("RandomDude", "Random event message");
    }

    public void randomize(String actorNickname, String message) {
        Random random = new Random();

        this.actorNickname = actorNickname;
        this.message = message;
        this.donationAmount = random.nextDouble() * 1000;
        this.donationCurrency = new String[]{"USD", "TRY", "EUR"}[random.nextInt(3)];
        this.subscriptionMonths = random.nextInt(100 - 1) + 1;
        this.subscriptionTier = random.nextInt(3 + 1);
        this.gifted = random.nextBoolean();
        this.viewerCount = random.nextInt(100 - 1) + 1;
        this.raiderCount = random.nextInt(100 - 1) + 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";

        try {
            for (Field field : getClass().getFields()) {
                Object value = field.get(this);
                Object defaultValue = Defaults.defaultValue(field.getType());

                if (value == null)
                    continue;

                if (!value.equals(defaultValue)) {
                    sb.append(delimiter);
                    sb.append(field.getName()).append("=").append(value);
                    delimiter = ", ";
                }

                // Exception for tier field. (where default 0=Prime)
                else if (field.getName().equalsIgnoreCase("tier") && value.equals(0)) {
                    sb.append(delimiter);
                    sb.append(field.getName()).append("=").append(value);
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
