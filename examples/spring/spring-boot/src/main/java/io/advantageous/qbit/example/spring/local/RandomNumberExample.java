package io.advantageous.qbit.example.spring.local;

import io.advantageous.qbit.example.spring.common.RandomNumberService;
import io.advantageous.qbit.example.spring.common.RandomNumberServiceAsync;
import io.advantageous.qbit.example.spring.common.RandomNumberServiceImpl;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.spring.annotation.AutoFlush;
import io.advantageous.qbit.spring.annotation.EnableQBit;
import io.advantageous.qbit.spring.annotation.QBitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
        SpringApplication.run(RandomNumberExample.class);
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
     * This is the QBit service bean that we will inject into our other class.  We annotate it with
     * {@link QBitService @QbitService} so QBit knows to create a proxy for it.  QBit will create a proxy with the
     * interface specified by the asyncInterface annotation metadata.  One thing to note in this example is the use of
     * {@link AutoFlush @Autoflush}.  This is needed here because there is no request queue to provide any flushing
     * mechanism for the {@link io.advantageous.qbit.service.ServiceQueue ServiceQueue}.  Because of this, we need to
     * flush our queue automatically.  In a remote service with an endpoint, this is typically done with a
     * {@link io.advantageous.qbit.annotation.QueueCallback @QueueCallback} in the calling service to optimize flushing
     * when the queue is empty, idle or full.  AutoFlush is handy for a worker service (like a log parser) where there
     * are no remote calls.
     *
     * @return the service bean definition
     */
    @Bean
    @AutoFlush
    @QBitService(asyncInterface = RandomNumberServiceAsync.class)
    public RandomNumberService randomNumberService() {
        return new RandomNumberServiceImpl();
    }

}
