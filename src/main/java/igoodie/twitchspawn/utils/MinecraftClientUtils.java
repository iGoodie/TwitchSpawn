package igoodie.twitchspawn.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class MinecraftClientUtils {	
	public static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}
	
	public static void noticeChat(String msg) {
		noticeChat(msg, null);
	}
	
	public static void noticeChat(String msg, TextFormatting style) {
		if(!isClient()) return;
		ITextComponent tc = new TextComponentString(msg).setStyle(new Style().setColor(style));
		EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();
		player.sendMessage(tc);
	}
	
	public static void noticeScreen(String msg) {
		noticeScreen(msg, "");
	}

	public static void noticeScreen(String title, String subtitle) {
		if(!isClient()) return;
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		gui.displayTitle(title, null, 1, 2, 3);
		gui.displayTitle(null, subtitle, 1, 2, 3);
		FMLClientHandler.instance().getClientPlayerEntity().playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
	}
}
