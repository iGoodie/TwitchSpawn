package igoodie.twitchspawn.utils;

import igoodie.twitchspawn.TSConstants;

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

public class FileUtils {
	public static boolean fileExists(String path) {
		return new File(path).exists();
	}

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
			return null;
		}
	}
	
	public static void writeString(String data, String path) {
		try {
			File f = new File(path);
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();
		}
		catch(IOException e) {
			
		}
	}

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
}
