package io.advantageous.qbit.example.spring.remote;

import io.advantageous.qbit.example.spring.common.RandomNumberServiceAsync;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.util.RemoteFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * Spring boot application that connects to a locally running {@link RandomNumberServer RandomNumberServer} and executes
 * a bunch of remote async calls.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Configuration
@EnableAutoConfiguration
public class RandomNumberClient {

    private final Logger logger = LoggerFactory.getLogger(RandomNumberClient.class);

    public static void main(String[] args) {
        SpringApplication.run(RandomNumberClient.class);
    }

    /**
     * This is the main class of the example client.  I'm using a {@link CommandLineRunner CommandLineRunner} here to
     * keep the example simple, but this could be any spring bean that needs a QBit proxy injected.
     *
     * @param randomNumberServiceAsync this is the async service that is injected by Spring
     * @return the runner instance.
     */
    @Bean
    public CommandLineRunner runner(final RandomNumberServiceAsync randomNumberServiceAsync) {
        return args -> {
            for (int i = 0; i < 100; i++) {
                randomNumberServiceAsync.getRandom(
                        CallbackBuilder.newCallbackBuilder()
                                .withCallback(n -> logger.info("Here's a random number: " + n))
                                .withErrorHandler(e -> logger.error("blew up: " + e.getMessage()))
                                .<Integer>build(),
                        0, 100
                );
            }
        };
    }

    /**
     * Creates a remote proxy to the remote service using the remote factory.  The uri is based on defaults for a
     * locally running instance of the {@link RandomNumberServer RandomNumberServer}.
     * The port and path come from the default settings in
     * {@link io.advantageous.qbit.spring.properties.ServiceEndpointServerProperties ServiceEndpointServerProperties}.
     * These defaults can be overridden by creating an application.properties or application.yml in the classpath root.
     *
     * @return an async proxy to the remote service
     */
    @Bean
    public RandomNumberServiceAsync remoteService() {
        return RemoteFactoryUtil.getRemote(
                URI.create("http://localhost:8080/services"), RandomNumberServiceAsync.class);
    }
}
