package net.programmer.igoodie.twitchspawn.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsoleLogger extends TSLogger {

    protected Logger logger;

    public ConsoleLogger(Class<?> target) {
        this.logger = LogManager.getLogger(target);
    }

    @Override
    public void info(String msg, Object... args) {
        logger.info(msg, args);
    }

    @Override
    public void warn(String msg, Object... args) {
        logger.warn(msg, args);
    }

    @Override
    public void error(String msg, Object... args) {
        logger.error(msg, args);
    }

    @Override
    public void debug(String msg, Object... args) {
        logger.debug(msg, args);
    }

    @Override
    public void trace(String msg, Object... args) {
        logger.trace(msg, args);
    }
}
