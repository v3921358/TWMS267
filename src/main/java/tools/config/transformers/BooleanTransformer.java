package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class BooleanTransformer
        implements PropertyTransformer<Boolean> {
    /* 20 */ public static final BooleanTransformer SHARED_INSTANCE = new BooleanTransformer();

    public Boolean transform(String value, Field field) throws TransformationException {
        /* 35 */
        if ("true".equalsIgnoreCase(value) || "1".equals(value))
            /* 36 */ return Boolean.valueOf(true);
        /* 37 */
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            /* 38 */
            return Boolean.valueOf(false);
        }
        /* 40 */
        throw new TransformationException("Invalid boolean string: " + value);
    }
}
