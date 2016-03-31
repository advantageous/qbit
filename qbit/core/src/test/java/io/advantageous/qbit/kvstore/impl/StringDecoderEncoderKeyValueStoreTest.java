package io.advantageous.qbit.kvstore.impl;

import io.advantageous.qbit.kvstore.JsonKeyValueStoreServiceBuilder;
import io.advantageous.qbit.kvstore.KeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelLocalKeyValueStoreServiceBuilder;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.time.Duration.TEN_SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringDecoderEncoderKeyValueStoreTest {


    private LowLevelLocalKeyValueStoreService localKeyValueStoreService;
    private LowLevelLocalKeyValueStoreServiceBuilder localKeyValueStoreServiceBuilder;

    private KeyValueStoreService<Todo> keyValueStoreService;
    private JsonKeyValueStoreServiceBuilder jsonKeyValueStoreServiceBuilder;
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

        localKeyValueStoreService.process();

        jsonKeyValueStoreServiceBuilder = JsonKeyValueStoreServiceBuilder.jsonKeyValueStoreServiceBuilder();

        jsonKeyValueStoreServiceBuilder.setLowLevelKeyValueStoreService(localKeyValueStoreService);

        keyValueStoreService = jsonKeyValueStoreServiceBuilder.buildKeyValueStore(Todo.class);


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

    private class Todo {
        final String name;

        private Todo(String name) {
            this.name = name;
        }
    }


}