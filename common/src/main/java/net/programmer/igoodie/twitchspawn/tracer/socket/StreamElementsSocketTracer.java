package net.programmer.igoodie.twitchspawn.tracer.socket;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class StreamElementsSocketTracer extends SocketIOTracer {

    public boolean authorized;

    public StreamElementsSocketTracer(TraceManager manager) {
        super(Platform.STREAMELEMENTS, manager);
        this.authorized = false;
    }

    @Override
    protected void onConnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        try {
            JSONObject authArguments = new JSONObject();
            authArguments.put("method", "jwt");
            authArguments.put("token", streamer.token);
            socket.emit("authenticate", authArguments);

        } catch (JSONException ignored) {} // Must be impossible

        socket.on("authenticated", foo -> {
            TwitchSpawn.LOGGER.info("Connected to StreamElements Socket API with {}'s token successfully!", streamer.twitchNick);
            authorized = true;
        });

        // TODO: refactor dis, duh :V
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (manager.isRunning() && !authorized) {
                    TwitchSpawn.LOGGER.info("Disconnected from {}'s StreamElements Socket connection. (unauthorized)", streamer.minecraftNick);
                    manager.stop(null, streamer.twitchNick + " unauthorized by the socket server");
                }
            }
        }, 5000);
    }

    @Override
    protected void onDisconnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        TwitchSpawn.LOGGER.info("Disconnected from {}'s StreamElements Socket connection. (intentional)", streamer.minecraftNick);
    }

    @Override
    protected void onLiveEvent(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        JSONObject event = (JSONObject) args[0];

        if (!event.has("data") || event.optJSONObject("data") == null) {
            TwitchSpawn.LOGGER.info("Received unexpected StreamElements packet -> {}", event);
            return; // Contains no data (in expected format), stop here
        }

        String eventType = JSONUtils.extractFrom(event, "type", String.class, null);
        String eventAccount = JSONUtils.extractFrom(event, "provider", String.class, "streamelements");
        TSLEventPair eventPair = new TSLEventPair(eventType, eventAccount);

        JSONObject data = JSONUtils.extractFrom(event, "data", JSONObject.class, new JSONObject());

        TwitchSpawn.LOGGER.info("Received StreamElements packet {} -> {}",
                eventPair, data);

        // Fetch the appropriate builder
        EventBuilder eventBuilder = TSLEventKeyword.getBuilder(eventPair);

        // Unregistered event alias
        if (eventBuilder == null)
            return; // Stop here, do not handle

        // Build arguments
        EventArguments eventArguments = eventBuilder.build(streamer, eventPair,
                data, Platform.STREAMELEMENTS);

        // Build failed for an unknown reason
        if (eventArguments == null) {
            TwitchSpawn.LOGGER.warn("{} was not able to build arguments from incoming data -> {}",
                    eventBuilder.getClass().getSimpleName(), data.toString());
            return;
        }

        // Pass the model to the handler
        ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments);
    }

}
