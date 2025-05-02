package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class StringTransformer
        implements PropertyTransformer<String> {
    /* 17 */ public static final StringTransformer SHARED_INSTANCE = new StringTransformer();

    public String transform(String value, Field field) throws TransformationException {
        /* 29 */
        return value;
    }
}
