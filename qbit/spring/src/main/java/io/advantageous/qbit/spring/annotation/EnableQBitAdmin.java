package io.advantageous.qbit.spring.annotation;

import io.advantageous.qbit.spring.config.AdminConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable qbit admin service and health checks.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({AdminConfiguration.class})
public @interface EnableQBitAdmin {
}
