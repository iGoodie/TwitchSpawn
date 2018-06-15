package igoodie.twitchspawn.network.handlers;

import igoodie.twitchspawn.model.packet.TitlePacket;
import igoodie.twitchspawn.utils.MinecraftClientUtils;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TitlePacketHandler implements IMessageHandler<TitlePacket, IMessage> { //<REQ, RES>
	@Override
	public IMessage onMessage(TitlePacket message, MessageContext ctx) {
		String[] format = message.subtitle.split("\\|");
		System.out.println(format[0] + "|" + format[1]);
		format[0] = String.format(format[0], I18n.format(format[1] + ".name"));
		System.out.println(format[0] + "|" + format[1]);
		MinecraftClientUtils.noticeScreen(message.title, format[0]);
		return null; // No response
	}
}
