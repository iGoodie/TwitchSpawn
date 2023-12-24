package net.programmer.igoodie.twitchspawn.udl;

import com.google.common.io.Resources;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotepadUDLUpdater {

    private static Set<String> PROCESSES_TO_TERMINATE = new HashSet<>(Arrays.asList(
            "notepad.exe",
            "notepad++.exe"
    ));

    private static Pattern USERLANG_MATCHER = Pattern.compile(
            "<UserLang (?<attributes>.*?)>.*?<\\/UserLang>",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    public static void attemptUpdate() {
        TwitchSpawn.LOGGER.info("Attempting to update TSL Notepad UDL.");

        if (!operatingSystemCapable()) {
            TwitchSpawn.LOGGER.warn("OS is not Windows. Skipping the TSL Notepad UDL update..");
            return;
        }

        try { closeNotepadPP(); } catch (IOException e) {
            TwitchSpawn.LOGGER.warn("Failed to close Notepad++ process. Skipping TSL Notepad UDL update.. Error: {}", e.getMessage());
            return;
        }

        try { injectUDL(); } catch (IOException e) {
            TwitchSpawn.LOGGER.warn("Failed to inject UDL. Skipping TSL Notepad UDL update.. Error: {}", e.getMessage());
        }

        TwitchSpawn.LOGGER.info("Updated TSL Notepad UDL successfully.");
    }

    private static boolean operatingSystemCapable() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void closeNotepadPP() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = input.readLine()) != null) {
            String[] info = line.split("\\s+");
            if (info.length < 2) continue;
            String processName = info[0];
            String pid = info[1];
            if (PROCESSES_TO_TERMINATE.contains(processName)) {
                TwitchSpawn.LOGGER.trace("Terminating {} (pid:{})", processName, pid);
                runtime.exec("taskkill /F /T /PID " + pid);
            }
        }

        input.close();
    }

    private static void injectUDL() throws IOException {
        String udlXMLPath = System.getenv("appdata") + "\\Notepad++\\userDefineLang.xml";
        File udlXMLFile = new File(udlXMLPath);

        if (!udlXMLFile.exists()) {
            System.out.println(udlXMLFile.getParentFile().mkdirs());
            FileUtils.writeStringToFile(udlXMLFile, "<NotepadPlus></NotepadPlus>", StandardCharsets.UTF_8);
        }

        StringBuilder replacedUDLs = new StringBuilder("<NotepadPlus>");

        boolean injected = false;
        String udlsRaw = FileUtils.readFileToString(udlXMLFile, StandardCharsets.UTF_8);
        Matcher userLangMatcher = USERLANG_MATCHER.matcher(udlsRaw);
        while (userLangMatcher.find()) {
            replacedUDLs.append("\n\t");

            if (userLangMatcher.group("attributes").contains("TSL")) {
                if (injected) continue;
                replacedUDLs.append(getTSLUDL());
                injected = true;

            } else {
                replacedUDLs.append(userLangMatcher.group());
            }
        }

        if (!injected) {
            replacedUDLs.append(getTSLUDL());
            injected = true;
        }

        replacedUDLs.append("\n</NotepadPlus>");

        FileUtils.writeStringToFile(udlXMLFile, replacedUDLs.toString(), StandardCharsets.UTF_8);
    }

    private static String getTSLUDL() throws IOException {
        URL location = Resources.getResource("assets/twitchspawn/udl/tsl_udl.xml");
        String raw = Resources.toString(location, StandardCharsets.UTF_8);
        Matcher matcher = USERLANG_MATCHER.matcher(raw);
        if (!matcher.find()) throw new InternalError("A malformed TSL UDL is under resources...");
        return matcher.group();
    }

}
