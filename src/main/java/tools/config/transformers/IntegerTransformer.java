package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class IntegerTransformer
        implements PropertyTransformer<Integer> {
    /* 17 */ public static final IntegerTransformer SHARED_INSTANCE = new IntegerTransformer();

    public Integer transform(String value, Field field) throws TransformationException {
        try {
            /* 30 */
            return Integer.decode(value);
            /* 31 */
        } catch (Exception e) {
            /* 32 */
            throw new TransformationException(e);
        }
    }
}
