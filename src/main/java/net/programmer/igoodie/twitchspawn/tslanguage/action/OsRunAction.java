package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.google.common.base.Defaults;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OsRunAction extends TSLAction {

    public static class ProcessResult {
        public Exception exception;
        public String output = "";
        public int exitCode = 0;
    }

    public static ProcessResult runScript(Shell shell, String script) {
        try {
            List<String> processScript = new LinkedList<>(shell.processPrefix);
            processScript.add(script);

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

    public enum ScriptLocation {LOCAL, REMOTE}

    public enum Shell {
        CMD("cmd", "/c"),
        POWERSHELL("powershell", "/c"),
        BASH();

        public List<String> processPrefix;

        Shell(String... processPrefix) {
            this.processPrefix = Arrays.asList(processPrefix);
        }
    }

    /* ---------------------------------- */

    private ScriptLocation scriptLocation;
    private Shell shell;
    private boolean parameterized;
    private String shellScript;

    /*
     * E.g usage:
     * OS_RUN LOCAL POWERSHELL echo %"Hello world!"%
     * OS_RUN LOCAL PARAMETERIZED BASH sh %some/path/to/script.sh%
     *
     * 0 - LOCAL/REMOTE
     * 1 - [PARAMETERIZED]
     * 1|2 - <shell_name>
     * 2|3 - <action>
     */
    public OsRunAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        try {
            try {
                scriptLocation = ScriptLocation.valueOf(actionWords.get(0));

            } catch (IllegalArgumentException e) {
                throw new TSLSyntaxError("Unknown script location -> %s", actionWords.get(0));
            }

            parameterized = actionWords.get(1).equalsIgnoreCase("PARAMETERIZED");

            try {
                shell = Shell.valueOf(actionWords.get(parameterized ? 2 : 1));

            } catch (IllegalArgumentException e) {
                throw new TSLSyntaxError("Unknown shell name -> %s", actionWords.get(parameterized ? 2 : 1));
            }

            shellScript = parameterized
                    ? actionWords.get(3)
                    : actionWords.get(2);

        } catch (IndexOutOfBoundsException e) {
            throw new TSLSyntaxError("Invalid length of words -> %s", actionWords);
        }
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        String script = shellScript;

        if (parameterized) {
            try {
                for (Field field : EventArguments.class.getFields()) {
                    Object value = field.get(args);
                    Object defaultValue = Defaults.defaultValue(field.getType());

                    if ((value instanceof String) && ((String) value).isEmpty())
                        value = null;

                    if (value != null && !value.equals(defaultValue)) {
                        script += (" -" + field.getName() + ":"
                                + ((value instanceof String)
                                ? "\"" + value + "\"" : value));
                    }
                }

            } catch (IllegalAccessException e) {
                throw new InternalError("Error trying to parameterize OS_RUN");
            }
        }

        if (scriptLocation == ScriptLocation.LOCAL) {
            ProcessResult result = runScript(shell, script);
            if (result.exception != null)
                TwitchSpawn.LOGGER.info("OS_RUN failed to run. ({})", result.exception.getMessage());
            else
                TwitchSpawn.LOGGER.info("OS_RUN done with exit code {}:\n{}", result.exitCode, result.output);

        } else if (scriptLocation == ScriptLocation.REMOTE) {
            NetworkManager.CHANNEL.sendTo(new OsRunPacket(shell, script),
                    player.connection.netManager,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
    }

}
