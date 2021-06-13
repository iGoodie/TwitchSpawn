package net.programmer.igoodie.twitchspawn.logger;

import net.programmer.igoodie.twitchspawn.log.ConsoleLogger;
import org.junit.jupiter.api.Test;

public class LoggerTests {

    @Test
    public void testConsoleLogger() {
        ConsoleLogger consoleLogger = new ConsoleLogger(LoggerTests.class);
        consoleLogger.info("Foo");
        consoleLogger.info("Foo {}", "bar");
    }

}
