package io.advantageous.qbit.spring.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * AutoFlush for a QBit service.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author gcc@rd.io (Geoff Chandler)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFlush {

    int interval() default 100;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
