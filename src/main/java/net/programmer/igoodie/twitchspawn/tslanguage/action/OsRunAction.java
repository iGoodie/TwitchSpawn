package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OsRunAction extends TSLAction {

    public static void handleLocalScript(Shell shell, String script) {
        ProcessResult result = runScript(shell, script);
        if (result.exception != null)
            TwitchSpawn.LOGGER.info("OS_RUN failed to run. ({})", result.exception.getMessage());
        else
            TwitchSpawn.LOGGER.info("OS_RUN ran script with exit code {}. Output Stream:{}",
                    result.exitCode, result.output.isEmpty()
                            ? "" : "\n" + result.output.trim());
    }

    /* ---------------------------------- */

    public static class ProcessResult {
        public Exception exception;
        public String output = "";
        public int exitCode = 0;
    }

    public static ProcessResult runScript(Shell shell, String script) {
        try {
            List<String> processScript = shell.createProcessScript(script);

            TwitchSpawn.LOGGER.info("Passing following script to the shell -> {}", String.join(" ", processScript));

            ProcessBuilder processBuilder = new ProcessBuilder(processScript);
            Process process = processBuilder.start();
            ProcessResult result = new ProcessResult();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String delimiter = "";
            String line;
            while ((line = reader.readLine()) != null) {
                result.output += (line + delimiter);
                delimiter = "\n";
            }

            result.exitCode = process.waitFor();
            return result;

        } catch (Exception e) {
            TwitchSpawn.LOGGER.info("Failed to run shell script -> {}", script);
            ProcessResult result = new ProcessResult();
            result.exception = e;
            return result;
        }
    }

    /* ---------------------------------- */

    public enum ScriptLocation {LOCAL, REMOTE}

    public enum Shell {
        CMD("cmd", "/c"),
        POWERSHELL("powershell", "/c"),
        BASH();

        public List<String> processPrefix;

        Shell(String... processPrefix) {
            this.processPrefix = Arrays.asList(processPrefix);
        }

        public List<String> createProcessScript(String script) {
            List<String> processScript = new LinkedList<>(this.processPrefix);

            String commandName = commandName(script);

            processScript.add(commandName);

            if (commandName.length() != script.length()) {
                processScript.add(script.substring(commandName.length()).trim());
            }

            return processScript;
        }

        private String commandName(String script) {
            boolean inQuote = false;

            for (int i = 0; i < script.length(); i++) {
                char ch = script.charAt(i);

                if (ch == '"') {
                    inQuote = !inQuote;
                    continue;
                }

                if (ch == ' ' && !inQuote)
                    return script.substring(0, i);
            }

            return script;
        }
    }

    /* ---------------------------------- */

    private ScriptLocation scriptLocation;
    private Shell shell;
    private String shellScript;

    /*
     * E.g usage:
     * OS_RUN LOCAL POWERSHELL echo %"Hello world!"%
     * OS_RUN LOCAL POWERSHELL echo %Hello world!%
     *
     * 0 - LOCAL/REMOTE
     * 1 - <shell_name>
     * 2 - <action>
     */
    public OsRunAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        // <location> <shell_name> <shell_script>
        if (actionWords.size() < 3)
            throw new TSLSyntaxError("Invalid length of words -> %s", actionWords);

        // Parse script location
        try {
            this.scriptLocation = ScriptLocation.valueOf(actionWords.get(0));

        } catch (IllegalArgumentException e) {
            throw new TSLSyntaxError("Unknown script location -> %s", actionWords.get(0));
        }

        // Parse shell name
        try {
            this.shell = Shell.valueOf(actionWords.get(1));

        } catch (IllegalArgumentException e) {
            throw new TSLSyntaxError("Unknown shell name -> %s", actionWords.get(1));
        }

        // Parse shell script
        StringBuilder stringBuilder = new StringBuilder();
        String delimiter = "";
        for (String scriptWord : actionWords.subList(2, actionWords.size())) {
            stringBuilder.append(delimiter);
            if (!scriptWord.startsWith("\"") && !scriptWord.endsWith("\"") && scriptWord.contains(" ")) {
                stringBuilder.append("\"");
                stringBuilder.append(scriptWord);
                stringBuilder.append("\"");
            } else {
                stringBuilder.append(scriptWord);
            }
            delimiter = " ";
        }

        this.shellScript = stringBuilder.toString();
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        if (scriptLocation == ScriptLocation.LOCAL) {
            handleLocalScript(shell, replaceExpressions(shellScript, args));

        } else if (scriptLocation == ScriptLocation.REMOTE) {
            NetworkManager.CHANNEL.sendTo(new OsRunPacket(shell, replaceExpressions(shellScript, args)),
                    player.connection.netManager,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
    }

}
