package net.programmer.igoodie.twitchspawn.tslanguage.event.builder;

import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.Platform;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONObject;

public class DonationBuilder extends EventBuilder {

    @Override
    public <T> EventArguments build(String minecraftNick, TSLEventPair eventPair, T rawData, Platform platform) {
        if ((rawData instanceof JSONObject) && platform == Platform.STREAMLABS)
            return buildStreamlabsEvent(minecraftNick, eventPair, (JSONObject) rawData);
        if ((rawData instanceof JSONObject) && platform == Platform.STREAMELEMENTS)
            return buildStreamElementsEvent(minecraftNick, eventPair, (JSONObject) rawData);
        return null;
    }

    private EventArguments buildStreamlabsEvent
            (String minecraftNick, TSLEventPair eventPair, JSONObject message) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = minecraftNick;
        eventArguments.actorNickname = JSONUtils.extractFrom(message, "name", String.class, null);
        eventArguments.message = JSONUtils.extractFrom(message, "message", String.class, null);
        eventArguments.donationAmount = JSONUtils.extractNumberFrom(message, "amount", 0.0).doubleValue();
        eventArguments.donationCurrency = JSONUtils.extractFrom(message, "currency", String.class, null);

        return eventArguments;
    }

    private EventArguments buildStreamElementsEvent
            (String minecraftNick, TSLEventPair eventPair, JSONObject data) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = minecraftNick;
        eventArguments.actorNickname = JSONUtils.extractFrom(data, "username", String.class, null);
        eventArguments.message = JSONUtils.extractFrom(data, "message", String.class, null);
        eventArguments.donationAmount = JSONUtils.extractNumberFrom(data, "amount", 0.0).doubleValue();
        eventArguments.donationCurrency = JSONUtils.extractFrom(data, "currency", String.class, null);

        return eventArguments;
    }

}
