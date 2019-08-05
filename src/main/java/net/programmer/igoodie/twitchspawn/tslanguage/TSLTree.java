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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSLTree {

    private List<String> rawRules;
    private String streamer;
    private Map<String, TSLEvent> eventMap;

    public TSLTree(String script) throws TSLSyntaxErrors {
        this(null, script);
    }

    public TSLTree(String streamer, String script) throws TSLSyntaxErrors {
        try {
            this.streamer = streamer;
            this.eventMap = new TSLParser(script).parse();
            this.rawRules = TSLParser.parseRules(script);

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

    public List<String> getRawRules() {
        return rawRules;
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

    @Override
    public String toString() {
        Map<String, Integer> occurrences = new HashMap<>();

        for (String rule : rawRules) {
            try {
                List<String> words = TSLParser.parseWords(rule);
                String actionAlias = words.get(0).toUpperCase();
                Integer currentCount = occurrences.get(actionAlias);

                if (currentCount == null)
                    currentCount = 0;

                occurrences.put(actionAlias, currentCount + 1);

            } catch (TSLSyntaxError e) {
                // MUST not be able to throw a syntax error
                // Since toString() is ONLY available after construction
                // Which already handles malformed syntax
                throw new IllegalStateException("Something is seriously wrong...");
            }
        }

        StringBuilder builder = new StringBuilder();

        occurrences.forEach((actionAlias, occurrence) -> {
            if(builder.length() != 0) builder.append("\n");
            builder.append(String.format("%s action %d time(s).", actionAlias, occurrence));
        });

        return builder.toString();
    }

}
