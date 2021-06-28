package net.programmer.igoodie.twitchspawn.network;

import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.network.socket.StreamlabsSocket;
import net.programmer.igoodie.twitchspawn.network.socket.TwitchChatSocket;
import net.programmer.igoodie.twitchspawn.network.socket.TwitchPubSubSocket;

public class SocketManager {

    private static boolean running = false;

    public static final StreamlabsSocket STREAMLABS_SOCKET = new StreamlabsSocket();
    public static final TwitchPubSubSocket TWITCH_PUB_SUB_SOCKET = new TwitchPubSubSocket();
    public static final TwitchChatSocket TWITCH_CHAT_SOCKET = new TwitchChatSocket();

    public static boolean isRunning() {
        return running;
    }

    /* --------------------- */

    public static boolean start() {
        if (!STREAMLABS_SOCKET.start(ConfigManager.CLIENT_CREDS)) {
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
        STREAMLABS_SOCKET.stop();
        TWITCH_PUB_SUB_SOCKET.stop();
        TWITCH_CHAT_SOCKET.stop();
        StatusIndicatorOverlay.setRunning(running = false);
        return true;
    }

}
