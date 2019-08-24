package net.programmer.igoodie.twitchspawn.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "0.0.1";

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
    }

    private static void registerPacket(int index, Class<?> packetType) {
        // TODO: Automatize encode/decode/handle registration
//        int index,
//        Class<MSG> messageType,
//        java.util.function.BiConsumer<MSG, net.minecraft.network.PacketBuffer> encoder,
//        java.util.function.Function<net.minecraft.network.PacketBuffer, MSG> decoder,
//        java.util.function.BiConsumer<MSG, java.util.function.Supplier<net.minecraftforge.fml.network.NetworkEvent.Context>> messageConsumer
    }

}
