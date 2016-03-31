package io.advantageous.qbit.kvstore.lowlevel;

import io.advantageous.qbit.kvstore.JsonKeyValueStoreServiceBuilder;
import io.advantageous.qbit.kvstore.KeyValueStoreService;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.time.Duration.TEN_SECONDS;
import static org.junit.Assert.*;

public class WriteBehindReadFallbackKeyValueStoreTest {

    private LowLevelLocalKeyValueStoreService localKeyValueStoreService;
    private LowLevelLocalKeyValueStoreService remoteKeyValueStoreService;

    private LowLevelLocalKeyValueStoreServiceBuilder localKeyValueStoreServiceBuilder;

    private KeyValueStoreService<Todo> keyValueStoreService;
    private JsonKeyValueStoreServiceBuilder jsonKeyValueStoreServiceBuilder;

    private LowLevelWriteBehindReadFallbackKeyValueStore writeBehindReadFallbackKeyValueStore;
    private TestTimer testTimer;

    @Before
    public void before() {
        localKeyValueStoreServiceBuilder =
                LowLevelLocalKeyValueStoreServiceBuilder.localKeyValueStoreBuilder();

        testTimer = new TestTimer();
        testTimer.setTime();

        localKeyValueStoreServiceBuilder.useDefaultFlushCacheDuration();
        localKeyValueStoreServiceBuilder.setTimer(testTimer).build();
        localKeyValueStoreService = localKeyValueStoreServiceBuilder.setDebug(true).build();
        remoteKeyValueStoreService = localKeyValueStoreServiceBuilder.build();

        localKeyValueStoreService.process();

        jsonKeyValueStoreServiceBuilder = JsonKeyValueStoreServiceBuilder.jsonKeyValueStoreServiceBuilder();

        writeBehindReadFallbackKeyValueStore = new LowLevelWriteBehindReadFallbackKeyValueStore(localKeyValueStoreService,
                remoteKeyValueStoreService, ReactorBuilder.reactorBuilder().build());

        jsonKeyValueStoreServiceBuilder.setLowLevelKeyValueStoreService(writeBehindReadFallbackKeyValueStore);


        keyValueStoreService = jsonKeyValueStoreServiceBuilder.buildKeyValueStore(Todo.class);


    }

    @Test
    public void testReadFallback() {
        final String[] valueHolder = new String[1];

        keyValueStoreService.put("fooKey", new Todo("barValue"));
        keyValueStoreService.process();
        keyValueStoreService.process();


        localKeyValueStoreService.delete("fooKey");
        localKeyValueStoreService.process();
        localKeyValueStoreService.process();

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "fooKey");


