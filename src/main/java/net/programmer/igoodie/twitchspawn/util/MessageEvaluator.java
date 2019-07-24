package net.programmer.igoodie.twitchspawn.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageEvaluator {

    public static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    public static String replaceExpressions(String input, Function<String, String> evaluator) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        int start = 0;

        while (matcher.find()) {
            String expression = matcher.group(1);

            // Append previous part
            sb.append(input, start, matcher.start());
            start = matcher.end();

            // Evaluate and append new value
            sb.append(evaluator.apply(expression));
        }

        // Append trailing chars
        sb.append(input, start, input.length());

        return sb.toString();
    }

}
