package tools.config;

import java.util.Collection;
import java.util.Map;

public class GenericValidator {
    public static boolean isBlankOrNull(String s) {
        /* 9 */
        return (s == null || s.isEmpty());
    }

    public static boolean isBlankOrNull(Collection<?> c) {
        /* 13 */
        return (c == null || c.isEmpty());
    }

    public static boolean isBlankOrNull(Map<?, ?> m) {
        /* 17 */
        return (m == null || m.isEmpty());
    }

    public static boolean isBlankOrNull(Number n) {
        /* 21 */
        return (n == null || n.doubleValue() == 0.0D);
    }

    public static boolean isBlankOrNull(Object[] a) {
        /* 25 */
        return (a == null || a.length == 0);
    }
}
