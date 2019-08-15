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
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StreamlabsSocketTracer extends SocketIOTracer {

    public Set<Socket> authorized;

    public StreamlabsSocketTracer(TraceManager manager) {
        super(Platform.STREAMLABS_SOCKET, manager);
        this.authorized = new HashSet<>();
    }

    @Override
    public void start() {
        TwitchSpawn.LOGGER.info("Starting Streamlabs Tracer...");

        ConfigManager.CREDENTIALS.streamers.forEach(this::createSocket);
        this.sockets.forEach(Socket::connect);
    }

    @Override
    public void stop() {
        TwitchSpawn.LOGGER.info("Stopping Streamlabs Tracer...");

        this.sockets.forEach(Socket::disconnect);
        this.sockets.clear();
    }

    @Override
    protected void checkCredentials(CredentialsConfig.Streamer streamer) {
        super.checkCredentials(streamer); // TODO
    }

    @Override
    protected IO.Options generateOptions(CredentialsConfig.Streamer streamer) {
        IO.Options options =  super.generateOptions(streamer);
        options.query = "token=" + streamer.token;
        return options;
    }

    @Override
    protected void onConnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        TwitchSpawn.LOGGER.info("Connected to Streamlabs Socket API with {}'s token successfully!", streamer.twitchNick);
        authorized.add(socket);
    }

    @Override
    protected void onDisconnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        TwitchSpawn.LOGGER.info("Disconnected from {}'s Streamlabs Socket connection. ({})",
                streamer.minecraftNick, authorized.contains(socket) ? "intentional" : "unauthorized");

        if(!authorized.contains(socket)) {
            manager.stop(null, streamer.twitchNick + " unauthorized by the socket server");
        }
    }

    @Override
    protected void onLiveEvent(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        JSONObject event = (JSONObject) args[0];

        if(!event.has("message") || event.optJSONArray("message") == null)
            return; // Contains no message (in expected format), stop here

        String responseType = JSONUtils.extractFrom(event, "type", String.class, null);
        String responseFor = JSONUtils.extractFrom(event, "for", String.class, "streamlabs");

        JSONArray messages = JSONUtils.extractFrom(event, "message", JSONArray.class, null);

        JSONUtils.forEach(messages, message -> {
            TwitchSpawn.LOGGER.info("Received streamlabs package {} -> {}",
                    new TSLEventPair(responseType, responseFor), message);

            // Unregistered event alias
            if (TSLEventKeyword.ofPair(responseType, responseFor) == null)
                return; // Stop here, do not handle

            // Refine incoming data into EventArguments model
            EventArguments eventArguments = new EventArguments(responseType, responseFor);
            eventArguments.streamerNickname = streamer.minecraftNick;
            eventArguments.actorNickname = JSONUtils.extractFrom(message, "name", String.class, null);
            eventArguments.message = JSONUtils.extractFrom(message, "message", String.class, null);
            eventArguments.donationAmount = JSONUtils.extractNumberFrom(message, "amount", 0.0).doubleValue();
            eventArguments.donationCurrency = JSONUtils.extractFrom(message, "currency", String.class, null);
            eventArguments.subscriptionMonths = JSONUtils.extractNumberFrom(message, "months", 0).intValue();
            eventArguments.raiderCount = JSONUtils.extractNumberFrom(message, "raiders", 0).intValue();
            eventArguments.viewerCount = JSONUtils.extractNumberFrom(message, "viewers", 0).intValue();

            // Pass the model to the handler
            ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments);
        });
    }

}
