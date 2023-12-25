package net.programmer.igoodie.twitchspawn.network;


import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;


public class NetworkManager {
    /**
     * Channel for network communication.
     */
    public static final NetworkChannel CHANNEL = NetworkChannel.
        create(new ResourceLocation(TwitchSpawn.MOD_ID, "network"));


    /**
     * The method that initializes the network channel.
     */
    public static void initialize() {
        CHANNEL.register(StatusChangedPacket.class,
            StatusChangedPacket::encode,
            StatusChangedPacket::decode,
            StatusChangedPacket::handle);

        CHANNEL.register(OsRunPacket.class,
            OsRunPacket::encode,
            OsRunPacket::decode,
            OsRunPacket::handle);

        CHANNEL.register(GlobalChatCooldownPacket.class,
            GlobalChatCooldownPacket::encode,
            GlobalChatCooldownPacket::decode,
            GlobalChatCooldownPacket::handle);
    }
}
