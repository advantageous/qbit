package io.advantageous.qbit.spring.annotation;

import io.advantageous.qbit.spring.config.StatsConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable QBit stats infrastructure.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({StatsConfiguration.class})
public @interface EnableQBitStats {
}
