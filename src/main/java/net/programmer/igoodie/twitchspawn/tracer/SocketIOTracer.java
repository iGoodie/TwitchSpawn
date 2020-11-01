package net.programmer.igoodie.twitchspawn.tracer;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONObject;

import java.net.URISyntaxException;

public abstract class SocketIOTracer {

    protected TraceManager manager;
    protected Platform api;

    public SocketIOTracer(Platform api, TraceManager manager) {
        this.manager = manager;
        this.api = api;
    }

    protected String liveEventChannelName() {
        return "event";
    }

    protected Socket createSocket(CredentialsConfig.Streamer streamer) {
        checkCredentials(streamer);

        try {
            IO.Options options = generateOptions(streamer);
            Socket socket = IO.socket(api.url, options);

            socket.on(Socket.EVENT_CONNECT, args -> onConnect(socket, streamer, args));
            socket.on(Socket.EVENT_DISCONNECT, args -> onDisconnect(socket, streamer, args));
            socket.on(liveEventChannelName(), args -> onLiveEvent(socket, streamer, args));

            return socket;

        } catch (URISyntaxException e) {
            throw new InternalError("Invalid URI for " + api.name + " = " + api.url);
        }
    }

    protected void checkCredentials(CredentialsConfig.Streamer streamer) {}

    protected IO.Options generateOptions(CredentialsConfig.Streamer streamer) {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.transports = new String[]{"websocket"};

        return options;
    }

    protected abstract void onConnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args);

    protected abstract void onDisconnect(Socket socket, CredentialsConfig.Streamer streamer, Object... args);

    protected abstract void onLiveEvent(Socket socket, CredentialsConfig.Streamer streamer, Object... args);

}
