package net.programmer.igoodie.twitchspawn.tslanguage.event.builder;

import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONObject;

import java.awt.*;

public class TwitchFollowBuilder extends EventBuilder {

    @Override
    public <T> EventArguments build(CredentialsConfig.Streamer streamer, TSLEventPair eventPair, T rawData, Platform platform) {
        if ((rawData instanceof JSONObject) && platform == Platform.STREAMLABS)
            return buildStreamlabsEvent(streamer, eventPair, (JSONObject) rawData);
        if ((rawData instanceof JSONObject) && platform == Platform.STREAMELEMENTS)
            return buildStreamElementsEvent(streamer, eventPair, (JSONObject) rawData);
        return null;
    }

    private EventArguments buildStreamlabsEvent
            (CredentialsConfig.Streamer streamer, TSLEventPair eventPair, JSONObject message) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = JSONUtils.extractFrom(message, "name", String.class, null);
        return eventArguments;
    }

    private EventArguments buildStreamElementsEvent
            (CredentialsConfig.Streamer streamer, TSLEventPair eventPair, JSONObject data) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = JSONUtils.extractFrom(data, "username", String.class, null);
        return eventArguments;
    }

}
