package io.advantageous.qbit.spring.annotation;

import io.advantageous.qbit.spring.config.ClusterConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable service discovery and distributed event bus.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ClusterConfiguration.class})
public @interface EnableQBitClusterSupport {
}
