package net.programmer.igoodie.twitchspawn.util;

import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

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

    public static String fromArgs(String expression, EventArguments args) {
        if (expression.equals("event"))
            return args.eventName;

        if (expression.equals("actor"))
            return args.actorNickname;

        if (expression.equals("streamer"))
            return args.streamerNickname;

        if (expression.equals("amount") && args.donationAmount != 0.0)
            return String.valueOf(args.donationAmount);

        if (expression.equals("amount_i") && args.donationAmount != 0.0)
            return String.valueOf((int) args.donationAmount);

        if (expression.equals("amount_f") && args.donationAmount != 0.0)
            return String.format("%.2f", args.donationAmount);

        if (expression.equals("currency") && args.donationCurrency != null)
            return args.donationCurrency;

        if (expression.equals("months") && args.subscriptionMonths != 0)
            return String.valueOf(args.subscriptionMonths);

        if (expression.equals("viewers") && args.viewerCount != 0)
            return String.valueOf(args.viewerCount);

        if (expression.equals("raiders") && args.raiderCount != 0)
            return String.valueOf(args.raiderCount);

        if (expression.equals("time"))
            return new SimpleDateFormat("HH:mm:ss").format(new Date());

        return null;
    }

}
