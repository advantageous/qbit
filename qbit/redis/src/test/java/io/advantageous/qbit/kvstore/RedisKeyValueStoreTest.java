package io.advantageous.qbit.kvstore;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreServiceBuilder;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelWriteBehindReadFallbackKeyValueStore;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.time.Duration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

@Ignore // we need integration versus non-integration tests
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

        try {
            builder.setRedisUri(new URI("redis://redisdb:foobared@localhost:6379/0"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        keyValueStore = builder.build();
    }


    @Test
    public void testPutWithConfirmation() throws Exception {

        final String value = "success-" + System.currentTimeMillis();

        final CountDownLatch putLatch = new CountDownLatch(1);
        final AtomicBoolean putCallbackResult = new AtomicBoolean();
        final AtomicBoolean putFailed = new AtomicBoolean();
        final AtomicBoolean putTimeout = new AtomicBoolean();

        final CallbackBuilder putCallbackBuilder = CallbackBuilder.newCallbackBuilder();


        /* Setup callback boolean. */
        putCallbackBuilder.withBooleanCallback(result -> {
            putCallbackResult.set(result);
            putLatch.countDown();
        });


        /* Setup callback error handler. */
        putCallbackBuilder.withErrorHandler(throwable -> {
            putFailed.set(true);
            putLatch.countDown();
        });


        /* Setup callback timeout handler. */
        putCallbackBuilder.withTimeoutHandler(() -> putTimeout.set(true));


        keyValueStore.putStringWithConfirmation(putCallbackBuilder.build(), "testPutWithConfirmation", value);

        putLatch.await(3, TimeUnit.SECONDS);


        assertFalse(putTimeout.get());
        assertFalse(putFailed.get());
        assertTrue(putCallbackResult.get());


        //Now test get


        final CountDownLatch getLatch = new CountDownLatch(1);
        final AtomicReference<String> getCallbackResult = new AtomicReference<>();
        final AtomicBoolean getFailed = new AtomicBoolean();
        final AtomicBoolean getTimeout = new AtomicBoolean();
        final CallbackBuilder getCallbackBuilder = CallbackBuilder.newCallbackBuilder();



        /* Setup callback boolean. */
        getCallbackBuilder.withOptionalStringCallback(result -> {

            if (result.isPresent()) {
                getCallbackResult.set(result.get());
            } else {

                getCallbackResult.set("NONE");
            }
            putLatch.countDown();
        });


        /* Setup callback error handler. */
        getCallbackBuilder.withErrorHandler(throwable -> {
            getFailed.set(true);
            putLatch.countDown();
        });


        /* Setup callback timeout handler. */
        getCallbackBuilder.withTimeoutHandler(() -> getTimeout.set(true));


        keyValueStore.getString(getCallbackBuilder.build(), "testPutWithConfirmation");

        getLatch.await(3, TimeUnit.SECONDS);


        assertFalse(getTimeout.get());
        assertFalse(getFailed.get());
        assertEquals(value, getCallbackResult.get());

    }

    @Test
    public void testPutWithConfirmationWrapped() throws Exception {

        final KeyValueStoreService<Todo> todoKVStoreInternal = JsonKeyValueStoreServiceBuilder
                .jsonKeyValueStoreServiceBuilder()
                .setLowLevelKeyValueStoreService(keyValueStore)
                .buildKeyValueStore(Todo.class);


        final KeyValueStoreService<Todo> todoKVStore = ServiceBuilder.serviceBuilder()
                .setServiceObject(todoKVStoreInternal)
                .buildAndStartAll()
                .createProxyWithAutoFlush(KeyValueStoreService.class, Duration.FIFTY_MILLIS);

        final String value = "success-" + System.currentTimeMillis();

        final CountDownLatch putLatch = new CountDownLatch(1);
        final AtomicBoolean putCallbackResult = new AtomicBoolean();
        final AtomicBoolean putFailed = new AtomicBoolean();
        final AtomicBoolean putTimeout = new AtomicBoolean();

        final CallbackBuilder putCallbackBuilder = CallbackBuilder.newCallbackBuilder();


        /* Setup callback boolean. */
        putCallbackBuilder.withBooleanCallback(result -> {
            putCallbackResult.set(result);
            putLatch.countDown();
        });


        /* Setup callback error handler. */
        putCallbackBuilder.withErrorHandler(throwable -> {
            putFailed.set(true);
            putLatch.countDown();
        });


        /* Setup callback timeout handler. */
        putCallbackBuilder.withTimeoutHandler(() -> putTimeout.set(true));


        todoKVStore.putWithConfirmation(putCallbackBuilder.build(),
                "testPutWithConfirmationWrapped", new Todo(value));

        ServiceProxyUtils.flushServiceProxy(todoKVStore);

        putLatch.await(3, TimeUnit.SECONDS);


        assertFalse(putTimeout.get());
        assertFalse(putFailed.get());
        assertTrue(putCallbackResult.get());


        //Now test get


        final CountDownLatch getLatch = new CountDownLatch(1);
        final AtomicReference<Todo> getCallbackResult = new AtomicReference<>();
        final AtomicBoolean getFailed = new AtomicBoolean();
        final AtomicBoolean getTimeout = new AtomicBoolean();
        final CallbackBuilder getCallbackBuilder = CallbackBuilder.newCallbackBuilder();



        /* Setup callback boolean. */
        getCallbackBuilder.withOptionalCallback(Todo.class, result -> {

            if (result.isPresent()) {
                getCallbackResult.set(result.get());
            } else {

                getCallbackResult.set(null);
            }
            putLatch.countDown();
        });


        /* Setup callback error handler. */
        getCallbackBuilder.withErrorHandler(throwable -> {
            getFailed.set(true);
            putLatch.countDown();
        });


        /* Setup callback timeout handler. */
        getCallbackBuilder.withTimeoutHandler(() -> getTimeout.set(true));


        todoKVStore.get(getCallbackBuilder.build(), "testPutWithConfirmationWrapped");
        ServiceProxyUtils.flushServiceProxy(todoKVStore);


        getLatch.await(3, TimeUnit.SECONDS);


        assertFalse(getTimeout.get());
        assertFalse(getFailed.get());
        assertNotNull(getCallbackResult.get());
        assertEquals(value, getCallbackResult.get().name);

    }

    @Test
    public void testPutWithConfirmationFallback() throws Exception {

        final LowLevelLocalKeyValueStoreServiceBuilder lowLevelLocalKeyValueStoreServiceBuilder =
                LowLevelLocalKeyValueStoreServiceBuilder.localKeyValueStoreBuilder();


        final LowLevelWriteBehindReadFallbackKeyValueStore writeBehindReadFallbackKeyValueStoreInternal =
                new LowLevelWriteBehindReadFallbackKeyValueStore(lowLevelLocalKeyValueStoreServiceBuilder.build(),
                        keyValueStore,
                        ReactorBuilder.reactorBuilder().build());

        final LowLevelKeyValueStoreService lowLevelKeyValueStoreService = ServiceBuilder.serviceBuilder()
                .setServiceObject(writeBehindReadFallbackKeyValueStoreInternal)
                .buildAndStartAll()
                .createProxyWithAutoFlush(LowLevelKeyValueStoreService.class, Duration.FIFTY_MILLIS);


        final KeyValueStoreService<Todo> todoKVStoreInternal = JsonKeyValueStoreServiceBuilder.jsonKeyValueStoreServiceBuilder()
                .setLowLevelKeyValueStoreService(lowLevelKeyValueStoreService).buildKeyValueStore(Todo.class);


        final KeyValueStoreService<Todo> todoKVStore = ServiceBuilder.serviceBuilder()
                .setServiceObject(todoKVStoreInternal)
                .buildAndStartAll()
                .createProxyWithAutoFlush(KeyValueStoreService.class, Duration.FIFTY_MILLIS);

        final String value = "success-" + System.currentTimeMillis();

        final CountDownLatch putLatch = new CountDownLatch(1);
        final AtomicBoolean putCallbackResult = new AtomicBoolean();
        final AtomicBoolean putFailed = new AtomicBoolean();
        final AtomicBoolean putTimeout = new AtomicBoolean();

        final CallbackBuilder putCallbackBuilder = CallbackBuilder.newCallbackBuilder();


        /* Setup callback boolean. */
        putCallbackBuilder.withBooleanCallback(result -> {
            putCallbackResult.set(result);
            putLatch.countDown();
        });


        /* Setup callback error handler. */
        putCallbackBuilder.withErrorHandler(throwable -> {
            putFailed.set(true);
            putLatch.countDown();
        });


        /* Setup callback timeout handler. */
        putCallbackBuilder.withTimeoutHandler(() -> putTimeout.set(true));


        todoKVStore.putWithConfirmation(putCallbackBuilder.build(), "testPutWithConfirmationWrapped", new Todo(value));
        ServiceProxyUtils.flushServiceProxy(todoKVStore);

        putLatch.await(3, TimeUnit.SECONDS);


        assertFalse(putTimeout.get());
        assertFalse(putFailed.get());
        assertTrue(putCallbackResult.get());


        //Now test get


        final CountDownLatch getLatch = new CountDownLatch(1);
        final AtomicReference<Todo> getCallbackResult = new AtomicReference<>();
        final AtomicBoolean getFailed = new AtomicBoolean();
        final AtomicBoolean getTimeout = new AtomicBoolean();
        final CallbackBuilder getCallbackBuilder = CallbackBuilder.newCallbackBuilder();



        /* Setup callback boolean. */
        getCallbackBuilder.withOptionalCallback(Todo.class, result -> {

            if (result.isPresent()) {
                getCallbackResult.set(result.get());
            } else {

                getCallbackResult.set(null);
            }
            getLatch.countDown();
        });


        /* Setup callback error handler. */
        getCallbackBuilder.withErrorHandler(throwable -> {
            getFailed.set(true);
            getLatch.countDown();
        });


        /* Setup callback timeout handler. */
        getCallbackBuilder.withTimeoutHandler(() -> getTimeout.set(true));


        todoKVStore.get(getCallbackBuilder.build(), "testPutWithConfirmationWrapped");
        ServiceProxyUtils.flushServiceProxy(todoKVStore);


        getLatch.await(3, TimeUnit.SECONDS);


        assertFalse(getTimeout.get());
        assertFalse(getFailed.get());
        assertNotNull(getCallbackResult.get());
        assertEquals(value, getCallbackResult.get().name);

    }

    @Test
    public void test() throws Exception {
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
    public void testCacheTimeOut() throws Exception {
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
    public void testCacheTimeOutWithConfirmation() throws Exception {
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
     *
     * @throws Exception
     */
    @Test
    public void testBytes() throws Exception {
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

    @Test
    public void loadTest() throws Exception {

    }

    private class Todo {
        final String name;

        private Todo(String name) {
            this.name = name;
        }
    }
}