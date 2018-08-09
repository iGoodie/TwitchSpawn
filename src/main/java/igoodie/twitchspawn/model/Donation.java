package igoodie.twitchspawn.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.JsonObject;

import igoodie.twitchspawn.TwitchSpawn;

public class Donation implements Comparable<Donation> {

	public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String username;
	public double amount;
	public long timestamp;
	public String note;

	public Donation() {}

	public Donation(JsonObject json) {
		try {
			this.amount = json.get("amount").getAsDouble();
			this.username = json.get("donator").getAsJsonObject().get("name").getAsString();
			this.timestamp = SIMPLE_DATE_FORMAT.parse(json.get("created_at").getAsString()).getTime();
			this.note = json.get("message").getAsString();
		}
		catch(ParseException e) {
			TwitchSpawn.LOGGER.error("Invalid donation model fetched from StreamLabs");
		}
	}

	@Override
	public int compareTo(Donation o) {
		if(timestamp > o.timestamp) return 1;
		if(timestamp < o.timestamp) return -1;
		return 0;
	}
}
