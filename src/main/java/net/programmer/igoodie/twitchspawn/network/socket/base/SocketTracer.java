package net.programmer.igoodie.twitchspawn.network.socket.base;

import net.programmer.igoodie.twitchspawn.configuration.ClientCredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.Platform;

public interface SocketTracer {

    Platform getPlatform();

    boolean isConnected();

    boolean start(ClientCredentialsConfig config);

    void stop();

}
