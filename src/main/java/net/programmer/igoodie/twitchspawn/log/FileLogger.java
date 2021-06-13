package net.programmer.igoodie.twitchspawn.log;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLogger extends TSLogger {

    public static final Pattern ARG_PATTERN = Pattern.compile("\\{}");

    protected Logger logger;

    public FileLogger(String namespace, String filepath) throws IOException {
        this.logger = Logger.getLogger(FileLogger.class.getSimpleName() + "." + namespace);
        this.logger.setUseParentHandlers(false);

        FileHandler fileHandler = new FileHandler(filepath, true);
        fileHandler.setFormatter(new FileLogger.Formatter());
        fileHandler.setLevel(Level.ALL);
        this.logger.addHandler(fileHandler);
    }

    @Override
    public void info(String msg, Object... args) {
        logger.info(replaceArgs(msg, args));
    }

    @Override
    public void warn(String msg, Object... args) {
        logger.warning(replaceArgs(msg, args));
    }

    @Override
    public void error(String msg, Object... args) {
        logger.log(Level.SEVERE, replaceArgs(msg, args));
    }

    @Override
    public void debug(String msg, Object... args) {
        logger.log(Level.FINER, replaceArgs(msg, args));
    }

    @Override
    public void trace(String msg, Object... args) {
        logger.log(Level.FINEST, replaceArgs(msg, args));
    }

    private String replaceArgs(String msg, Object... args) {
        Matcher matcher = ARG_PATTERN.matcher(msg);
        StringBuilder builder = new StringBuilder();
        int start = 0;
        int argIndex = 0;

        while (matcher.find()) {
            // Append previous part
            builder.append(msg, start, matcher.start());
            start = matcher.end();

            // Evaluate and append new value
            builder.append(args[argIndex++]);
        }

        // Append trailing chars
        builder.append(msg, start, msg.length());

        return builder.toString();
    }

    public static void clearHistoricalLogs(File folder, int maxDays) {
        if (folder == null) return;
        File[] childrenFiles = folder.listFiles();

        if (childrenFiles == null) return;

        Date now = new Date();

        for (File childFile : childrenFiles) {
            if (childFile.isDirectory()) {
                clearHistoricalLogs(childFile, maxDays);
                return;
            }

            Date logDate = parseDate(childFile.getName());
            if (logDate == null) continue;
            if (daysBetweenDates(now, logDate) > maxDays) {
                childFile.delete();
            }
        }
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern DATE_PATTERN = Pattern.compile("(.+?)\\.log");

    public static int daysBetweenDates(Date date1, Date date2) {
        long diff = Math.abs(date1.getTime() - date2.getTime());
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public static Date parseDate(String filename) {
        Matcher matcher = DATE_PATTERN.matcher(filename);
        if (!matcher.matches()) return null;

        try {
            String dateName = matcher.group(1);
            return DATE_FORMAT.parse(dateName);

        } catch (ParseException e) {
            return null;
        }
    }

    protected static class Formatter extends java.util.logging.Formatter {

        @Override
        public String format(LogRecord record) {
            return String.format("[%d] %s\n", record.getMillis(), record.getMessage());
        }

    }

}
