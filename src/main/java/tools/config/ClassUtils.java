package tools.config;

public class ClassUtils {
    public static boolean isSubclass(Class<?> a, Class<?> b) {
        /* 23 */
        if (a == b) {
            /* 24 */
            return true;
        }
        /* 26 */
        if (a == null || b == null) {
            /* 27 */
            return false;
        }
        /* 29 */
        for (Class<?> x = a; x != null; x = x.getSuperclass()) {
            /* 30 */
            if (x == b) {
                /* 31 */
                return true;
            }
            /* 33 */
            if (b.isInterface()) {
                /* 34 */
                Class<?>[] interfaces = x.getInterfaces();
                /* 35 */
                for (Class<?> anInterface : interfaces) {
                    /* 36 */
                    if (isSubclass(anInterface, b)) {
                        /* 37 */
                        return true;
                    }
                }
            }
        }
        /* 42 */
        return false;
    }
}
