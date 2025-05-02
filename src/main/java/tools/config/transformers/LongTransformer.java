package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class LongTransformer
        implements PropertyTransformer<Long> {
    /* 15 */ public static final LongTransformer SHARED_INSTANCE = new LongTransformer();

    public Long transform(String value, Field field) throws TransformationException {
        try {
            /* 28 */
            return Long.decode(value);
            /* 29 */
        } catch (Exception e) {
            /* 30 */
            throw new TransformationException(e);
        }
    }
}
