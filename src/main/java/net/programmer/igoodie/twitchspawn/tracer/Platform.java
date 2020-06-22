package net.programmer.igoodie.twitchspawn.tracer;

public enum Platform {

    STREAMLABS(
            "Streamlabs",
            "https://sockets.streamlabs.com"),
    STREAMELEMENTS(
            "StreamElements",
            "https://realtime.streamelements.com"),
    TWITCH_PUBSUB(
            "Twitch PubSub",
            "wss://pubsub-edge.twitch.tv"
    ),
    TWITCH_CHAT_IRC(
            "Twitch Chat IRC",
            "wss://irc-ws.chat.twitch.tv:443"
    );

    public static Platform withName(String name) {
        for (Platform platform : values()) {
            if (platform.name.equalsIgnoreCase(name))
                return platform;
        }
        return null;
    }

    /* ----------------------------- */

    public String name;
    public String url;

    Platform(String name, String url) {
        this.name = name;
        this.url = url;
    }

}
