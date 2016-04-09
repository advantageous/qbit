package io.advantageous.qbit.kvstore;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class LocalKeyValueStoreServiceTest {

    KeyValueStoreService<Todo> kvStore;
    LowLevelKeyValueStoreService lowLevelKVStore;
    TestTimer timer;

    @Before
    public void setup() {

        timer = new TestTimer();

        lowLevelKVStore = LowLevelLocalKeyValueStoreServiceBuilder.localKeyValueStoreBuilder()
                .setTimer(timer)
                .buildAsServiceAndStartAll()
                .createProxy(LowLevelKeyValueStoreService.class);

        kvStore = LocalKeyValueStoreServiceBuilder.localKeyValueStoreServiceBuilder(Todo.class)
                .setTimer(timer)
                .setWriteBehindAndReadFallbackAsLowLevel(lowLevelKVStore)
                .buildAsServiceAndStartAll().createProxy(KeyValueStoreService.class);

        Sys.sleep(100);

    }

    public void setupWithDebug() {
        lowLevelKVStore = LowLevelLocalKeyValueStoreServiceBuilder.localKeyValueStoreBuilder()
                .setTimer(timer)
                .buildAsServiceAndStartAll()
                .createProxy(LowLevelKeyValueStoreService.class);

        kvStore = LocalKeyValueStoreServiceBuilder.localKeyValueStoreServiceBuilder(Todo.class)
                .setTimer(timer).setDebug(true).setDebugInterval(Duration.ONE_HOUR)
                .setWriteBehindAndReadFallbackAsLowLevel(lowLevelKVStore)
                .buildAsServiceAndStartAll().createProxy(KeyValueStoreService.class);

        timer = new TestTimer();

        Sys.sleep(100);

    }

    @Test
    public void testAllDebug() throws InterruptedException {
        setupWithDebug();
        test();
        setupWithDebug();
        this.putWithConfirmation();
        setupWithDebug();
        this.testDelete();
        setupWithDebug();
        this.testDeleteWithConfirmation();
    }

    @Test
    public void test() throws InterruptedException {

        kvStore.put("testKey", new Todo("testValue"));
        ServiceProxyUtils.flushServiceProxy(kvStore);
        Sys.sleep(100);


        final Optional<Todo> todoOptional = getTodoForKey("testKey");

        assertTrue(todoOptional.isPresent());
        assertEquals("testValue", todoOptional.get().name);


        final Optional<String> todoForKeyFromBacking = getTodoForKeyFromBacking("testKey");

        assertTrue(todoForKeyFromBacking.isPresent());
        assertEquals("{\"name\":\"testValue\"}", todoForKeyFromBacking.get());

    }

    public Optional<Todo> getTodoForKey(String key) throws InterruptedException {
        final CountDownLatch getLatch = new CountDownLatch(1);
        final AtomicReference<Optional<Todo>> reference = new AtomicReference<>();


        kvStore.get(todo -> {
            reference.set(todo);
            getLatch.countDown();
        }, key);
        Sys.sleep(100);
        ServiceProxyUtils.flushServiceProxy(kvStore);

        getLatch.await(2, TimeUnit.SECONDS);

        assertNotNull(reference.get());

        return reference.get();
    }

    public boolean hasKey(String key) throws InterruptedException {
        final CountDownLatch getLatch = new CountDownLatch(1);
        final AtomicBoolean reference = new AtomicBoolean();


        kvStore.hasKey(present -> {
            reference.set(present);
            getLatch.countDown();
        }, key);
        Sys.sleep(100);
        ServiceProxyUtils.flushServiceProxy(kvStore);

        getLatch.await(2, TimeUnit.SECONDS);


        return reference.get();
    }

    public Optional<String> getTodoForKeyFromBacking(String key) throws InterruptedException {
        final CountDownLatch getLatch = new CountDownLatch(1);
        final AtomicReference<Optional<String>> reference = new AtomicReference<>();


        lowLevelKVStore.getString(todo -> {
            reference.set(todo);
            getLatch.countDown();
        }, key);
        Sys.sleep(100);
        ServiceProxyUtils.flushServiceProxy(lowLevelKVStore);

        getLatch.await(2, TimeUnit.SECONDS);

        assertNotNull(reference.get());

        return reference.get();
    }

    @Test
    public void testTimeout() throws InterruptedException {

        timer.setTime();

        kvStore.putWithTimeout("testKey2", new Todo("testValue2"), Duration.FIVE_SECONDS);
        ServiceProxyUtils.flushServiceProxy(kvStore);
        Sys.sleep(100);


        final Optional<Todo> todoOptional = getTodoForKey("testKey2");

        assertTrue(todoOptional.isPresent());
        assertEquals("testValue2", todoOptional.get().name);


        final Optional<String> todoForKeyFromBacking = getTodoForKeyFromBacking("testKey2");
        assertTrue(todoForKeyFromBacking.isPresent());
        assertEquals("{\"name\":\"testValue2\"}", todoForKeyFromBacking.get());

        assertTrue(hasKey("testKey2"));

        timer.seconds(6);
        Sys.sleep(1000);

        final Optional<Todo> todoOptional2 = getTodoForKey("testKey2");
        assertFalse(todoOptional2.isPresent());


        final Optional<String> todoForKeyFromBacking2 = getTodoForKeyFromBacking("testKey2");
        assertFalse(todoForKeyFromBacking2.isPresent());


        assertFalse(hasKey("testKey2"));


        Sys.sleep(100);


        final Optional<Todo> todoOptional3 = getTodoForKey("testKey2");
        assertFalse(todoOptional3.isPresent());


        final Optional<String> todoForKeyFromBacking3 = getTodoForKeyFromBacking("testKey2");
        assertFalse(todoForKeyFromBacking3.isPresent());


    }

    @Test
    public void testDelete() throws InterruptedException {


        kvStore.put("testKey3", new Todo("testValue3"));
        ServiceProxyUtils.flushServiceProxy(kvStore);
        Sys.sleep(100);


        final Optional<Todo> todoOptional = getTodoForKey("testKey3");

        assertTrue(todoOptional.isPresent());
        assertEquals("testValue3", todoOptional.get().name);


        final Optional<String> todoForKeyFromBacking = getTodoForKeyFromBacking("testKey3");
        assertTrue(todoForKeyFromBacking.isPresent());
        assertEquals("{\"name\":\"testValue3\"}", todoForKeyFromBacking.get());


        kvStore.delete("testKey3");
        ServiceProxyUtils.flushServiceProxy(kvStore);
        Sys.sleep(100);

        final Optional<Todo> todoOptional2 = getTodoForKey("testKey3");
        assertFalse(todoOptional2.isPresent());


        final Optional<String> todoForKeyFromBacking2 = getTodoForKeyFromBacking("testKey3");
        assertFalse(todoForKeyFromBacking2.isPresent());


    }

    @Test
    public void testDeleteWithConfirmation() throws InterruptedException {


        kvStore.put("testKey3", new Todo("testValue3"));
        ServiceProxyUtils.flushServiceProxy(kvStore);
        Sys.sleep(100);


        final Optional<Todo> todoOptional = getTodoForKey("testKey3");

        assertTrue(todoOptional.isPresent());
        assertEquals("testValue3", todoOptional.get().name);


        final Optional<String> todoForKeyFromBacking = getTodoForKeyFromBacking("testKey3");
        assertTrue(todoForKeyFromBacking.isPresent());
        assertEquals("{\"name\":\"testValue3\"}", todoForKeyFromBacking.get());


        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean deleteConfirmation = new AtomicBoolean();


        kvStore.deleteWithConfirmation(confirmed -> {
            deleteConfirmation.set(confirmed);
            latch.countDown();
        }, "testKey3");
        ServiceProxyUtils.flushServiceProxy(kvStore);

        latch.await(1, TimeUnit.SECONDS);

        assertTrue(deleteConfirmation.get());

        final Optional<Todo> todoOptional2 = getTodoForKey("testKey3");
        assertFalse(todoOptional2.isPresent());


        final Optional<String> todoForKeyFromBacking2 = getTodoForKeyFromBacking("testKey3");
        assertFalse(todoForKeyFromBacking2.isPresent());


    }

    @Test
    public void putWithConfirmation() throws InterruptedException {


        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean putConfirmation = new AtomicBoolean();


        kvStore.putWithConfirmation(confirmed -> {
            putConfirmation.set(confirmed);
            latch.countDown();

        }, "testKey", new Todo("testValue"));

        ServiceProxyUtils.flushServiceProxy(kvStore);

        latch.await(1, TimeUnit.SECONDS);


        final Optional<Todo> todoOptional = getTodoForKey("testKey");

        assertTrue(todoOptional.isPresent());
        assertEquals("testValue", todoOptional.get().name);


        final Optional<String> todoForKeyFromBacking = getTodoForKeyFromBacking("testKey");

        assertTrue(todoForKeyFromBacking.isPresent());
        assertEquals("{\"name\":\"testValue\"}", todoForKeyFromBacking.get());

    }

    @Test
    public void testTimeoutWithPutConfirmation() throws InterruptedException {

        timer.setTime();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean putConfirmation = new AtomicBoolean();

        kvStore.putWithConfirmationAndTimeout(confirmed -> {
            putConfirmation.set(confirmed);
            latch.countDown();
        }, "testKey2", new Todo("testValue2"), Duration.FIVE_SECONDS);
        ServiceProxyUtils.flushServiceProxy(kvStore);

        latch.await(1, TimeUnit.SECONDS);


        final Optional<Todo> todoOptional = getTodoForKey("testKey2");

        assertTrue(todoOptional.isPresent());
        assertEquals("testValue2", todoOptional.get().name);


        final Optional<String> todoForKeyFromBacking = getTodoForKeyFromBacking("testKey2");
        assertTrue(todoForKeyFromBacking.isPresent());
        assertEquals("{\"name\":\"testValue2\"}", todoForKeyFromBacking.get());

        timer.seconds(6);


        final Optional<Todo> todoOptional2 = getTodoForKey("testKey2");
        assertFalse(todoOptional2.isPresent());


        final Optional<String> todoForKeyFromBacking2 = getTodoForKeyFromBacking("testKey2");
        assertFalse(todoForKeyFromBacking2.isPresent());


    }

    public static class Todo {
        final String name;

        public Todo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


}