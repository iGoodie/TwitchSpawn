package net.programmer.igoodie.twitchspawn.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

public class NetworkManager {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(TwitchSpawn.MOD_ID);

    public static void initialize() {
        CHANNEL.registerMessage(StatusChangedPacket.class,
                StatusChangedPacket.Message.class, 0, Side.CLIENT);

        CHANNEL.registerMessage(OsRunPacket.class,
                OsRunPacket.Message.class, 1, Side.CLIENT);

        CHANNEL.registerMessage(GlobalChatCooldownPacket.class,
                GlobalChatCooldownPacket.Message.class, 2, Side.CLIENT);
    }

}
