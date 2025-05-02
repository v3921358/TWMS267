package tools.config;

import tools.config.transformers.PropertyTransformer;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String DEFAULT_VALUE = "DO_NOT_OVERWRITE_INITIALIAZION_VALUE";

    String key();

    Class<? extends PropertyTransformer> propertyTransformer() default PropertyTransformer.class;

    String defaultValue() default "DO_NOT_OVERWRITE_INITIALIAZION_VALUE";
}
