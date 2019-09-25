package net.programmer.igoodie.twitchspawn.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;

public class StatusChangedPacket implements IMessageHandler<StatusChangedPacket.Message, IMessage> {

    public static class Message implements IMessage {

        private boolean status;

        public Message() {}

        public Message(boolean status) {
            this.status = status;
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeBoolean(this.status);
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            this.status = buffer.readBoolean();
        }

    }

    @Override
    public IMessage onMessage(Message message, MessageContext context) {
        StatusIndicatorOverlay.setRunning(message.status);

        return null; // No response
    }

}
