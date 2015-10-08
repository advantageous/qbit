package io.advantageous.qbit.example.spring;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.spring.annotation.AutoFlush;
import io.advantageous.qbit.spring.annotation.EnableQBit;
import io.advantageous.qbit.spring.annotation.QBitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;

/**
 * Example of an async service with a callback.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@EnableQBit
@SpringBootApplication
public class RandomNumberExample {

    private final Logger logger = LoggerFactory.getLogger(RandomNumberExample.class);

    public static void main(String[] args) {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        SpringApplication.run(RandomNumberExample.class);
    }

    @Bean
    public CommandLineRunner runner(final RandomNumberServiceAsync randomNumberServiceAsync) {
        return args -> {
            for (int a = 0; a < 100; a++) {
                randomNumberServiceAsync.getRandom(
                        CallbackBuilder.newCallbackBuilder()
                                .withCallback(l -> logger.info("Here's a random number: " + l))
                                .withErrorHandler(e -> logger.error("blew up: " + e.getCause()))
                                .<Integer>build(),
                        0, 100
                );
            }
        };
    }

    @Bean
    @AutoFlush
    @QBitService(asyncInterface = RandomNumberServiceAsync.class)
    public RandomNumberService randomNumberService() {
        return (min, max) -> {
            final Random random = new Random();
            final int result = random.nextInt(max - min + 1) + min;
            if (String.valueOf(result).contains("7")) throw new RuntimeException("Oh no!  Not a seven!");
            return result;
        };
    }

    private interface RandomNumberService {
        int getRandom(int min, int max);
    }

    private interface RandomNumberServiceAsync {
        void getRandom(Callback<Integer> callback, int min, int max);
    }

}
