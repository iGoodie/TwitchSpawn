package igoodie.twitchspawn.command;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.model.Donation;
import igoodie.twitchspawn.tracer.StreamLabsSocket;
import igoodie.twitchspawn.tracer.StreamLabsTracer;
import igoodie.twitchspawn.tracer.TwitchTracer;
import igoodie.twitchspawn.utils.MinecraftServerUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

/**
 * Effective Side = Server </br>
 * Trackers are always going to be @ Effective Server Side
 */
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
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if(args.length == 1) 
			return getListOfStringsMatchingLastWord(args, "start", "stop", "reloadcfg", "status", "test"); //twitchspawn *
		
		if(args.length == 2) {
			if(args[0].equals("test") && TwitchTracer.instance!=null && TwitchTracer.instance.isRunning()) { //twitchspawn test *
				Collection<String> viewers = TwitchTracer.instance.getViewers();
				if(!viewers.isEmpty()) return getListOfStringsMatchingLastWord(args, viewers);
				return Collections.<String>emptyList();
			}
		}
		
		if(args.length == 3) {
			if(args[0].equals("test")) { //twitchspawn test xxx *
				return getListOfStringsMatchingLastWord(args, "1");
			}
		}
		
		return Collections.<String>emptyList();
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		//return Configs.json.get("streamer_mc_nick").getAsString().equalsIgnoreCase(sender.getName());
		return true; // Everyone can see the command and execute the command
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) throw new WrongUsageException(getUsage(sender), new Object[0]);

		switch(args[0].toLowerCase()) {
		case "start": moduleStart(sender); break;
		case "stop": moduleStop(sender); break;
		case "reloadcfg": moduleReloadCfg(sender); break;
		case "status": moduleStatus(sender); break;
		case "test": moduleTest(sender, args); break;
		case "debug": moduleDebug(sender); break;
		case "socket":
			if(StreamLabsSocket.isRunning())
				StreamLabsSocket.dispose();
			else
				StreamLabsSocket.start(Configs.json.get("socket_api_token").getAsString());
			break;
		default: throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	/* Modules */
	public void moduleStart(ICommandSender sender) throws CommandException {
		if(StreamLabsTracer.instance!=null && StreamLabsTracer.instance.isRunning()) {
			throw new CommandException("TwitchSpawn is already started!");
		}
		
		// If sender is not a streamer, send an error chat message.
		String streamerNick = Configs.json.get("streamer_mc_nick").getAsString();
		if(!streamerNick.equalsIgnoreCase(sender.getName())) {
			String msg = String.format("Only streamer %s can start TwitchSpawn!", streamerNick!=null?"("+streamerNick+")":"");
			MinecraftServerUtils.noticeChatFor(sender, msg, TextFormatting.RED);
			return;
		}
		
		// Check and run TwitchTracer as well
		if(!streamerNick.isEmpty()) {
			TwitchTracer.instance = new TwitchTracer(streamerNick);
			TwitchTracer.instance.start();
		}
		
		// Fetch the access token from the configs
		String legacyToken = Configs.json.get("access_token").getAsString();
		if(legacyToken.isEmpty() || legacyToken==null) {
			TwitchSpawn.LOGGER.error("TwitchSpawn won't work, because no valid legacy token found in the configs");
			MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn configs are invalid. Please check/refill them", TextFormatting.RED);
			return;
		}
		
		// Construct the tracer with legacy token from configs
		StreamLabsTracer.instance = new StreamLabsTracer(legacyToken);
		StreamLabsTracer.instance.start();
		MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn is ready to spawn items!", TextFormatting.AQUA);
	}

	public void moduleStop(ICommandSender sender) throws CommandException {
		if(StreamLabsTracer.instance==null || !StreamLabsTracer.instance.isRunning()) {
			throw new CommandException("TwitchSpawn is not running!");
		}
		
		StreamLabsTracer.instance.stop();
		StreamLabsTracer.instance = null;
		
		if(TwitchTracer.instance.isRunning()) {
			TwitchTracer.instance.stop();
			TwitchTracer.instance = null;
		}
		
		MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn stopped.", TextFormatting.AQUA);
	}

	public void moduleReloadCfg(ICommandSender sender) throws CommandException {
		if(StreamLabsTracer.instance!=null && StreamLabsTracer.instance.isRunning())
			throw new CommandException("TwitchSpawn should be stopped in order to be able to reload configs. Type '/twitchspawn stop' and retry.");
		Configs.load();
		MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn reloaded configs", TextFormatting.BLUE);
	}

	public void moduleStatus(ICommandSender sender) {
		if(StreamLabsTracer.instance!=null && StreamLabsTracer.instance.isRunning()) 
			MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn is currently waiting for donations. [ON]", TextFormatting.AQUA);
		else 
			MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn is currently not running. [OFF]", TextFormatting.AQUA);
	}

	public void moduleTest(ICommandSender sender, String[] args) throws CommandException {
		if(args.length != 3) throw new WrongUsageException("/twitchspawn test <nick> <amount>", new Object[0]);
		if(StreamLabsTracer.instance==null || !StreamLabsTracer.instance.isRunning())
			throw new CommandException("TwitchSpawn is currently not running. Turn it on before using test donation.");
		
		//Fetch & evaluate args
		String username;
		double amount;
		try {
			username = args[1];
			amount = Double.parseDouble(args[2]);
		}
		catch(NumberFormatException e) {
			throw new WrongUsageException("<amount> should be a numerical value (e.g 1 / 1.0 / 1.0d / 1.0f)");
		}
		
		//Prepare model and queue
		Donation d = new Donation();
		d.username = username;
		d.amount = amount;
		d.timestamp = System.currentTimeMillis();
		StreamLabsTracer.instance.unhandledQueue.add(d);
	}

	public void moduleDebug(ICommandSender sender) {
		/*String configs = Configs.beautifyJson(Configs.json);
		
		//MinecraftServerUtils.noticeChatFor(sender, configs);
		MinecraftServerUtils.noticeChatFor(sender, Configs.CONFIG_DIR);
		
		MinecraftServerUtils.noticeScreen((EntityPlayerMP)sender, "sa", "as");
		MinecraftServerUtils.noticeChatAll("To Everyone!");*/
	}
}
