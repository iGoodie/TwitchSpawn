package net.programmer.igoodie.twitchspawn.tslanguage.event.builder;

import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONObject;

import java.awt.*;

public class ExtraLifeBuilder extends EventBuilder {

    @Override
    public <T> EventArguments build(CredentialsConfig.Streamer streamer, TSLEventPair eventPair, T rawData, Platform platform) {
        if ((rawData instanceof JSONObject) && platform == Platform.STREAMLABS)
            return buildStreamlabsEvent(streamer, eventPair, (JSONObject) rawData);
        if ((rawData instanceof JSONObject) && platform == Platform.STREAMELEMENTS)
            return null;
        return null;
    }

    private EventArguments buildStreamlabsEvent
            (CredentialsConfig.Streamer streamer, TSLEventPair eventPair, JSONObject message) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = JSONUtils.extractFrom(message, "from", String.class, null);
        eventArguments.message = JSONUtils.extractFrom(message, "message", String.class, null);
        eventArguments.donationAmount = JSONUtils.extractNumberFrom(message, "amount", 0.0).doubleValue();
        eventArguments.donationCurrency = JSONUtils.extractFrom(message, "currency", String.class, null);
        return eventArguments;
    }
}
