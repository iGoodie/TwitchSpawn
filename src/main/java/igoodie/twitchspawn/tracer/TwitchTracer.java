package igoodie.twitchspawn.tracer;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.utils.FileUtils;
import net.minecraftforge.fml.common.FMLLog;

public class TwitchTracer extends Thread {
	private static final String CHATTERS_REQ_URL_PATTERN = "http://tmi.twitch.tv/group/user/${streamer_twitch_nick}/chatters";
	
	public static TwitchTracer instance;
	
	public static String formedUrl;
	
	private volatile boolean running = false;
	
	public volatile ArrayList<String> viewers = new ArrayList<>();
	
	/* Singleton Model */
	private TwitchTracer() { running = true; }
	
	public static void init() {
		//Fetch the twitch nick from configs and form the URL
		String streamerNick = Configs.json.get("streamer_twitch_nick").getAsString().toLowerCase();
		formedUrl = CHATTERS_REQ_URL_PATTERN.replace("${streamer_twitch_nick}", streamerNick);
	
		//Pre-check if configs are proper
		if(streamerNick.isEmpty() || streamerNick==null || FileUtils.fetchJson(formedUrl)==null) {
			FMLLog.log.error("TwitchSpawn can't find valid twitch channel. Twitch tracer won't work.");
			return;
		}
		
		//Initiate the instance & start thread
		instance = new TwitchTracer();
		instance.setName("Twitch Tracer");
		instance.start();
	}
	
	public static boolean isRunning() {
		return instance!=null && instance.running;
	}
	
	public static void stopRunning() {
		instance.running = false;
		instance.viewers.clear();
	}
	
	public static Collection<String> getViewers() {
		return instance.viewers;
	}
	
	/* Thread insider methods*/
	@Override
	public void run() {
		while(running) {
			viewers.clear();
			
			//Fetch viewers
			JsonArray modsJson = FileUtils.fetchJson(formedUrl).get("chatters").getAsJsonObject().get("viewers").getAsJsonArray();
			JsonArray viewersJson = FileUtils.fetchJson(formedUrl).get("chatters").getAsJsonObject().get("moderators").getAsJsonArray();
			System.out.println("Viewer size: " + viewersJson.size());
			for(JsonElement j : modsJson) {
				viewers.add(j.getAsString());
			}
			for(JsonElement j : viewersJson) {
				viewers.add(j.getAsString());
			}
			
			freeze(30); //Frequency: 30 sec
		}
	}
	
	protected void freeze(int timeSec) {
		try {
			synchronized (this) {
				this.wait(timeSec * 1000);
			}
		}
		catch (Exception e) {}
	}
}
