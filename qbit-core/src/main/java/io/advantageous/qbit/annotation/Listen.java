package io.advantageous.qbit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rhightower on 2/3/15.
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Listen {

    /* The channel you want to listen to. */;
    String value() default "";

    /* The consume is the last object listening to this event.
       An event channel can have many subscribers but only one consume.
     */
    boolean consume() default false;

}
