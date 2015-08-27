package io.advantageous.qbit.spring.annotation;

import org.springframework.context.annotation.Primary;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier annotation for publishing event channel injections.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Primary
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface QBitPublisher {
}
