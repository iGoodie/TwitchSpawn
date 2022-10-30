package net.programmer.igoodie.twitchspawn.network.socket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.Platform;
import net.programmer.igoodie.twitchspawn.network.SocketManager;
import net.programmer.igoodie.twitchspawn.network.packet.EventPacket;
import net.programmer.igoodie.twitchspawn.network.socket.base.WebSocketBase;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class TwitchPubSubSocket extends WebSocketBase {

    protected Timer pingScheduler;

    public TwitchPubSubSocket() {
        super(Platform.TWITCH_PUBSUB);
    }

    @Override
    public void stop() {
        super.stop();

        if (pingScheduler != null) {
            pingScheduler.cancel();
            pingScheduler.purge();
            pingScheduler = null;
        }
    }

    /* ----------------------------- */

    protected JSONObject createAuthJSON(String twitchNick) {
        try {
            String userID = nicknameToIDSync(twitchNick);

            JSONObject authentication = new JSONObject();
            authentication.put("type", "LISTEN");

            JSONObject data = new JSONObject();
            Collection<String> topics = new ArrayList<>();
            topics.add("community-points-channel-v1." + userID);
            data.put("topics", topics);
            authentication.put("data", data);

            return authentication;

        } catch (JSONException e) {
            throw new InternalError("TODO: Error message");
        }
    }

    protected String nicknameToIDSync(String nickname) {
        try {
            URL url = new URL("https://decapi.me/twitch/id/" + nickname);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String userID = reader.readLine();

            reader.close();

            return userID;

        } catch (IOException e) {
            // TODO: Unknown username too :p
            throw new InternalError("TODO: Error message");
        }
    }

    protected JSONObject createPingJSON() {
        try {
            JSONObject ping = new JSONObject();
            ping.put("type", "PING");
            return ping;

        } catch (JSONException e) {
            throw new InternalError("TODO: Error message");
        }
    }

    /* ----------------------------- */

    @Override
    public void onOpen(@Nonnull WebSocket socket, @Nonnull Response response) {
        super.onOpen(socket, response);

        JSONObject authJSON = createAuthJSON(ConfigManager.CLIENT_CREDS.twitchNickname);
        webSocket.send(authJSON.toString());

        pingScheduler = new Timer();
        pingScheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                webSocket.send(createPingJSON().toString());
            }
        }, 0, 4 * 60 * 1000);
    }

    @Override
    public void onClosing(@Nonnull WebSocket webSocket, int code, @Nonnull String reason) {
        SocketManager.stop();
    }

    @Override
    public void onMessage(@Nonnull WebSocket socket, @Nonnull String text) {

        try {
            JSONObject json = new JSONObject(text);

            TwitchSpawn.LOGGER.info("Received Twitch PubSub packet {} -> {}",
                    ConfigManager.CLIENT_CREDS.twitchNickname, json);

            if (json.getString("type").equals("RESPONSE")) {
                if (json.has("error")) {
                    // TODO: Do stuff with error (?)
                }
                // No problems here, just responded to our requests :p
                return;
            }

            // $.data
            if (!json.has("data")) return;
            JSONObject data = json.getJSONObject("data");

            // $.data.message
            if (!data.has("message")) return;
            JSONObject message = new JSONObject(data.getString("message"));

            // $.data.message.type
            if (!message.has("type")) return;
            String type = message.getString("type");

            if (type.equals("reward-redeemed"))
                onChannelPointRedeem(message);

        } catch (JSONException e) {
            throw new InternalError("TODO: Error message", e);
        }
    }

    protected void onChannelPointRedeem(JSONObject message) {
        JSONObject data = message.optJSONObject("data");

        String title = data.optJSONObject("redemption").optJSONObject("reward").optString("title");
        int cost = data.optJSONObject("redemption").optJSONObject("reward").optInt("cost");
        String actorNickname = data.optJSONObject("redemption").optJSONObject("user").optString("display_name");
        String actorMessage = data.optJSONObject("redemption").optString("user_input");

        // TODO: Extract to EventBuilder, perhaps? Can stay like this too. Need to reconsider

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        EventArguments eventArguments = new EventArguments("channelPointReward", "twitch");
        eventArguments.streamerNickname = player == null ? "Dev" : player.getDisplayName().getString();
        eventArguments.actorNickname = actorNickname;
        eventArguments.message = actorMessage;
        eventArguments.donationAmount = cost;
        // In the memory of BrooKlynOtter's 1 Sea Shell "Test Reward"
        eventArguments.donationCurrency = "Sea Shell";
        eventArguments.rewardTitle = title;

        // Pass the model to the handler
        NetworkManager.CHANNEL.sendToServer(
                new EventPacket(eventArguments)
        );
    }

}
