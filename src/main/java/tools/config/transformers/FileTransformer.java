package tools.config.transformers;

import tools.config.TransformationException;

import java.io.File;
import java.lang.reflect.Field;

public class FileTransformer
        implements PropertyTransformer<File> {
    /* 18 */ public static final FileTransformer SHARED_INSTANCE = new FileTransformer();

    public File transform(String value, Field field) throws TransformationException {
        /* 29 */
        return new File(value);
    }
}
