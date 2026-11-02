package net.programmer.igoodie.twitchspawn.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;

public record StatusChangedPacket(boolean status) implements CustomPacketPayload {

    public static final Type<StatusChangedPacket> TYPE = NetworkManager.type("status_changed");

    public static final StreamCodec<ByteBuf, StatusChangedPacket> CODEC =
            ByteBufCodecs.BOOL.map(StatusChangedPacket::new, StatusChangedPacket::status);

    @Override
    public Type<StatusChangedPacket> type() {
        return TYPE;
    }

    public static void handle(final StatusChangedPacket packet) {
        StatusIndicatorOverlay.setRunning(packet.status);
    }

}
