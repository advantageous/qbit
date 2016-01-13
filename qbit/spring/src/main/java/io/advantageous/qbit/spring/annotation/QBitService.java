package io.advantageous.qbit.spring.annotation;

import java.lang.annotation.*;

/**
 * This is used to mark a spring factory method that creates a QBit service.  This annotation is read to automatically
 * create service queues.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QBitService {

    boolean exposeRemoteEndpoint() default false;

    boolean remoteEventListener() default false;

    Class asyncInterface() default NoAsyncInterface.class;
}
