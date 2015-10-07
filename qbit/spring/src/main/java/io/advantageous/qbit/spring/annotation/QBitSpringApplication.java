package io.advantageous.qbit.spring.annotation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * Indicates a {@link Configuration configuration} class that declares one or more
 * {@link Bean @Bean} methods and also triggers {@link EnableAutoConfiguration
 * auto-configuration}, {@link EnableQBit QBit services}, {@link EnableQBitAdmin QBit Admin services},
 * {@link EnableQBitClusterSupport cluster support}, {@link EnableQBitStats statistics} and
 * {@link EnableQBitServiceEndpointServer an endpoint server}. This is a convenience
 * annotation that is equivalent to declaring all of these annotations.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Configuration
@EnableAutoConfiguration
@EnableQBit
@EnableQBitAdmin
@EnableQBitStats
@EnableQBitClusterSupport
@EnableQBitServiceEndpointServer
public @interface QBitSpringApplication {
}
