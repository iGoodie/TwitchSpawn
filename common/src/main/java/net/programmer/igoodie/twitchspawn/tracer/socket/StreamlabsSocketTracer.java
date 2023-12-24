package net.programmer.igoodie.twitchspawn.tracer.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tracer.SocketIOTracer;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.event.builder.EventBuilder;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import net.programmer.igoodie.twitchspawn.util.TSHelper;
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
        TSLEventPair eventPair = new TSLEventPair(eventType, eventFor.replace("_account", ""));

        JSONUtils.forEach(messages, message -> {
            TwitchSpawn.LOGGER.info("Received Streamlabs packet {} -> {}",
                    new TSLEventPair(eventType, eventFor), message);

            // Fetch the appropriate builder
            EventBuilder eventBuilder = TSLEventKeyword.getBuilder(eventPair);

            // Unregistered event alias
            if (eventBuilder == null)
                return; // Stop here, do not handle

            // Build arguments
            EventArguments eventArguments = eventBuilder.build(streamer, eventPair,
                    message, Platform.STREAMLABS);

            // Build failed for an unknown reason
            if (eventArguments == null) {
                TwitchSpawn.LOGGER.warn("{} was not able to build arguments from incoming data -> {}",
                        eventBuilder.getClass().getSimpleName(), message.toString());
                return;
            }

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
