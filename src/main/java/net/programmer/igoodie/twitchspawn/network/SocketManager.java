package net.programmer.igoodie.twitchspawn.network;

import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.network.socket.TwitchChatSocket;
import net.programmer.igoodie.twitchspawn.network.socket.TwitchPubSubSocket;
import net.programmer.igoodie.twitchspawn.network.socket.base.SocketTracer;

public class SocketManager {

    private static boolean running = false;

    public static SocketTracer PLATFORM_SOCKET;
    public static TwitchPubSubSocket TWITCH_PUB_SUB_SOCKET;
    public static TwitchChatSocket TWITCH_CHAT_SOCKET;

    public static boolean isRunning() {
        return running;
    }

    /* --------------------- */

    public static void initialize() {
        PLATFORM_SOCKET = ConfigManager.CLIENT_CREDS.platform.handlerGenerator.get();
        TWITCH_PUB_SUB_SOCKET = new TwitchPubSubSocket();
        TWITCH_CHAT_SOCKET = new TwitchChatSocket();
    }

    public static boolean start() {
        if (!PLATFORM_SOCKET.start(ConfigManager.CLIENT_CREDS)) {
            stop();
            return false;
        }

        if (!TWITCH_PUB_SUB_SOCKET.start(ConfigManager.CLIENT_CREDS)) {
            stop();
            return false;
        }

        if (!TWITCH_CHAT_SOCKET.start(ConfigManager.CLIENT_CREDS)) {
            stop();
            return false;
        }

        StatusIndicatorOverlay.setRunning(running = true);
        return true;
    }

    public static boolean stop() {
        PLATFORM_SOCKET.stop();
        TWITCH_PUB_SUB_SOCKET.stop();
        TWITCH_CHAT_SOCKET.stop();
        StatusIndicatorOverlay.setRunning(running = false);
        return true;
    }

}
