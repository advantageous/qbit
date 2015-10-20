package io.advantageous.qbit.kvstore;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.time.Duration;
import org.junit.Before;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Ignore
public class RedisKeyValueStoreTest {

    private RedisKeyValueStore keyValueStore;

    private RedisKeyValueStoreBuilder builder;



    @Before
    public void setup() {
        builder = RedisKeyValueStoreBuilder.redisKeyValueStoreBuilder();

        builder.setRedisClient(null);
        builder.setRedisOptions(null);
        builder.setVertx(null);
        builder.setVertxOptions(null);
        keyValueStore = builder.build();
    }

    @Test
    public void test() throws Exception{
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final CountDownLatch countDownLatch2 = new CountDownLatch(1);
        final AtomicReference<String> ref = new AtomicReference<>();
        final AtomicBoolean hasKeyRef = new AtomicBoolean();

        keyValueStore.putString("foo", "bar");
        keyValueStore.getString(optional -> {
            ref.set(optional.get());
            countDownLatch.countDown();
        }, "foo");
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertEquals("bar", ref.get());

        keyValueStore.delete("foo");

        keyValueStore.hasKey(hasKey -> {
            hasKeyRef.set(hasKey);
            countDownLatch2.countDown();
        }, "foo");


        countDownLatch2.await(10, TimeUnit.SECONDS);

        assertFalse(hasKeyRef.get());


        keyValueStore.putStringWithConfirmation(aBoolean -> {

        }, "foo", "bar");

        keyValueStore.deleteWithConfirmation(deleted -> {

        }, "foo");


        keyValueStore.hasKey(hasKey -> {
            hasKeyRef.set(hasKey);
        }, "foo");

        Sys.sleep(100);


        assertFalse(hasKeyRef.get());
    }



    @Test
    public void testCacheTimeOut() throws Exception{
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final CountDownLatch countDownLatch2 = new CountDownLatch(1);
        final AtomicReference<String> ref = new AtomicReference<>();
        keyValueStore.putStringWithTimeout("foo2", "bar2", Duration.HUNDRED_MILLIS.units(2));


        keyValueStore.getString(optional -> {

            if (optional.isPresent()) {

                ref.set(optional.get());
            } else {
                ref.set("NONE");
            }
            countDownLatch.countDown();
        }, "foo2");
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertEquals("bar2", ref.get());

        Sys.sleep(1_000);
        keyValueStore.getString(optional -> {

            if (optional.isPresent()) {

                ref.set(optional.get());
            } else {
                ref.set("NONE");
            }
            countDownLatch2.countDown();
        }, "foo2");
        countDownLatch2.await(10, TimeUnit.SECONDS);
        assertEquals("NONE", ref.get());
    }


    @Test
    public void testCacheTimeOutWithConfirmation() throws Exception{
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicReference<String> ref = new AtomicReference<>();
        final AtomicBoolean ok = new AtomicBoolean();
        keyValueStore.putStringWithConfirmationAndTimeout(success -> ok.set(success),
                "foo2", "bar2", Duration.HUNDRED_MILLIS.units(2));
        Sys.sleep(1_000);
        keyValueStore.getString(optional -> {

            if (optional.isPresent()) {

                ref.set(optional.get());
            } else {
                ref.set("NONE");
            }
            countDownLatch.countDown();
        }, "foo2");
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertEquals("NONE", ref.get());
    }


    /**
     * Bytes are not really supported by the redis client yet.
     * @throws Exception
     */
    @Test
    public void testBytes() throws Exception{
        keyValueStore.putBytes("key1", "value1".getBytes());

        keyValueStore.putBytesWithConfirmation(aBoolean -> {
        }, "key2", "value2".getBytes());


        keyValueStore.putBytesWithConfirmationAndTimeout(aBoolean -> {
        }, "key3", "value3".getBytes(), Duration.SECONDS.units(2));

        keyValueStore.putBytesWithTimeout("key4", "value4".getBytes(), Duration.FIVE_SECONDS);

        keyValueStore.getBytes(bytes -> {

        }, "key1");

        keyValueStore.delete("key1");
        keyValueStore.delete("key2");
        keyValueStore.delete("key3");
        keyValueStore.delete("key4");
    }
}