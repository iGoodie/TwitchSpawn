package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import java.util.List;

public class TSLSyntaxError extends Exception {

    public static final int RULE_LENGTH_LIMIT = 50;

    private String associatedRule;

    public TSLSyntaxError(String messageFormat, Object... args) {
        super(args.length == 0 ? messageFormat : String.format(messageFormat, args));
    }

    public void setAssociatedRule(String associatedRule) {
        this.associatedRule = associatedRule;
    }

    public void setAssociatedRule(List<String> associatedWords) {
        this.associatedRule = TSLTokenizer.buildRule(associatedWords);
    }

    @Override
    public String getMessage() {
        if (associatedRule != null) {
            return super.getMessage()
                    + " \non rule \u00A7c\u00A7o"
                    + associatedRule();
        }

        return super.getMessage();
    }

    private String associatedRule() {
        if (associatedRule.length() <= RULE_LENGTH_LIMIT)
            return associatedRule;

        return associatedRule.substring(0, RULE_LENGTH_LIMIT / 2).trim() + "......"
                + associatedRule.substring(associatedRule.length() - RULE_LENGTH_LIMIT / 2).trim();
    }

}
