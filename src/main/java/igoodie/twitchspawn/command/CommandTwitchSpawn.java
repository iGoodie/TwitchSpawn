package igoodie.twitchspawn.command;

import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.model.Donation;
import igoodie.twitchspawn.streamlabs.StreamLabsChecker;
import igoodie.twitchspawn.utils.MinecraftUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class CommandTwitchSpawn extends CommandBase {
	@Override
	public String getName() {
		return "twitchspawn";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/twitchspawn start|stop|reloadcfg|status|test";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) throw new WrongUsageException(getUsage(sender), new Object[0]);
		
		switch(args[0].toLowerCase()) {
		case "start": {
			StreamLabsChecker.init(sender);
			break;			
		}
		case "stop":
			StreamLabsChecker.stopRunning(sender);
			break;
			
		case "reloadcfg": {
			if(StreamLabsChecker.isRunning()) {
				MinecraftUtils.noticeChat(sender, "TwitchSpawn should be stopped in order to be able to reload configs. Type '/twitchspawn stop' and retry.", TextFormatting.RED);
				break;
			}
			Configs.load();
			MinecraftUtils.noticeChat(sender, "TwitchSpawn reloaded configs", TextFormatting.BLUE);
			break;
		}
		
		case "status": {
			if(StreamLabsChecker.isRunning()) MinecraftUtils.noticeChat(sender, "TwitchSpawn is currently waiting for donations. [ON]", TextFormatting.BLUE);
			else MinecraftUtils.noticeChat(sender, "TwitchSpawn is currently not running. [OFF]", TextFormatting.BLUE);
			break;
		}
		
		case "test": {
			if(args.length != 3) throw new WrongUsageException("/twitchspawn test <nick> <amount>", new Object[0]);
			if(!StreamLabsChecker.isRunning()) MinecraftUtils.noticeChat(sender, "TwitchSpawn is currently not running. Turn it on before using test donation.", TextFormatting.RED);	
			Donation d = new Donation();
			d.username = args[1];
			d.amount = Double.parseDouble(args[2]);
			d.timestamp = System.currentTimeMillis();
			StreamLabsChecker.instance.donationQueue.add(d);
			//WorldServer w = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
			//EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername("Player47");
			//EntityItem item = new EntityItem(w, p.posX, p.posY+10, p.posZ, new ItemStack(Items.APPLE, 1).setStackDisplayName("Redowar"));
			//p.dropItem(new ItemStack(Items.APPLE, 1).setStackDisplayName("Redowar"), false);
			//Minecraft.getMinecraft().ingameGUI.displayTitle("iGoodie donated!", null, 1, 2, 3);
			//Minecraft.getMinecraft().ingameGUI.displayTitle(null, "You're lucky enough to be rewarded with Sikisokko Apple!", 1, 2, 3);
			//w.spawnEntity(item);
			//Minecraft.getMinecraft().
			break;
		}
		
		default: throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
		//MinecraftUtils.noticeChat(sender, "");
	}
}
