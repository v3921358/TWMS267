package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class CharTransformer
        implements PropertyTransformer<Character> {
    /* 15 */ public static final CharTransformer SHARED_INSTANCE = new CharTransformer();

    public Character transform(String value, Field field) throws TransformationException {
        try {
            /* 28 */
            char[] chars = value.toCharArray();
            /* 29 */
            if (chars.length > 1) {
                /* 30 */
                throw new TransformationException("To many characters in the value");
            }
            /* 32 */
            return Character.valueOf(chars[0]);
            /* 33 */
        } catch (Exception e) {
            /* 34 */
            throw new TransformationException(e);
        }
    }
}
