package io.advantageous.qbit.kvstore;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

@Ignore
public class RedisKeyValueStoreTest {

    private RedisKeyValueStore keyValueStore;

    private RedisKeyValueStoreBuilder builder;



    @Before
    public void setup() {
        builder = RedisKeyValueStoreBuilder.redisKeyValueStoreBuilder();
        keyValueStore = builder.build();
    }

    @Test
    public void test() throws Exception{

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final AtomicReference<String> ref = new AtomicReference<>();

        keyValueStore.putString("foo", "bar");

        keyValueStore.getString(optional -> {
            ref.set(optional.get());
            countDownLatch.countDown();
        }, "foo");

        countDownLatch.await(10, TimeUnit.SECONDS);
        assertEquals("bar", ref.get());

    }

}