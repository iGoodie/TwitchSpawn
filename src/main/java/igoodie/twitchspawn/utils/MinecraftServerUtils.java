package igoodie.twitchspawn.utils;

import igoodie.twitchspawn.packet.PacketDisplayTitle;
import igoodie.twitchspawn.packet.TwitchSpawnPacketHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class MinecraftServerUtils {
	public static boolean isServer() {
		return FMLCommonHandler.instance().getEffectiveSide().isServer();
	}
	
	public static void noticeChatFor(ICommandSender sender, String msg, TextFormatting style) {
		noticeChatFor(sender.getName(), msg, style);
	}

	public static void noticeChatFor(ICommandSender sender, String msg) {
		noticeChatFor(sender, msg, null);
	}
	
	public static void noticeChatFor(String sender, String msg, TextFormatting style) {
		if(!isServer()) return;
		ITextComponent tc = new TextComponentString(msg).setStyle(new Style().setColor(style));
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		server.getPlayerList().getPlayerByUsername(sender).sendMessage(tc);
		
	}

	public static void noticeChatAll(String msg) {
		noticeChatAll(msg, null);
	}
	
	public static void noticeChatAll(String msg, TextFormatting style) {
		if(!isServer()) return;
		ITextComponent tc = new TextComponentString(msg).setStyle(new Style().setColor(style));
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		server.getPlayerList().sendMessage(tc);
	}
	
	public static void noticeScreen(EntityPlayerMP player, String title, String subtitle) {
		if(!isServer()) return;
		PacketDisplayTitle.Message packet = new PacketDisplayTitle.Message(title, subtitle);
		TwitchSpawnPacketHandler.NETWORK_WRAPPER.sendTo(packet, player);
	}
}
