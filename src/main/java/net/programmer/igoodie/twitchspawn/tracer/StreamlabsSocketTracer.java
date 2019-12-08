package net.programmer.igoodie.twitchspawn.tracer;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamlabsSocketTracer extends SocketIOTracer {

    public boolean authorized;

    public StreamlabsSocketTracer(TraceManager manager) {
        super(Platform.STREAMLABS, manager);
        this.authorized = false;
    }

    @Override
    protected IO.Options generateOptions(CredentialsConfig.Streamer streamer) {
        IO.Options options = super.generateOptions(streamer);
        options.query = "token=" + streamer.token;
        return options;
    }

    @Override
    protected void onConnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        TwitchSpawn.LOGGER.info("Connected to Streamlabs Socket API with {}'s token successfully!", streamer.twitchNick);
        authorized = true;
    }

    @Override
    protected void onDisconnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        TwitchSpawn.LOGGER.info("Disconnected from {}'s Streamlabs Socket connection. ({})",
                streamer.minecraftNick, authorized ? "intentional" : "unauthorized");

        if (manager.isRunning() && !authorized) { // TODO: concern what to do in this case?
            manager.stop(null, streamer.twitchNick + " unauthorized by the socket server");
        }
    }

    @Override
    protected void onLiveEvent(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        JSONObject event = (JSONObject) args[0];
        JSONArray messages = extractMessages(event);

        if (messages == null) {
            TwitchSpawn.LOGGER.info("Received unexpected Streamlabs packet -> {}", event);
            return; // Contains no message (in expected format), stop here
        }

        String eventType = JSONUtils.extractFrom(event, "type", String.class, null);
        String eventFor = JSONUtils.extractFrom(event, "for", String.class, "streamlabs");
        String eventAccount = eventFor.replace("_account", "");

        JSONUtils.forEach(messages, message -> {
            TwitchSpawn.LOGGER.info("Received Streamlabs packet {} -> {}",
                    new TSLEventPair(eventType, eventFor), message);

            // Unregistered event alias
            if (TSLEventKeyword.ofPair(eventType, eventAccount) == null)
                return; // Stop here, do not handle

            // Refine incoming data into EventArguments model
            EventArguments eventArguments = new EventArguments(eventType, eventAccount);
            eventArguments.streamerNickname = streamer.minecraftNick;
            eventArguments.actorNickname = JSONUtils.extractFrom(message, "name", String.class, null);
            eventArguments.message = JSONUtils.extractFrom(message, "message", String.class, null);
            eventArguments.donationAmount = JSONUtils.extractNumberFrom(message, "amount", 0.0).doubleValue();
            eventArguments.donationCurrency = JSONUtils.extractFrom(message, "currency", String.class, null);
            eventArguments.subscriptionMonths = JSONUtils.extractNumberFrom(message, "months", 0).intValue();
            eventArguments.raiderCount = JSONUtils.extractNumberFrom(message, "raiders", 0).intValue();
            eventArguments.viewerCount = JSONUtils.extractNumberFrom(message, "viewers", 0).intValue();
            eventArguments.subscriptionTier = extractTier(message, "sub_plan");
            eventArguments.gifted = JSONUtils.extractFrom(message, "gifter_twitch_id", String.class, null) != null;

            // Pass the model to the handler
            ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments);
        });
    }

    private JSONArray extractMessages(JSONObject event) {
        try {
            Object messageField = event.get("message");

            if (messageField instanceof JSONArray)
                return JSONUtils.extractFrom(event, "message", JSONArray.class, new JSONArray());

            else if (messageField instanceof JSONObject)
                return new JSONArray().put(messageField);

            return null;

        } catch (JSONException e) {
            return null;
        }
    }

}
