package net.programmer.igoodie.twitchspawn.network.packet;


import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;


public class StatusChangedPacket {

    public StatusChangedPacket(boolean status) {
        this.status = status;
    }

    public static void encode(StatusChangedPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.status);
    }

    public static StatusChangedPacket decode(FriendlyByteBuf buffer) {
        return new StatusChangedPacket(buffer.readBoolean());
    }

    public void handle(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> StatusIndicatorOverlay.setRunning(this.status));
    }

    /**
     * True if the status is running, false if stopped.
     */
    private final boolean status;
}
