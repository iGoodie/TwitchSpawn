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

    public static void loadPropertyAliases() {
        FIELD_ALIASES = new HashMap<>();

        loadAliases(EventArguments.getField("donationCurrency"),
                "currency", "donation_currency");
        loadAliases(EventArguments.getField("donationAmount"),
                "amount", "donation_amount");
        loadAliases(EventArguments.getField("subscriptionMonths"),
                "months", "#months", "subscription_months");
        loadAliases(EventArguments.getField("viewerCount"),
                "#viewer", "viewer_count");
        loadAliases(EventArguments.getField("raiderCount"),
                "#raider", "raider_count");
        // TODO: Include "chance" field, which needs no extraction
    }

    private static void loadAliases(Field field, String... aliases) {
        Stream.of(aliases).forEach(alias -> {
            FIELD_ALIASES.put(alias, field);
            TwitchSpawn.LOGGER.debug("Loaded TSLPredicate property alias: {} -> {}", alias, field.getName());
        });
    }

    public static Object extractValue(EventArguments args, String fieldAlias) {
        Field field = FIELD_ALIASES.get(fieldAlias);

        if (field == null)
            throw new InternalError("Invalid field alias " + fieldAlias + " was bound to the predicate.");

        try {
            return field.get(args);

        } catch (IllegalAccessException e) {
            throw new InternalError("Invalid field alias " + fieldAlias + " was bound to the predicate.");
        }
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
        TwitchSpawn.LOGGER.debug("Reached TSLPredicate node -> {} with {}",
                comparator.getClass().getSimpleName(), args);

        Object value = extractValue(args, fieldAlias);

        if (comparator.compare(value))
            return next.process(args);

        return false;
    }

}
