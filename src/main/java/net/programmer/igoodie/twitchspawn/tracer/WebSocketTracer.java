package net.programmer.igoodie.twitchspawn.tracer;

import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import okhttp3.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class WebSocketTracer {

    protected TraceManager manager;
    protected Platform api;
    protected List<OkHttpClient> clients;

    public WebSocketTracer(Platform api, TraceManager manager) {
        this.manager = manager;
        this.api = api;
        this.clients = new LinkedList<>();
    }

    public abstract void start();

    public abstract void stop();

    protected WebSocketListener createSocket(CredentialsConfig.Streamer streamer) {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                WebSocketTracer.this.onOpen(streamer, webSocket, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                WebSocketTracer.this.onMessage(streamer, webSocket, text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                WebSocketTracer.this.onClosing(streamer, webSocket, code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                WebSocketTracer.this.onFailure(streamer, webSocket, t, response);
            }
        };
    }

    protected OkHttpClient startClient(WebSocketListener socket) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request connectRequest = new Request.Builder()
                .url(api.url)
                .build();

        client.newWebSocket(connectRequest, socket);

        return client;
    }

    protected void onOpen(CredentialsConfig.Streamer streamer, WebSocket socket, Response response) {}

    protected void onMessage(CredentialsConfig.Streamer streamer, WebSocket socket, String text) {}

    protected void onClosing(CredentialsConfig.Streamer streamer, WebSocket socket, int code, String reason) {}

    protected void onFailure(CredentialsConfig.Streamer streamer, WebSocket socket, Throwable t, Response response) {}

}
