package net.programmer.igoodie.twitchspawn.tslanguage.keyword;

import net.programmer.igoodie.twitchspawn.tslanguage.action.*;

public enum TSLActionKeyword {

    DROP(DropAction.class),
    SUMMON(SummonAction.class),
    EXECUTE(ExecuteAction.class),

    EITHER(EitherAction.class),
    BOTH(BothAction.class),
    NOTHING(NothingAction.class),
    ;

    public static boolean exists(String actionName) {
        try {
            valueOf(actionName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Class<? extends TSLAction> toClass(String actionName) {
        if (!exists(actionName))
            return null;

        return valueOf(actionName.toUpperCase()).actionClass;
    }

    public static String ofClass(Class<? extends TSLAction> actionClass) {
        for (TSLActionKeyword keyword : values()) {
            if (keyword.actionClass.equals(actionClass))
                return keyword.name();
        }
        return null;
    }

    /* --------------------------- */

    public final Class<? extends TSLAction> actionClass;

    TSLActionKeyword(Class<? extends TSLAction> actionClass) {
        this.actionClass = actionClass;
    }
}
