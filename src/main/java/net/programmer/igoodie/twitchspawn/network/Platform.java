package net.programmer.igoodie.twitchspawn.network;

import net.programmer.igoodie.twitchspawn.network.socket.StreamElementsSocket;
import net.programmer.igoodie.twitchspawn.network.socket.StreamlabsSocket;
import net.programmer.igoodie.twitchspawn.network.socket.TwitchChatSocket;
import net.programmer.igoodie.twitchspawn.network.socket.TwitchPubSubSocket;
import net.programmer.igoodie.twitchspawn.network.socket.base.SocketTracer;

import java.util.function.Supplier;

public enum Platform {

    STREAMLABS(
            "Streamlabs",
            "https://sockets.streamlabs.com",
            StreamlabsSocket::new
    ),
    STREAMELEMENTS(
            "StreamElements",
            "https://realtime.streamelements.com",
            StreamElementsSocket::new
    ),
    TWITCH_PUBSUB(
            "Twitch PubSub",
            "wss://pubsub-edge.twitch.tv",
            TwitchPubSubSocket::new
    ),
    TWITCH_CHAT_IRC(
            "Twitch Chat IRC",
            "wss://irc-ws.chat.twitch.tv:443",
            TwitchChatSocket::new
    );

    public static Platform withName(String name) {
        for (Platform platform : values()) {
            if (platform.name.equalsIgnoreCase(name))
                return platform;
        }
        return null;
    }

    /* ----------------------------- */

    public final String name;
    public final String url;
    public final Supplier<SocketTracer> handlerGenerator;

    Platform(String name, String url, Supplier<SocketTracer> handlerGenerator) {
        this.name = name;
        this.url = url;
        this.handlerGenerator = handlerGenerator;
    }

}
