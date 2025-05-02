package Handler;

import Opcode.Headler.InHeader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    InHeader op() default InHeader.UNKNOWN;
    InHeader[] ops() default {};
}
