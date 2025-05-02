package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class ClassTransformer
        implements PropertyTransformer<Class<?>> {
    /* 22 */ public static final ClassTransformer SHARED_INSTANCE = new ClassTransformer();

    public Class<?> transform(String value, Field field) throws TransformationException {
        try {
            /* 27 */
            return Class.forName(value, false, getClass().getClassLoader());
            /* 28 */
        } catch (ClassNotFoundException e) {
            /* 29 */
            throw new TransformationException("Cannot find class with name '" + value + "'");
        }
    }
}
