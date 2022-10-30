package net.programmer.igoodie.twitchspawn.network.socket.base;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.programmer.igoodie.twitchspawn.configuration.ClientCredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.Platform;

import java.net.URISyntaxException;

public abstract class SocketIOBase implements SocketTracer {

    protected Platform platform;
    protected Socket socket;

    public SocketIOBase(Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean isConnected() {
        return socket != null
                && socket.connected();
    }

    @Override
    public Platform getPlatform() {
        return platform;
    }

    public Socket getSocket() {
        return socket;
    }

    /* ------------------------ */

    protected IO.Options generateOptions(ClientCredentialsConfig config) {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.transports = new String[]{"websocket"};
        return options;
    }

    public boolean validateCredentials(ClientCredentialsConfig config) {
        return true;
    }

    public boolean initialize(ClientCredentialsConfig config) {
        if (!validateCredentials(config)) {
            return false;
        }

        try {
            IO.Options options = generateOptions(config);
            this.socket = IO.socket(platform.url, options);
            bindEvents();

            return true;

        } catch (URISyntaxException e) {
            throw new InternalError("Invalid URI for " + platform.name + " = " + platform.url);
        }
    }

    @Override
    public boolean start(ClientCredentialsConfig config) {
        if (!initialize(config)) {
            return false;
        }

        if (this.socket == null) {
            throw new InternalError();
        }

        this.socket.connect();
        return true;
    }

    @Override
    public void stop() {
        if (this.socket == null) {
            return;
        }

        if (this.socket.connected()) {
            this.socket.disconnect();
            this.socket.close();
        }

        this.socket = null;
    }

    /* ------------------------ */

    protected void bindEvents() {
        this.socket.on(Socket.EVENT_CONNECT, args -> onConnect(socket, args));
        this.socket.on(Socket.EVENT_DISCONNECT, args -> onDisconnect(socket, args));
    }

    protected abstract void onConnect(Socket socket, Object... args);

    protected abstract void onDisconnect(Socket socket, Object... args);

}
