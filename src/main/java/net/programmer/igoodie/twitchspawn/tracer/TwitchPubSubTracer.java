package net.programmer.igoodie.twitchspawn.tracer;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TwitchPubSubTracer extends WebSocketTracer {

    protected List<Timer> pingSchedulers;

    public TwitchPubSubTracer(TraceManager manager) {
        super(Platform.TWITCH_PUBSUB, manager);
        this.pingSchedulers = new LinkedList<>();
    }

    @Override
    public void start() {
        for (CredentialsConfig.Streamer streamer : ConfigManager.CREDENTIALS.streamers) {
            WebSocketListener socket = createSocket(streamer);
            this.clients.add(startClient(socket));
        }
    }

    @Override
    public void stop() {
        for (OkHttpClient client : this.clients) {
            // TODO: Test shutdown for async issues
            client.dispatcher().executorService().shutdown();
        }

        pingSchedulers.forEach(timer -> {
            timer.cancel();
            timer.purge();
        });

        pingSchedulers.clear();
    }

    @Override
    protected void onClosing(CredentialsConfig.Streamer streamer, WebSocket socket, int code, String reason) {
        socket.close(1000, null);
    }

    @Override
    protected void onOpen(CredentialsConfig.Streamer streamer, WebSocket socket, Response response) {
        JSONObject authJSON = createAuthJSON(streamer);
        socket.send(authJSON.toString());

        Timer timer = new Timer();
        this.pingSchedulers.add(timer);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                socket.send(createPingJSON().toString());
            }
        }, 0, 4 * 60 * 1000);
    }

    protected JSONObject createAuthJSON(CredentialsConfig.Streamer streamer) {
        try {
            String userID = nicknameToIDSync(streamer.twitchNick);

            JSONObject authentication = new JSONObject();
            authentication.put("type", "LISTEN");

            JSONObject data = new JSONObject();
            Collection<String> topics = new ArrayList<>();
            topics.add("community-points-channel-v1." + userID);
            data.put("topics", topics);
            authentication.put("data", data);

            return authentication;

        } catch (JSONException e) {
            throw new InternalError("TODO: Error message", e);
        }
    }

    protected JSONObject createPingJSON() {
        try {
            JSONObject ping = new JSONObject();
            ping.put("type", "PING");
            return ping;

        } catch (JSONException e) {
            throw new InternalError("TODO: Error message", e);
        }
    }

    protected String nicknameToIDSync(String nickname) {
        try {
            URL url = new URL("http://decapi.me/twitch/id/" + nickname);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String userID = reader.readLine();

            reader.close();

            return userID;

        } catch (IOException e) {
            // TODO: Unknown username too :p
            throw new InternalError("TODO: Error message", e);
        }
    }

    @Override
    protected void onMessage(CredentialsConfig.Streamer streamer, WebSocket socket, String text) {
        try {
            JSONObject json = new JSONObject(text);

            TwitchSpawn.LOGGER.info("Received Twitch PubSub packet {} -> {}",
                    streamer.twitchNick, json);

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
                onChannelPointRedeem(streamer, message);

        } catch (JSONException e) {
            throw new InternalError("TODO: Error message", e);
        }
    }

    protected void onChannelPointRedeem(CredentialsConfig.Streamer streamer, JSONObject message) {
        JSONObject data = message.optJSONObject("data");

        String title = data.optJSONObject("redemption").optJSONObject("reward").optString("title");
        int cost = data.optJSONObject("redemption").optJSONObject("reward").optInt("cost");
        String actorNickname = data.optJSONObject("redemption").optJSONObject("user").optString("display_name");
        String actorMessage = data.optJSONObject("redemption").optString("user_input");

        EventArguments eventArguments = new EventArguments("channelPointReward", "twitch");
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = actorNickname;
        eventArguments.message = actorMessage;
        eventArguments.donationAmount = cost;
        // In the memory of BrooKlynOtter's 1 Sea Shell "Test Reward"
        eventArguments.donationCurrency = "Sea Shell";
        eventArguments.rewardTitle = title;

        ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments);
    }

}
