package io.advantageous.qbit.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a path variable.
 * @author rhightower
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.PARAMETER})
public @interface PathVariable {

    String value() default "";
}
