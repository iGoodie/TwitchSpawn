package net.programmer.igoodie.twitchspawn.tslanguage;

import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLTokenizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSLRuleset {

    private String streamer;
    private List<String> rulesRaw;
    private Map<String, TSLEvent> eventMap;

    public TSLRuleset(String script) throws TSLSyntaxErrors {
        this(null, script);
    }

    public TSLRuleset(String streamer, String script) throws TSLSyntaxErrors {
        try {
            this.streamer = streamer;
            this.eventMap = new TSLParser(script).parse();
            this.rulesRaw = TSLTokenizer.intoRules(script);

        } catch (TSLSyntaxError e) {
            throw new TSLSyntaxErrors(e);
        }
    }

    public String getStreamer() {
        return streamer;
    }

    public TSLEvent getEventHandler(String eventKeyword) {
        return eventMap.get(eventKeyword.toLowerCase());
    }

    public List<String> getRulesRaw() {
        return rulesRaw;
    }

    public Collection<TSLEvent> getEvents() {
        return eventMap.values();
    }

    public boolean willPerform(EventArguments args) {
        for (TSLEvent event : eventMap.values()) {
            if (event.willPerform(args)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        Map<String, Integer> occurrences = new HashMap<>();

        for (String rule : rulesRaw) {
            try {
                List<String> words = TSLTokenizer.intoWords(rule);
                String actionKeyword = words.get(0).toUpperCase();
                Integer currentCount = occurrences.getOrDefault(actionKeyword, 0);

                occurrences.put(actionKeyword, currentCount + 1);

            } catch (TSLSyntaxError e) {
                // MUST not be able to throw a syntax error
                // Since toString() is ONLY available after construction
                // Which already handles malformed syntax
                throw new IllegalStateException("Something is seriously wrong...");
            }
        }

        StringBuilder builder = new StringBuilder();

        occurrences.forEach((actionKeyword, occurrence) -> {
            if (builder.length() != 0) builder.append("\n");
            builder.append(String.format("%s action %d time(s).", actionKeyword, occurrence));
        });

        return builder.toString();
    }

}
