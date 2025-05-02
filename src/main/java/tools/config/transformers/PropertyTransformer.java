package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;

public interface PropertyTransformer<T> {
    T transform(String paramString, Field paramField) throws TransformationException;
}

