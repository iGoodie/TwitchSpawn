package net.programmer.igoodie.twitchspawn.network.socket.base;

import net.programmer.igoodie.twitchspawn.configuration.ClientCredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.Platform;
import okhttp3.*;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public abstract class WebSocketBase extends WebSocketListener implements SocketTracer {

    protected Platform platform;
    protected WebSocket webSocket;
    protected boolean connected;

    public WebSocketBase(Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean isConnected() {
        return webSocket != null
                && connected;
    }

    @Override
    public Platform getPlatform() {
        return platform;
    }

    public WebSocket getSocket() {
        return webSocket;
    }

    /* ------------------------ */

    public boolean validateCredentials(ClientCredentialsConfig config) {
        return true;
    }

    @Override
    public boolean start(ClientCredentialsConfig config) {
        if (!validateCredentials(config)) {
            return false;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request connectRequest = new Request.Builder()
                .url(platform.url)
                .build();

        this.webSocket = client.newWebSocket(connectRequest, this);
        return true;
    }

    @Override
    public void stop() {
        if (webSocket != null && !webSocket.close(1000, null)) {
            webSocket.cancel();
        }
        webSocket = null;
    }

    /* ------------------------ */

    @Override
    public void onOpen(@Nonnull WebSocket socket, @Nonnull Response response) {
        this.connected = true;
    }

    @Override
    public void onClosed(@Nonnull WebSocket webSocket, int code, @Nonnull String reason) {
        this.connected = false;
        this.webSocket = null;
    }

    @Override
    public abstract void onMessage(@Nonnull WebSocket socket, @Nonnull String text);

}
