package net.programmer.igoodie.twitchspawn.tracer.socket;

import io.socket.client.Socket;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tracer.SocketIOTracer;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
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
        JSONObject data = JSONUtils.extractFrom(event, "data", JSONObject.class, new JSONObject());

        TwitchSpawn.LOGGER.info("Received StreamElements packet {} -> {}",
                new TSLEventPair(eventType, eventAccount), data);

        // Unregistered event alias
        if (TSLEventKeyword.ofPair(eventType, eventAccount) == null)
            return; // Stop here, do not handle

        // Refine incoming data into EventArguments model
        EventArguments eventArguments = new EventArguments(eventType, eventAccount);
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

        // Pass the model to the handler
        ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments);
    }

}
