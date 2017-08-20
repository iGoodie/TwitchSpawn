package igoodie.twitchspawn.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MinecraftUtils {
	public static void noticeChat(ICommandSender sender, String msg, TextFormatting style) {
		ITextComponent tc = new TextComponentString(msg).setStyle(new Style().setColor(style));
		sender.sendMessage(tc);
	}

	public static void noticeChat(ICommandSender sender, String msg) {
		ITextComponent tc = new TextComponentString(msg);
		sender.sendMessage(tc);
	}

	public static void noticeScreen(String title, String subtitle) {
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		gui.displayTitle(title, null, 1, 2, 3);
		gui.displayTitle(null, subtitle, 1, 2, 3);
	}
}
