package io.advantageous.qbit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Consume {


    /* The channel you want to listen to. */;
    String value() default "";

    /* The consume is the last object listening to this event.
       An event channel can have many subscribers but only one consume.
     */
    boolean consume() default true;


}