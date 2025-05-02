package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class DoubleTransformer
        implements PropertyTransformer<Double> {
    /* 17 */ public static final DoubleTransformer SHARED_INSTANCE = new DoubleTransformer();

    public Double transform(String value, Field field) throws TransformationException {
        try {
            /* 30 */
            return Double.valueOf(Double.parseDouble(value));
            /* 31 */
        } catch (Exception e) {
            /* 32 */
            throw new TransformationException(e);
        }
    }
}
