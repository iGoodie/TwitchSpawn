package net.programmer.igoodie.twitchspawn.tslanguage.keyword;

import net.programmer.igoodie.twitchspawn.tslanguage.action.*;

public enum TSLActionKeyword {

    DROP(true, DropAction.class),
    SUMMON(true, SummonAction.class),
    THROW(true, ThrowAction.class),
    CLEAR(true, ClearAction.class),
    EXECUTE(true, ExecuteAction.class),
    SHUFFLE(true, ShuffleAction.class),
    CHANGE(true, ChangeAction.class),
    NOTHING(true, NothingAction.class),

    EITHER(false, EitherAction.class),
    BOTH(false, BothAction.class),
    FOR(false, ForAction.class),
    WAIT(true, WaitAction.class),
    REFLECT(false, ReflectAction.class),

    OS_RUN(true, OsRunAction.class),
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

    public final boolean displayable;
    public final Class<? extends TSLAction> actionClass;

    TSLActionKeyword(boolean displayable, Class<? extends TSLAction> actionClass) {
        this.displayable = displayable;
        this.actionClass = actionClass;
    }
}
