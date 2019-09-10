package net.programmer.igoodie.twitchspawn.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "0.0.3";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(TwitchSpawn.MOD_ID, "network"))
            .clientAcceptedVersions(v -> v.equals(PROTOCOL_VERSION))
            .serverAcceptedVersions(v -> v.equals(PROTOCOL_VERSION))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void initialize() {
        CHANNEL.registerMessage(0, StatusChangedPacket.class,
                StatusChangedPacket::encode,
                StatusChangedPacket::decode,
                StatusChangedPacket::handle);

        CHANNEL.registerMessage(1, OsRunPacket.class,
                OsRunPacket::encode,
                OsRunPacket::decode,
                OsRunPacket::handle);
    }

}
