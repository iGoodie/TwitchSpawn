package net.programmer.igoodie.twitchspawn.log;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class TSLogger {

    public static ConsoleLogger createConsoleLogger(Class<?> target) {
        return new ConsoleLogger(target);
    }

    public static FileLogger createFileLogger(String namespace) throws IOException {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        String relativePath = String.format("/logs/TwitchSpawn/%s%s.log",
                namespace == null ? "default_" : namespace + "/", today);
        String path = FMLPaths.GAMEDIR.get().toString() + relativePath;
        File file = new File(path);
        if (!Files.exists(file.toPath())) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return new FileLogger(namespace, path);
    }

    public static void clearHistoricalLogs(int maxDays) {
        String path = FMLPaths.GAMEDIR.get().toString() + "/logs/TwitchSpawn";
        File logsFolder = new File(path);
        FileLogger.clearHistoricalLogs(logsFolder, maxDays);
    }

    public abstract void info(String msg, Object... args);

    public abstract void warn(String msg, Object... args);

    public abstract void error(String msg, Object... args);

    public abstract void debug(String msg, Object... args);

    public abstract void trace(String msg, Object... args);

}
