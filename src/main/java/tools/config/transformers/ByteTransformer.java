package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public class ByteTransformer
        implements PropertyTransformer<Byte> {
    /* 18 */ public static final ByteTransformer SHARED_INSTANCE = new ByteTransformer();

    public Byte transform(String value, Field field) throws TransformationException {
        try {
            /* 31 */
            return Byte.decode(value);
            /* 32 */
        } catch (Exception e) {
            /* 33 */
            throw new TransformationException(e);
        }
    }
}
