package net.programmer.igoodie.twitchspawn.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

public class NetworkManager {

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String name) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TwitchSpawn.MOD_ID, name));
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(StatusChangedPacket.TYPE, StatusChangedPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OsRunPacket.TYPE, OsRunPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GlobalChatCooldownPacket.TYPE, GlobalChatCooldownPacket.CODEC);
    }

    public static void sendTo(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }

}
