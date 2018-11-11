package igoodie.twitchspawn.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import igoodie.twitchspawn.TSConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class FileUtils {
	
	public static boolean fileExists(String path) {
		return new File(path).exists();
	}

	/* External IO */
	public static void createFile(String path) {
		try {
			File f = new File(path);
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		catch(IOException e) {
		
		}
	}

	public static void createDir(String path) {
		File f = new File(path);
		f.mkdirs();
	}
	
	public static String readString(String path) {
		try {
			File f = new File(path);
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String str="", line;
			while((line=br.readLine()) != null) {
				str += line + "\n"; 
			}
			br.close();
			return str;
		}
		catch(IOException e) {
			return "";
		}
	}
	
	public static void writeString(String data, String path) {
		try {
			createFile(path);
			File f = new File(path);
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();
		}
		catch(IOException e) {
			System.out.println("ERROR");
		}
	}

	/* Online IO */
	public static JsonObject fetchJson(String url) {
		try {
			HttpURLConnection req = (HttpURLConnection) new URL(url).openConnection();
			req.setRequestProperty("User-Agent", TSConstants.URL_USER_AGENT);
			req.connect();
			JsonElement root = new JsonParser().parse(new InputStreamReader((InputStream) req.getContent()));
			return root.getAsJsonObject();
		}
		catch(IOException e) {
			return null;
		}
	}

	/* Internal IO */
	public static String readPrototypeString(String path) {
		StringBuilder sb = new StringBuilder();
		
		try {
			ResourceLocation loc = new ResourceLocation("twitchspawn", "prototypes/"+path);
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
			String line;
			while((line=br.readLine()) != null) sb.append(line+"\n");
			br.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
}
