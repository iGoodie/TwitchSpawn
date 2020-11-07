package net.programmer.igoodie.twitchspawn.tslanguage.event;

import com.google.common.base.Defaults;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class EventArguments implements INBTSerializable<CompoundNBT> {

    public static EventArguments createRandom(String streamerNickname) {
        EventArguments eventArguments = new EventArguments(TSLEventKeyword.randomPair());

        eventArguments.streamerNickname = streamerNickname;
        eventArguments.randomize();

        return eventArguments;
    }

    /* ------------------------------ */

    public String eventType;
    public String eventAccount;
    public String eventName;

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

    public EventArguments() { }

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
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("eventType", eventType);
        nbt.putString("eventAccount", eventAccount);
        nbt.putString("eventName", eventName);

        nbt.putString("streamerNickname", streamerNickname);
        nbt.putString("actorNickname", actorNickname);
        if (message != null) nbt.putString("message", message);

        nbt.putDouble("donationAmount", donationAmount);
        if (donationCurrency != null) nbt.putString("donationCurrency", donationCurrency);

        nbt.putInt("subscriptionMonths", subscriptionMonths);
        nbt.putInt("subscriptionTier", subscriptionTier);
        nbt.putBoolean("gifted", gifted);

        nbt.putInt("viewerCount", viewerCount);
        nbt.putInt("raiderCount", raiderCount);

        if (rewardTitle != null) nbt.putString("rewardTitle", rewardTitle);
//        public Set<String> chatBadges;

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.eventType = nbt.getString("eventType");
        this.eventAccount = nbt.getString("eventAccount");
        this.eventName = nbt.getString("eventName");

        this.streamerNickname = nbt.getString("streamerNickname");
        this.actorNickname = nbt.getString("actorNickname");
        this.message = nbt.getString("message");

        this.donationAmount = nbt.getDouble("donationAmount");
        this.donationCurrency = nbt.getString("donationCurrency");

        this.subscriptionMonths = nbt.getInt("subscriptionMonths");
        this.subscriptionTier = nbt.getInt("subscriptionTier");
        this.gifted = nbt.getBoolean("gifted");

        this.viewerCount = nbt.getInt("viewerCount");
        this.raiderCount = nbt.getInt("raiderCount");

        this.rewardTitle = nbt.getString("rewardTitle");
//        public Set<String> chatBadges;
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
