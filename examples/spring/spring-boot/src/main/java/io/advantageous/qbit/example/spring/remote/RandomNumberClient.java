package io.advantageous.qbit.example.spring.remote;

import io.advantageous.qbit.example.spring.common.RandomNumberServiceAsync;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.util.RemoteFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@EnableAutoConfiguration
public class RandomNumberClient {

    private final Logger logger = LoggerFactory.getLogger(RandomNumberClient.class);

    public static void main(String[] args) {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        SpringApplication.run(RandomNumberClient.class);
    }

    @Bean
    public CommandLineRunner runner(final RandomNumberServiceAsync randomNumberServiceAsync) {
        return args -> {
            for (int a = 0; a < 100; a++) {
                randomNumberServiceAsync.getRandom(
                        CallbackBuilder.newCallbackBuilder()
                                .withCallback(l -> logger.info("Here's a random number: " + l))
                                .withErrorHandler(e -> logger.error("blew up: " + e.getMessage()))
                                .<Integer>build(),
                        0, 100
                );
            }
        };
    }

    @Bean
    public RandomNumberServiceAsync remoteService() {
        return RemoteFactoryUtil.getRemote(
                URI.create("http://localhost:8080/services"), RandomNumberServiceAsync.class);
    }
}
