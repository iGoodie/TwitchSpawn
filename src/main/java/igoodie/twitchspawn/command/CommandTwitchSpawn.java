package igoodie.twitchspawn.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import igoodie.twitchspawn.TSConstants;
import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.tracer.StreamLabsSocket;
import igoodie.twitchspawn.tracer.TwitchTracer;
import igoodie.twitchspawn.utils.JSONHelper;
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

	// Probably the ugliest way to allias test command
	public static final HashMap<String, String> EVENT_TYPES = new HashMap<>(); {
		EVENT_TYPES.put("donation", "donation|streamlabs");
		EVENT_TYPES.put("d", "donation|streamlabs");

		EVENT_TYPES.put("bits", "bits|twitch_account");
		EVENT_TYPES.put("b", "bits|twitch_account");

		EVENT_TYPES.put("subscription", "subscription|twitch_account");
		EVENT_TYPES.put("sub", "subscription|twitch_account");
		EVENT_TYPES.put("subs", "subscription|twitch_account");
		EVENT_TYPES.put("s", "subscription|twitch_account");
		
		EVENT_TYPES.put("follow", "follow|twitch_account");
		EVENT_TYPES.put("f", "follow|twitch_account");
		
		EVENT_TYPES.put("host", "host|twitch_account");
		EVENT_TYPES.put("h", "host|twitch_account");
	}

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

		if(sender!=null) { // if command is not sent via server command line
			// Developer can by-pass that check on online mode servers!
			boolean verifiedDev = server.isServerInOnlineMode() && sender.getName().equals(TSConstants.DEVELOPER_NICK);
			if(!verifiedDev) {
				// If sender is not the streamer or a moderator, send an error chat message.
				String streamerNick = Configs.configJson.get("streamer_mc_nick").getAsString();
				JsonArray moderators = Configs.configJson.get("moderator_mc_nicks").getAsJsonArray();
				
				if(!streamerNick.equalsIgnoreCase(sender.getName()) && !JSONHelper.jsonArrayContains(moderators, sender.getName()) ) {
					TwitchSpawn.LOGGER.warn(sender.getName() + " tried to use TwitchSpawn commands but insufficient permissions.");
					String msg = String.format("Only streamer %s or moderators can execute TwitchSpawn commands!", streamerNick!=null ? "("+streamerNick+")" : "");
					MinecraftServerUtils.noticeChatFor(sender, msg, TextFormatting.RED);
					return;
				}
			}
		}

		// Find and execute module. XXX: Can be implemented better
		switch(args[0].toLowerCase()) {
		case "start": moduleStart(sender); break;
		case "stop": moduleStop(sender); break;
		case "reloadcfg": moduleReloadCfg(sender); break;
		case "status": moduleStatus(sender); break;
		case "test": moduleTest(sender, args); break;
		case "debug": moduleDebug(sender); break;
		default: throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	/* Modules */
	public void moduleStart(ICommandSender sender) throws CommandException {
		String streamerNick = Configs.configJson.get("streamer_mc_nick").getAsString();

		// Stop if already running
		if(StreamLabsSocket.isRunning()) {
			TwitchSpawn.LOGGER.warn("TwitchSpawn already running. Start command sent by " + sender.getName());
			MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn is already running!", TextFormatting.RED);
			return;
		}

		// Fetch streamlab tokens
		String accessToken = Configs.configJson.get("access_token").getAsString();
		String socketApiToken = Configs.configJson.get("socket_api_token").getAsString();

		try {
			StreamLabsSocket.start(socketApiToken);
			TwitchTracer.instance = new TwitchTracer(streamerNick); // TODO Change API to TwitchTracer::start()
			TwitchTracer.instance.start();
		} catch(IllegalArgumentException e) { // Invalid socket token
			TwitchSpawn.LOGGER.error("TwitchSpawn won't work, because tokens are not valid. Check config file!");
			MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn configs are invalid. Please check/refill them", TextFormatting.RED);
			return;
		} catch(InternalError e) {
			TwitchSpawn.LOGGER.error("TwitchSpawn couldn't connect Streamlabs socket. URI changed, or invalid token in config.json");
			MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn couldn't connect Streamlabs Socket. Please recheck your config.json or console!", TextFormatting.RED);
			return;
		}

		// Let everybody know TwitchSpawn is now listening to Streamlabs Socket!
		MinecraftServerUtils.noticeChatAll(">> TwitchSpawn is now started in this server!", TextFormatting.AQUA);
	}

	public void moduleStop(ICommandSender sender) throws CommandException {
		// Stop if not running
		if(!StreamLabsSocket.isRunning()) {
			TwitchSpawn.LOGGER.warn("TwitchSpawn already stopped. Stop command sent by " + sender.getName());
			MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn is already stopped!", TextFormatting.RED);
			return;
		}

		StreamLabsSocket.dispose();

		// Stop Twitch tracer if it's running
		if(TwitchTracer.instance.isRunning()) {
			TwitchTracer.instance.stop();
			TwitchTracer.instance = null;
		}

		MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn stopped.", TextFormatting.AQUA);
		MinecraftServerUtils.noticeChatAll(">> TwitchSpawn now left the server. :c", TextFormatting.AQUA);
	}

	public void moduleReloadCfg(ICommandSender sender) throws CommandException {
		if(StreamLabsSocket.isRunning())
			throw new CommandException(">> TwitchSpawn should be stopped in order to be able to reload configs. Type '/twitchspawn stop' and retry.");

		try {
			Configs.loadGeneralConfig();
		} catch(JsonParseException e) {
			MinecraftServerUtils.noticeChatFor(sender, ">> Reload failed. Invalid JSON syntax!", TextFormatting.RED);
			return;
		}

		MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn reloaded configs.", TextFormatting.BLUE);
	}

	public void moduleStatus(ICommandSender sender) {
		if(StreamLabsSocket.isRunning())
			MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn is currently running. [ON]", TextFormatting.AQUA);
		else 
			MinecraftServerUtils.noticeChatFor(sender, ">> TwitchSpawn is currently not running. [OFF]", TextFormatting.AQUA);
	}

	public void moduleTest(ICommandSender sender, String[] args) throws CommandException {
		if(args.length!=3 && args.length!=4)
			throw new WrongUsageException("/twitchspawn test <nick> <amount> [type]", new Object[0]);

		if(!StreamLabsSocket.isRunning())
			throw new CommandException(">> TwitchSpawn is currently not running. Turn it on before using test donation.");

		//Fetch & evaluate args
		try {
			String username = args[1];
			double amount = Double.parseDouble(args[2]);
			String type = args.length==4 ? args[3] : "donation";

			// Create a pseudo-donation message
			JSONArray donations = new JSONArray();
			JSONObject donation = new JSONObject();
			donation.put("from", username);
			donation.put("amount", amount);
			donation.put("months", amount); // Ugly hacky wack
			donation.put("viewers", amount); // Ugly hacky wack
			donations.put(donation);

			String eventType = EVENT_TYPES.get(type);
			if(eventType==null) {
				String types = String.join(", ", EVENT_TYPES.keySet());
				throw new CommandException("[type] is invalid. Valid values: " + types);
			}

			// Now simulate a donation test
			StreamLabsSocket.instance.handleMessage(eventType, donations);
		} catch(NumberFormatException e) {
			throw new WrongUsageException("<amount> should be a numerical value (e.g 1 / 1.0 / 1.0d / 1.0f)");
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void moduleDebug(ICommandSender sender) {
		/*String configs = Configs.beautifyJson(Configs.json);

		//MinecraftServerUtils.noticeChatFor(sender, configs);
		MinecraftServerUtils.noticeChatFor(sender, Configs.CONFIG_DIR);

		MinecraftServerUtils.noticeScreen((EntityPlayerMP)sender, "sa", "as");
		MinecraftServerUtils.noticeChatAll("To Everyone!");*/
	}
}
