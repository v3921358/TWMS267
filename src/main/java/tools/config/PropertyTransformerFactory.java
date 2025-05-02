
package tools.config;

import tools.config.transformers.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public class PropertyTransformerFactory {
    public static PropertyTransformer newTransformer(Class<?> class1, Class<? extends PropertyTransformer> tc)
            throws TransformationException {
        /* 18 */
        if (tc == PropertyTransformer.class) {
            /* 19 */
            tc = null;
        }
        /* 21 */
        if (tc != null) {
            try {
                /* 23 */
                return tc.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                /* 24 */
            } catch (Exception e) {
                /* 25 */
                throw new TransformationException("Can't instantiate property transformer", e);
            }
        }
        /* 28 */
        if (class1 == Boolean.class || class1 == boolean.class)
            /* 29 */ return (PropertyTransformer) BooleanTransformer.SHARED_INSTANCE;
        /* 30 */
        if (class1 == Byte.class || class1 == byte.class)
            /* 31 */ return (PropertyTransformer) ByteTransformer.SHARED_INSTANCE;
        /* 32 */
        if (class1 == Character.class || class1 == char.class)
            /* 33 */ return (PropertyTransformer) CharTransformer.SHARED_INSTANCE;
        /* 34 */
        if (class1 == Double.class || class1 == double.class)
            /* 35 */ return (PropertyTransformer) DoubleTransformer.SHARED_INSTANCE;
        /* 36 */
        if (class1 == Float.class || class1 == float.class)
            /* 37 */ return (PropertyTransformer) FloatTransformer.SHARED_INSTANCE;
        /* 38 */
        if (class1 == Integer.class || class1 == int.class)
            /* 39 */ return (PropertyTransformer) IntegerTransformer.SHARED_INSTANCE;
        /* 40 */
        if (class1 == Long.class || class1 == long.class)
            /* 41 */ return (PropertyTransformer) LongTransformer.SHARED_INSTANCE;
        /* 42 */
        if (class1 == Short.class || class1 == short.class)
            /* 43 */ return (PropertyTransformer) ShortTransformer.SHARED_INSTANCE;
        /* 44 */
        if (class1 == String.class)
            /* 45 */ return (PropertyTransformer) StringTransformer.SHARED_INSTANCE;
        /* 46 */
        if (class1.isEnum()) {
            /* 47 */
            return (PropertyTransformer) EnumTransformer.SHARED_INSTANCE;
        }

        /* 53 */
        if (class1 == File.class)
            /* 54 */ return (PropertyTransformer) FileTransformer.SHARED_INSTANCE;
        /* 55 */
        if (ClassUtils.isSubclass(class1, InetSocketAddress.class))
            /* 56 */ return (PropertyTransformer) InetSocketAddressTransformer.SHARED_INSTANCE;
        /* 57 */
        if (class1 == Pattern.class)
            /* 58 */ return (PropertyTransformer) PatternTransformer.SHARED_INSTANCE;
        /* 59 */
        if (class1 == Class.class) {
            /* 60 */
            return (PropertyTransformer) ClassTransformer.SHARED_INSTANCE;
        }
        /* 62 */
        throw new TransformationException("Transformer not found for class " + class1.getName());
    }
}
