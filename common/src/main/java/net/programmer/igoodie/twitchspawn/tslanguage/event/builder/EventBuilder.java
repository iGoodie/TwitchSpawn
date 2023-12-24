package net.programmer.igoodie.twitchspawn.tslanguage.event.builder;

import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import net.programmer.igoodie.twitchspawn.util.TSHelper;
import org.json.JSONObject;

public abstract class EventBuilder {

    public <T> EventArguments
    build(CredentialsConfig.Streamer streamer, TSLEventPair eventPair, T rawData, Platform platform) {
        // Here as a fallback to ensure backwards compatibility
        if (rawData instanceof JSONObject) {
            if (platform == Platform.STREAMLABS) {
                return legacyStreamlabsBuilder(streamer, eventPair, (JSONObject) rawData);

            } else if (platform == Platform.STREAMELEMENTS) {
                return legacyStreamElementsBuilder(streamer, eventPair, (JSONObject) rawData);
            }
        }

        throw new InternalError(getClass().getSimpleName() + "::build() is not implemented...");
    }

    private EventArguments
    legacyStreamlabsBuilder(CredentialsConfig.Streamer streamer, TSLEventPair eventPair, JSONObject message) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = TSHelper.jslikeOr(
                JSONUtils.extractFrom(message, "name", String.class, null),
                JSONUtils.extractFrom(message, "from", String.class, null)
        );
        eventArguments.message = JSONUtils.extractFrom(message, "message", String.class, null);
        eventArguments.donationAmount = JSONUtils.extractNumberFrom(message, "amount", 0.0).doubleValue();
        eventArguments.donationCurrency = JSONUtils.extractFrom(message, "currency", String.class, null);
        eventArguments.subscriptionMonths = JSONUtils.extractNumberFrom(message, "months", 0).intValue();
        eventArguments.raiderCount = JSONUtils.extractNumberFrom(message, "raiders", 0).intValue();
        eventArguments.viewerCount = JSONUtils.extractNumberFrom(message, "viewers", 0).intValue();
        eventArguments.subscriptionTier = extractTier(message, "sub_plan");
        eventArguments.gifted = JSONUtils.extractFrom(message, "gifter_twitch_id", String.class, null) != null;
        eventArguments.rewardTitle = TSHelper.jslikeOr(
                JSONUtils.extractFrom(message, "redemption_name", String.class, null),
                JSONUtils.extractFrom(message, "product", String.class, null),
                JSONUtils.extractFrom(message, "title", String.class, null)
        );

        return eventArguments;
    }

    private EventArguments
    legacyStreamElementsBuilder(CredentialsConfig.Streamer streamer, TSLEventPair eventPair, JSONObject data) {
        EventArguments eventArguments = new EventArguments(eventPair);
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = JSONUtils.extractFrom(data, "username", String.class, null);
        eventArguments.message = JSONUtils.extractFrom(data, "message", String.class, null);
        eventArguments.donationAmount = JSONUtils.extractNumberFrom(data, "amount", 0.0).doubleValue();
        eventArguments.donationCurrency = JSONUtils.extractFrom(data, "currency", String.class, null);
        eventArguments.subscriptionMonths = JSONUtils.extractNumberFrom(data, "amount", 0).intValue();
//        eventArguments.raiderCount = JSONUtils.extractNumberFrom(message, "raiders", 0).intValue(); // Raids aren't supported (?)
        eventArguments.viewerCount = JSONUtils.extractNumberFrom(data, "amount ", 0).intValue();
        eventArguments.subscriptionTier = extractTier(data, "tier");
        // TODO: add gifted
        eventArguments.rewardTitle = JSONUtils.extractFrom(data, "redemption", String.class, null);

        return eventArguments;
    }


    protected int extractTier(JSONObject message, String tierFieldName) {
        String tierString = JSONUtils.extractFrom(message, tierFieldName, String.class, null);

        if (tierString == null)
            return -1;

        if (tierString.equalsIgnoreCase("Prime"))
            return 0; // tier = 0 stands for Prime

        if (tierString.equalsIgnoreCase("1000"))
            return 1;
        if (tierString.equalsIgnoreCase("2000"))
            return 2;
        if (tierString.equalsIgnoreCase("3000"))
            return 3;

        return -1; // Unknown tier String
    }

}
