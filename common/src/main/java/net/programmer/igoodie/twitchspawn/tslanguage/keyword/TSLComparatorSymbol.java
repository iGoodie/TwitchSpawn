package net.programmer.igoodie.twitchspawn.tslanguage.keyword;

import net.programmer.igoodie.twitchspawn.tslanguage.predicate.*;

public enum TSLComparatorSymbol {

    IN_RANGE(
            "IN RANGE", InRangeComparator.class),
    CONTAINS(
            "CONTAINS", ContainsComparator.class),
    IS(
            "IS", IsComparator.class),
    PREFIX(
            "PREFIX", PrefixComparator.class),
    POSTFIX(
            "POSTFIX", PostfixComparator.class),
    EQUALS(
            "=", EqualsComparator.class),
    GREATER_THAN(
            ">", GreaterThanComparator.class),
    GREATER_THAN_OR_EQUAL_TO(
            ">=", GreaterThanOrEqComparator.class),
    LESS_THAN(
            "<", LessThanComparator.class),
    LESS_THAN_OR_EQUAL_TO(
            "<=", LessThanOrEqComparator.class),
    ;

    public static Class<? extends TSLComparator> toClass(String symbol) {
        for (TSLComparatorSymbol comparatorSymbol : values()) {
            if (comparatorSymbol.symbol.equalsIgnoreCase(symbol.toUpperCase()))
                return comparatorSymbol.comparatorClass;
        }
        return null;
    }

    public static String ofClass(Class<? extends TSLComparator> comparatorClass) {
        for (TSLComparatorSymbol comparatorSymbol : values()) {
            if (comparatorSymbol.comparatorClass.equals(comparatorClass))
                return comparatorSymbol.symbol;
        }
        return null;
    }

    /* ------------------------------- */

    public final String symbol;
    public final Class<? extends TSLComparator> comparatorClass;

    TSLComparatorSymbol(String symbol, Class<? extends TSLComparator> comparatorClass) {
        this.symbol = symbol;
        this.comparatorClass = comparatorClass;
    }

}
