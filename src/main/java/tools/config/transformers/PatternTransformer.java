package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class PatternTransformer
        implements PropertyTransformer<Pattern> {
    /* 18 */ public static final PatternTransformer SHARED_INSTANCE = new PatternTransformer();

    public Pattern transform(String value, Field field) throws TransformationException {
        try {
            /* 31 */
            return Pattern.compile(value);
            /* 32 */
        } catch (Exception e) {
            /* 33 */
            throw new TransformationException("Not valid RegExp: " + value, e);
        }
    }
}
