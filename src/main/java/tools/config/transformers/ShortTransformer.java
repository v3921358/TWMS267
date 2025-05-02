package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class ShortTransformer
        implements PropertyTransformer<Short> {
    /* 17 */ public static final ShortTransformer SHARED_INSTANCE = new ShortTransformer();

    public Short transform(String value, Field field) throws TransformationException {
        try {
            /* 30 */
            return Short.decode(value);
            /* 31 */
        } catch (Exception e) {
            /* 32 */
            throw new TransformationException(e);
        }
    }
}
