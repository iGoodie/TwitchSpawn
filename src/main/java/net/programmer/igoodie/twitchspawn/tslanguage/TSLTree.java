package net.programmer.igoodie.twitchspawn.tslanguage;

import com.google.common.collect.Lists;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class TSLTree {

    private String streamer;
    private Map<String, TSLEvent> eventMap;

    public TSLTree(String script) throws TSLSyntaxErrors {
        this(null, script);
    }

    public TSLTree(String streamer, String script) throws TSLSyntaxErrors {
        try {
            this.streamer = streamer;
            this.eventMap = new TSLParser(script).parse();

        } catch (TSLSyntaxError e) {
            throw new TSLSyntaxErrors(e);
        }
    }

    public String getStreamer() {
        return streamer;
    }

    public TSLEvent getEventHandler(String eventAlias) {
        return eventMap.get(eventAlias);
    }

    public boolean handleEvent(EventArguments args) {
        String eventAlias = TSLEvent.getEventAlias(args.eventType, args.eventFor);

        // Cannot resolve event alias
        if (eventAlias == null)
            throw new InternalError("Handler was called with invalid event arguments "
                    + new TSLEventPair(args.eventType, args.eventFor));

        TSLEvent event = eventMap.get(eventAlias);

        // No handler was bound, skip handling
        if (event == null)
            return false;

        // Pass the args to the TSLEvent
        TwitchSpawn.LOGGER.debug("Processing {} -> {}", eventAlias, args);
        return event.process(args);
    }

}
