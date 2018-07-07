package igoodie.twitchspawn.tracer;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TwitchTracer extends JsonTracer {
	
	public static final String CHATTERS_REQ_URL_PATTERN = "http://tmi.twitch.tv/group/user/${streamer_twitch_nick}/chatters";
	
	public static TwitchTracer instance;
	
	private volatile ArrayList<String> viewers = new ArrayList<>();
	
	/* --- */
	public TwitchTracer(String name, String streamerNick) {
		super(name, CHATTERS_REQ_URL_PATTERN.replace("${streamer_twitch_nick}", streamerNick), 30);
	}

	public TwitchTracer(String streamerNick) {
		super("Twitch Tracer Thread", CHATTERS_REQ_URL_PATTERN.replace("${streamer_twitch_nick}", streamerNick), 30);
	}

	public ArrayList<String> getViewers() {
		return viewers;
	}
	
	@Override
	protected void trace(JsonObject fetchedJSON) {
		JsonObject chatters = fetchedJSON.get("chatters").getAsJsonObject();
		JsonArray modsJson = chatters.get("viewers").getAsJsonArray();
		JsonArray viewersJson = chatters.get("moderators").getAsJsonArray();
		
		for(JsonElement j : modsJson) {
			viewers.add(j.getAsString());
		}
		for(JsonElement j : viewersJson) {
			viewers.add(j.getAsString());
		}
	}
}