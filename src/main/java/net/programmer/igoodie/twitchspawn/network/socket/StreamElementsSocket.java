package net.programmer.igoodie.twitchspawn.network.socket;

import io.socket.client.Socket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ClientCredentialsConfig;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.Platform;
import net.programmer.igoodie.twitchspawn.network.SocketManager;
import net.programmer.igoodie.twitchspawn.network.packet.EventPacket;
import net.programmer.igoodie.twitchspawn.network.socket.base.SocketIOBase;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.event.builder.EventBuilder;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class StreamElementsSocket extends SocketIOBase {

    protected boolean authorized;

    public StreamElementsSocket() {
        super(Platform.STREAMELEMENTS);
    }

    @Override
    public boolean validateCredentials(ClientCredentialsConfig config) {
        return config.platform != null
                && !config.platformToken.isEmpty();
    }

    @Override
    protected void onConnect(Socket socket, Object... args) {
        try {
            JSONObject authArguments = new JSONObject();
            authArguments.put("method", "jwt");
            authArguments.put("token", ConfigManager.CLIENT_CREDS.platformToken);
            socket.emit("authenticate", authArguments);

        } catch (JSONException ignored) {} // Must be impossible

        socket.on("authenticated", foo -> {
            TwitchSpawn.LOGGER.info("Connected to StreamElements Socket API with the token successfully!");
            authorized = true;
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (SocketManager.isRunning() && !authorized) {
                    TwitchSpawn.LOGGER.info("Disconnected from the StreamElements Socket connection. (unauthorized)");
                    SocketManager.stop();
                }
            }
        }, 5000);
    }

    @Override
    protected void onDisconnect(Socket socket, Object... args) {
        TwitchSpawn.LOGGER.info("Disconnected from the StreamElements Socket connection. (intentional)");
        SocketManager.stop();
    }

    /* -------------------- */

    @Override
    protected void bindEvents() {
        super.bindEvents();
        this.socket.on("event", args -> onEvent(socket, false, args));
        this.socket.on("event:test", args -> onEvent(socket, true, args));
    }

    protected void onEvent(Socket socket, boolean test, Object... args) {
        JSONObject event = (JSONObject) args[0];

//        if(!event.has("listener") || event.optString("listener"))
        // TODO: Talk to someone from SE... And continue afterwards

        if (!event.has("data") || event.optJSONObject("data") == null) {
            TwitchSpawn.LOGGER.info("Received unexpected {} StreamElements packet -> {}", test ? "Test" : "Live", event);
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

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // Build arguments
        EventArguments eventArguments = eventBuilder.build(
                player == null ? "Dev" : player.getDisplayName().getString(),
                eventPair,
                data,
                Platform.STREAMELEMENTS);

        // Build failed for an unknown reason
        if (eventArguments == null) {
            TwitchSpawn.LOGGER.warn("{} was not able to build arguments from incoming data -> {}",
                    eventBuilder.getClass().getSimpleName(), data.toString());
            return;
        }

        // Pass the model to the handler
        NetworkManager.CHANNEL.sendToServer(
                new EventPacket(eventArguments)
        );
    }

}
