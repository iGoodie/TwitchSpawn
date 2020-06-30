package net.programmer.igoodie.twitchspawn.util;

public class TSHelper {

    public static boolean truthy(Object object) {
        if (object == null) return false;
        if ((object instanceof String) && ((String) object).isEmpty()) return false;
        if ((object instanceof Number) && ((Number) object).doubleValue() == 0.0) return false;
        return (!(object instanceof Boolean)) || ((Boolean) object);
    }

    public static String jslikeOr(String... values) {
        for (String value : values) {
            if (truthy(value))
                return value;
        }
        return null;
    }

}
