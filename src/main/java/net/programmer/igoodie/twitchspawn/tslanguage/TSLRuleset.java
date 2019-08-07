package net.programmer.igoodie.twitchspawn.tslanguage;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSLRuleset {

    private List<String> rawRules;
    private String streamer;
    private Map<String, TSLEvent> eventMap;

    public TSLRuleset(String script) throws TSLSyntaxErrors {
        this(null, script);
    }

    public TSLRuleset(String streamer, String script) throws TSLSyntaxErrors {
        try {
            this.streamer = streamer;
            this.eventMap = new TSLParser(script).parse();
            this.rawRules = TSLTokenizer.intoRules(script);

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
        String eventAlias = TSLEventKeyword.ofPair(args.eventType, args.eventFor);

        // Cannot resolve event alias
        if (eventAlias == null)
            throw new InternalError("Handler was called with invalid event arguments "
                    + new TSLEventPair(args.eventType, args.eventFor));

        TSLEvent event = eventMap.get(eventAlias.toLowerCase());

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
                List<String> words = TSLTokenizer.intoWords(rule);
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
