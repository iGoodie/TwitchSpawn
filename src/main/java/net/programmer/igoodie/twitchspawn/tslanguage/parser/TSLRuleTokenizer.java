package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import java.util.LinkedList;
import java.util.List;

public class TSLRuleTokenizer {

    public static final String DISPLAY_KEYWORD = "DISPLAYING";
    public static final String EVENT_KEYWORD = "ON";
    public static final String PREDICATE_KEYWORD = "WITH";

    /* ---------------------------- */

    private List<String> words;
    private int wordCursor = 0;

    private String actionName;
    private List<String> actionParameters;
    private String eventName;
    private List<List<String>> predicateParameters;

    public TSLRuleTokenizer(List<String> words) {
        this.words = words;
    }

    public String getActionName() {
        return actionName;
    }

    public List<String> getActionParameters() {
        return actionParameters;
    }

    public String getEventName() {
        return eventName;
    }

    public List<List<String>> getPredicateParameters() {
        return predicateParameters;
    }

    private String currentWord() {
        if (wordCursor >= words.size())
            return null;
        return words.get(wordCursor);
    }

    /* ---------------------------- */

    public TSLRuleTokenizer intoParts() throws TSLSyntaxError {
        validate();
        tokenizeAction();
        tokenizeEvent();

        this.predicateParameters = new LinkedList<>();
        while (currentWord() != null)
            tokenizePredicateWords();

        return this;
    }

    private void tokenizeAction() {
        String word = currentWord();

        actionName = word;
        actionParameters = new LinkedList<>();

        wordCursor++; // Proceed to expected action arguments

        // <ACTION> foo bar baz [DISPLAYING ..] ON ...
        while (true) {
            word = currentWord();

            if (word.equalsIgnoreCase(EVENT_KEYWORD))
                break;

            wordCursor++;
            actionParameters.add(word);
        }
    }

    private void tokenizeEvent() throws TSLSyntaxError {
        if (!currentWord().equalsIgnoreCase(EVENT_KEYWORD))
            throw new TSLSyntaxError("Expected %s keyword, found word -> %s", EVENT_KEYWORD, currentWord());

        wordCursor++; // Proceed to expected Event name

        StringBuilder eventName = new StringBuilder();
        while (currentWord() != null && !currentWord().equalsIgnoreCase(PREDICATE_KEYWORD)) {
            eventName.append(currentWord()).append(TSLTokenizer.SPACE);
            wordCursor++;
        }

        this.eventName = eventName.toString().trim();

        if (this.eventName.isEmpty())
            throw new TSLSyntaxError("Expected event name after %s keyword", EVENT_KEYWORD);
    }

    public void tokenizePredicateWords() throws TSLSyntaxError {
        if (!currentWord().equalsIgnoreCase(PREDICATE_KEYWORD))
            throw new TSLSyntaxError("Expected %s keyword, but found -> %s", PREDICATE_KEYWORD, currentWord());

        wordCursor++; // Proceed to expected first predicate word

        List<String> predicateWords = new LinkedList<>();
        while (currentWord() != null && !currentWord().equalsIgnoreCase(PREDICATE_KEYWORD)) {
            predicateWords.add(currentWord());
            wordCursor++;
        }

        if (predicateWords.size() == 0)
            throw new TSLSyntaxError("Expected a word after %s keyword", PREDICATE_KEYWORD);

        predicateParameters.add(predicateWords);
    }

    /* ---------------------------- */

    private void validate() throws TSLSyntaxError {
        int indexDisplay = lastIndexOfWord(DISPLAY_KEYWORD);
        int indexEvent = lastIndexOfWord(EVENT_KEYWORD);
        int indexPredicate = lastIndexOfWord(PREDICATE_KEYWORD);

        if (indexEvent == -1)
            throw new TSLSyntaxError("Expected at least one %s statement.", EVENT_KEYWORD);

        if (indexDisplay != -1) {
            if (indexDisplay >= indexEvent)
                throw new TSLSyntaxError("Found %s statement on unexpected location.", DISPLAY_KEYWORD);
        }

        if (indexPredicate != -1) {
            if (indexEvent >= indexPredicate)
                throw new TSLSyntaxError("Found %s statement on unexpected location.", EVENT_KEYWORD);
        }

        if (words.size() < 3)
            throw new TSLSyntaxError("Unexpected length of words -> %d", words.size());
    }

    private int lastIndexOfWord(String word) {
        for (int i = words.size() - 1; i >= 0; i--) {
            if (words.get(i).equalsIgnoreCase(word))
                return i;
        }
        return -1;
    }

}