        keyValueStoreService.process();
        keyValueStoreService.process();
        assertEquals("barValue", valueHolder[0]);

    }

    @Test
    public void testReadPrimary() {
        final String[] valueHolder = new String[1];


        keyValueStoreService.put("fooKey", new Todo("barValue"));
        keyValueStoreService.process();
        keyValueStoreService.process();


        remoteKeyValueStoreService.delete("fooKey");
        remoteKeyValueStoreService.process();
        remoteKeyValueStoreService.process();

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "fooKey");


        keyValueStoreService.process();
        keyValueStoreService.process();
        assertEquals("barValue", valueHolder[0]);

    }

    @Test
    public void testWriteBehind() {
        final String[] valueHolder1 = new String[1];

        final String[] valueHolder2 = new String[1];


        keyValueStoreService.put("fooKey", new Todo("barValue"));
        keyValueStoreService.process();
        keyValueStoreService.process();

        remoteKeyValueStoreService.getString(s -> valueHolder1[0] = s.get(), "fooKey");
        localKeyValueStoreService.getString(s -> valueHolder2[0] = s.get(), "fooKey");

        keyValueStoreService.process();
        keyValueStoreService.process();

        assertNotNull(valueHolder1[0]);
        assertNotNull(valueHolder2[0]);

        assertArrayEquals(valueHolder1, valueHolder2);


    }

    @Test
    public void testTodo() {
        final boolean[] hasKeyRef = new boolean[1];
        final String[] valueHolder = new String[1];
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);
        keyValueStoreService.put("key", new Todo("value"));
        keyValueStoreService.process();

        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        assertEquals(true, hasKeyRef[0]);

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "key");

        keyValueStoreService.process();
        assertEquals("value", valueHolder[0]);

        keyValueStoreService.delete("key");
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        keyValueStoreService.process();

        assertEquals(false, hasKeyRef[0]);


        keyValueStoreService.putWithTimeout("key", new Todo("value"), TEN_SECONDS);
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        keyValueStoreService.process();
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        assertEquals(true, hasKeyRef[0]);

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "key");

        keyValueStoreService.process();

        assertEquals("value", valueHolder[0]);


        testTimer.seconds(6);
        keyValueStoreService.process();
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        keyValueStoreService.process();
        assertEquals(false, hasKeyRef[0]);


        valueHolder[0] = null;

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "key");

        keyValueStoreService.process();

        assertNull(valueHolder[0]);


        //


        keyValueStoreService.putWithTimeout("key", new Todo("value"), TEN_SECONDS);
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        localKeyValueStoreService.process();

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "key");

        keyValueStoreService.process();
        assertEquals("value", valueHolder[0]);


        testTimer.seconds(6);
        keyValueStoreService.process();


        valueHolder[0] = null;

        keyValueStoreService.get(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get().name;
            }
        }, "key");
        assertNull(valueHolder[0]);

        //

        keyValueStoreService.putWithConfirmation(
                aBoolean -> {
                }
                , "key", new Todo("value"));
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();
        assertEquals(true, hasKeyRef[0]);
        keyValueStoreService.delete("key");


        //

        keyValueStoreService.putWithConfirmationAndTimeout(
                aBoolean -> {
                }
                , "key", new Todo("value"), TEN_SECONDS);
        keyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        keyValueStoreService.process();


        assertEquals(true, hasKeyRef[0]);
        keyValueStoreService.delete("key");


        keyValueStoreService.deleteWithConfirmation(aBoolean -> {

        }, "key");


        jsonKeyValueStoreServiceBuilder.setJsonMapper(null);
        jsonKeyValueStoreServiceBuilder.setReactor(null);

        jsonKeyValueStoreServiceBuilder.buildKeyListOfValueStore(Todo.class);


    }

    @Test
    public void testBytes() {
        final boolean[] hasKeyRef = new boolean[1];
        final byte[][] valueHolder = new byte[1][];
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);
        writeBehindReadFallbackKeyValueStore.putBytes("key", "value".getBytes());
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(true, hasKeyRef[0]);

        writeBehindReadFallbackKeyValueStore.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");

    }

    @Test
    public void testBytesDelete() {

        final boolean[] hasKeyRef = new boolean[1];

        writeBehindReadFallbackKeyValueStore.putBytes("foo", "bar".getBytes());

        writeBehindReadFallbackKeyValueStore.deleteWithConfirmation(aBoolean -> {

        }, "foo");
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "foo");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(false, hasKeyRef[0]);


    }

    @Test
    public void testBytes2() {

        final boolean[] hasKeyRef = new boolean[1];
        final byte[][] valueHolder = new byte[1][];


        writeBehindReadFallbackKeyValueStore.putBytesWithTimeout("key1", "value1".getBytes(), TEN_SECONDS);
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key1");
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key1");
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(true, hasKeyRef[0]);


        writeBehindReadFallbackKeyValueStore.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key1");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertArrayEquals("value1".getBytes(), valueHolder[0]);


        testTimer.seconds(6);
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(false, hasKeyRef[0]);


        valueHolder[0] = null;

        writeBehindReadFallbackKeyValueStore.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertNull(valueHolder[0]);


        //


        writeBehindReadFallbackKeyValueStore.putBytesWithTimeout("key", "value".getBytes(), TEN_SECONDS);

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        writeBehindReadFallbackKeyValueStore.process();


        writeBehindReadFallbackKeyValueStore.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertArrayEquals("value".getBytes(), valueHolder[0]);


        testTimer.seconds(6);
        writeBehindReadFallbackKeyValueStore.process();


        valueHolder[0] = null;

        writeBehindReadFallbackKeyValueStore.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertNull(valueHolder[0]);


        //

        writeBehindReadFallbackKeyValueStore.putBytesWithConfirmation(
                aBoolean -> {
                }
                , "key", "value".getBytes());

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        assertEquals(true, hasKeyRef[0]);
        writeBehindReadFallbackKeyValueStore.delete("key");


        //

        writeBehindReadFallbackKeyValueStore.putBytesWithConfirmationAndTimeout(
                aBoolean -> {
                }
                , "key", "value".getBytes(), TEN_SECONDS);

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");

        writeBehindReadFallbackKeyValueStore.process();
        writeBehindReadFallbackKeyValueStore.process();

        assertEquals(true, hasKeyRef[0]);

        writeBehindReadFallbackKeyValueStore.delete("key");


        testTimer.minutes(10);


        writeBehindReadFallbackKeyValueStore.process();


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