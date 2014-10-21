package io.advantageous.qbit.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 * Exposes a method as a ServiceMethod, i.e., a method that can be accessed from the outside world.
 * @author Rick Hightower
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public  @interface  ServiceMethod {

    String value() default "";
}
