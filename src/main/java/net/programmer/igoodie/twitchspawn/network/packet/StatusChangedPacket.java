package net.programmer.igoodie.twitchspawn.network.packet;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
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

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> StatusIndicatorOverlay.setRunning(this.status));
        context.setPacketHandled(true);
    }

    /**
     * True if the status is running, false if stopped.
     */
    private final boolean status;
}
