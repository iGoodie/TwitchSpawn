package net.programmer.igoodie.twitchspawn.network;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

public class NetworkManager {
    /**
     * Protocol versions now are Integers. So gone 0.0.5 :)
     */
    private static final int PROTOCOL_VERSION = 1;

    /**
     * Channel for network communication.
     */
    public static final SimpleChannel CHANNEL = ChannelBuilder.
        named(new ResourceLocation(TwitchSpawn.MOD_ID, "network")).
        clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).
        serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).
        networkProtocolVersion(PROTOCOL_VERSION).
        simpleChannel();


    /**
     * The method that initializes the network channel.
     */
    public static void initialize() {
        CHANNEL.messageBuilder(StatusChangedPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(StatusChangedPacket::decode)
            .encoder(StatusChangedPacket::encode)
            .consumerMainThread(StatusChangedPacket::handle)
            .add();

        CHANNEL.messageBuilder(OsRunPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(OsRunPacket::decode)
            .encoder(OsRunPacket::encode)
            .consumerMainThread(OsRunPacket::handle)
            .add();

        CHANNEL.messageBuilder(GlobalChatCooldownPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(GlobalChatCooldownPacket::decode)
            .encoder(GlobalChatCooldownPacket::encode)
            .consumerMainThread(GlobalChatCooldownPacket::handle)
            .add();
    }
}
