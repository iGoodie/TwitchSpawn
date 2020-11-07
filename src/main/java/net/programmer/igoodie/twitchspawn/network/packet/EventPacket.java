package net.programmer.igoodie.twitchspawn.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;

import java.util.Objects;
import java.util.function.Supplier;

public class EventPacket {

    public EventArguments eventArguments;

    public EventPacket() {}

    public EventPacket(EventArguments eventArguments) {
        this.eventArguments = eventArguments;
    }

    /* ------------------------------------ */

    public static void encode(EventPacket packet, PacketBuffer buffer) {
        buffer.writeCompoundTag(packet.eventArguments.serializeNBT());
    }

    public static EventPacket decode(PacketBuffer buffer) {
        EventPacket packet = new EventPacket();
        packet.eventArguments = new EventArguments();
        packet.eventArguments.deserializeNBT(Objects.requireNonNull(buffer.readCompoundTag()));
        return packet;
    }

    public static void handle(final EventPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ConfigManager.RULESET_COLLECTION.handleEvent(packet.eventArguments);
        });

        context.setPacketHandled(true);
    }

}
