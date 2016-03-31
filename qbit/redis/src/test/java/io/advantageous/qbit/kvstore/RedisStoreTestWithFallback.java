package io.advantageous.qbit.kvstore;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.kvstore.impl.StringDecoderEncoderKeyValueStore;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreServiceBuilder;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelWriteBehindReadFallbackKeyValueStore;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


@Ignore
public class RedisStoreTestWithFallback {


    private RedisKeyValueStore keyValueStore;

    private RedisKeyValueStoreBuilder builder;


    private TestTimer testTimer;


    private LowLevelLocalKeyValueStoreService localKeyValueStoreService;

    private LowLevelLocalKeyValueStoreServiceBuilder localKeyValueStoreServiceBuilder;

    private KeyValueStoreService<Todo> keyValueStoreService;
    private JsonKeyValueStoreServiceBuilder jsonKeyValueStoreServiceBuilder;

    private LowLevelWriteBehindReadFallbackKeyValueStore writeBehindReadFallbackKeyValueStore;


    @Before
    public void setup() {
        builder = RedisKeyValueStoreBuilder.redisKeyValueStoreBuilder();

        builder.setRedisClient(null);
        builder.setRedisOptions(null);
        builder.setVertx(null);
        builder.setVertxOptions(null);
        keyValueStore = builder.build();

        localKeyValueStoreServiceBuilder =
                LowLevelLocalKeyValueStoreServiceBuilder.localKeyValueStoreBuilder();

        testTimer = new TestTimer();
        testTimer.setTime();

        localKeyValueStoreServiceBuilder.useDefaultFlushCacheDuration();
        localKeyValueStoreServiceBuilder.setTimer(testTimer).build();
        localKeyValueStoreService = localKeyValueStoreServiceBuilder.setDebug(true).build();

        localKeyValueStoreService.process();

        jsonKeyValueStoreServiceBuilder = JsonKeyValueStoreServiceBuilder.jsonKeyValueStoreServiceBuilder();

        writeBehindReadFallbackKeyValueStore = new LowLevelWriteBehindReadFallbackKeyValueStore(localKeyValueStoreService,
                keyValueStore, ReactorBuilder.reactorBuilder().build());

        jsonKeyValueStoreServiceBuilder.setLowLevelKeyValueStoreService(writeBehindReadFallbackKeyValueStore);


        final StringDecoderEncoderKeyValueStore<Todo> todoStringDecoderEncoderKeyValueStore =
                jsonKeyValueStoreServiceBuilder.buildKeyValueStore(Todo.class);

        final ServiceQueue serviceQueue = ServiceBuilder.serviceBuilder()
                .setServiceObject(todoStringDecoderEncoderKeyValueStore).buildAndStartAll();

        keyValueStoreService = serviceQueue.createProxyWithAutoFlush(KeyValueStoreService.class, Duration.FIFTY_MILLIS);

        Sys.sleep(1000);

    }

    @Test
    public void testReadFallback() throws Exception {

        final CountDownLatch putWithConfirmation = new CountDownLatch(1);
        final CountDownLatch getReturn = new CountDownLatch(1);

        final String[] valueHolder = new String[1];

        keyValueStoreService.putWithConfirmation(new Callback<Boolean>() {
            @Override
            public void accept(Boolean flag) {

                System.out.println("CALLBACK CALLED");
                putWithConfirmation.countDown();
            }
        }, "fooKey", new Todo("barValue"));

        System.out.println("PUT CALLED");
        putWithConfirmation.await(5, TimeUnit.SECONDS);


        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
            getReturn.countDown();
        }, "fooKey");

        System.out.println("GET CALLED");

        getReturn.await(5, TimeUnit.SECONDS);

        assertEquals("barValue", valueHolder[0]);

    }

    private class Todo {
        final String name;

        private Todo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
