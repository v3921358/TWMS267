package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class FloatTransformer
        implements PropertyTransformer<Float> {
    /* 17 */ public static final FloatTransformer SHARED_INSTANCE = new FloatTransformer();

    public Float transform(String value, Field field) throws TransformationException {
        try {
            /* 30 */
            return Float.valueOf(Float.parseFloat(value));
            /* 31 */
        } catch (Exception e) {
            /* 32 */
            throw new TransformationException(e);
        }
    }
}
