package tools.json;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@interface JSONPropertyName {
    String value();
}

