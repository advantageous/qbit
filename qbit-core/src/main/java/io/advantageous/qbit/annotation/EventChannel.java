package io.advantageous.qbit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rhightower on 2/11/15.
 */

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EventChannel {
    /* The channel you want to listen to. */;
    String value();

    boolean appendMethodName() default false;
}
