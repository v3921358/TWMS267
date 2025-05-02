package Config.configs;

import tools.config.TransformationException;
import tools.config.transformers.PropertyTransformer;

import java.lang.reflect.Field;

public interface getIntegerTransformer extends PropertyTransformer<Integer> {
    Integer transform(String value);

    @Override
    Integer transform(String s, Field field) throws TransformationException;
}
