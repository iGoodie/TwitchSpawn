package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TSLPredicate implements TSLFlowNode {

    private static Map<String, Field> FIELD_ALIASES;

    public static void loadFieldAliases() {
        FIELD_ALIASES = new HashMap<>();

        loadMultipleAliases(EventArguments.getField("donationCurrency"), "currency", "donation_currency");
        loadMultipleAliases(EventArguments.getField("donationAmount"), "amount", "donation_amount");
        loadMultipleAliases(EventArguments.getField("subscriptionMonths"), "months", "#months", "subscription_months");
        loadMultipleAliases(EventArguments.getField("viewerCount"), "#viewer", "viewer_count");
        loadMultipleAliases(EventArguments.getField("raiderCount"), "#raider", "raider_count");
    }

    private static void loadMultipleAliases(Field field, String... aliases) {
        Stream.of(aliases).forEach(alias -> {
            FIELD_ALIASES.put(alias, field);
            TwitchSpawn.LOGGER.info("Loaded alias {} -> {}",
                    alias, field.getName());
        });
    }

    private static Object extractValue(EventArguments args, String fieldAlias) {
        return null; // TODO
    }

    /* ----------------------------------- */

    public TSLComparator comparator;
    public String fieldAlias;
    private TSLFlowNode next;

    public TSLPredicate(String fieldAlias, TSLComparator comparator) throws TSLSyntaxError {
        if (!FIELD_ALIASES.containsKey(fieldAlias))
            throw new TSLSyntaxError("Unexpected predicate field alias -> " + fieldAlias);

        this.fieldAlias = fieldAlias;
        this.comparator = comparator;
    }

    @Override
    public TSLFlowNode chain(TSLFlowNode next) {
        this.next = next;
        return next;
    }

    @Override
    public boolean process(EventArguments args) {
        System.out.println("Processing " + getClass().getSimpleName());

        Object value = extractValue(args, fieldAlias);

        if (comparator.compare(value))
            return next.process(args);

        return false;
    }

}
