package net.programmer.igoodie.twitchspawn.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;

public class GlobalChatCooldownPacket implements IMessageHandler<GlobalChatCooldownPacket.Message, IMessage> {

    public static class Message implements IMessage {

        private long timestamp;

        public Message() {}

        public Message(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeLong(this.timestamp);
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            this.timestamp = buffer.readLong();
        }

    }

    @Override
    public IMessage onMessage(Message message, MessageContext context) {
        GlobalChatCooldownOverlay.setCooldownTimestamp(message.timestamp);

        return null; // No response
    }

}
