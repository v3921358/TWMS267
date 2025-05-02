package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class EnumTransformer
        implements PropertyTransformer<Enum<?>> {
    /* 26 */ public static final EnumTransformer SHARED_INSTANCE = new EnumTransformer();

    public Enum<?> transform(String value, Field field) throws TransformationException {
        /* 40 */
        Class<? extends Enum> clazz = (Class) field.getType();

        try {
            /* 43 */
            return Enum.valueOf((Class) clazz, value);
            /* 44 */
        } catch (Exception e) {
            /* 45 */
            throw new TransformationException(e);
        }
    }
}
