package io.advantageous.qbit.kvstore.lowlevel;

import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.time.Duration.TEN_SECONDS;
import static org.junit.Assert.*;

/**
 * Testing an async interface but it is not  yet async.
 */
public class LowLevelKeyValueStoreServiceTest {

    private LowLevelLocalKeyValueStoreService localKeyValueStoreService;
    private LowLevelLocalKeyValueStoreServiceBuilder localKeyValueStoreServiceBuilder;

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
    }


    @Test
    public void testString() {
        final boolean[] hasKeyRef = new boolean[1];
        final String[] valueHolder = new String[1];
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);
        localKeyValueStoreService.putString("key", "value");
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);

        localKeyValueStoreService.getString(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertEquals("value", valueHolder[0]);

        localKeyValueStoreService.delete("key");
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);


        localKeyValueStoreService.putStringWithTimeout("key", "value", TEN_SECONDS);
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        localKeyValueStoreService.process();
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);

        localKeyValueStoreService.getString(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertEquals("value", valueHolder[0]);


        testTimer.seconds(6);
        localKeyValueStoreService.process();
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);


        valueHolder[0] = null;

        localKeyValueStoreService.getString(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertNull(valueHolder[0]);


        //


        localKeyValueStoreService.putStringWithTimeout("key", "value", TEN_SECONDS);
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        localKeyValueStoreService.process();

        localKeyValueStoreService.getString(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertEquals("value", valueHolder[0]);


        testTimer.seconds(6);
        localKeyValueStoreService.process();


        valueHolder[0] = null;

        localKeyValueStoreService.getString(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertNull(valueHolder[0]);

        //

        localKeyValueStoreService.putStringWithConfirmation(
                aBoolean -> {
                }
                , "key", "value");
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);
        localKeyValueStoreService.delete("key");


        //

        localKeyValueStoreService.putStringWithConfirmationAndTimeout(
                aBoolean -> {
                }
                , "key", "value", TEN_SECONDS);
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");

        assertEquals(true, hasKeyRef[0]);
        localKeyValueStoreService.delete("key");


    }


    @Test
    public void testBytes() {
        final boolean[] hasKeyRef = new boolean[1];
        final byte[][] valueHolder = new byte[1][];
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);
        localKeyValueStoreService.putBytes("key", "value".getBytes());
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);

        localKeyValueStoreService.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertArrayEquals("value".getBytes(), valueHolder[0]);


        localKeyValueStoreService.deleteWithConfirmation(aBoolean -> {

        }, "key");
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);


        localKeyValueStoreService.putBytesWithTimeout("key", "value".getBytes(), TEN_SECONDS);
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        localKeyValueStoreService.process();
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);


        localKeyValueStoreService.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertArrayEquals("value".getBytes(), valueHolder[0]);


        testTimer.seconds(6);
        localKeyValueStoreService.process();
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(false, hasKeyRef[0]);


        valueHolder[0] = null;

        localKeyValueStoreService.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertNull(valueHolder[0]);


        //


        localKeyValueStoreService.putBytesWithTimeout("key", "value".getBytes(), TEN_SECONDS);
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);


        testTimer.seconds(5);
        localKeyValueStoreService.process();


        localKeyValueStoreService.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertArrayEquals("value".getBytes(), valueHolder[0]);


        testTimer.seconds(6);
        localKeyValueStoreService.process();


        valueHolder[0] = null;

        localKeyValueStoreService.getBytes(returnValue -> {

            if (returnValue.isPresent()) {
                valueHolder[0] = returnValue.get();
            }
        }, "key");
        assertNull(valueHolder[0]);


        //

        localKeyValueStoreService.putBytesWithConfirmation(
                aBoolean -> {
                }
                , "key", "value".getBytes());
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");
        assertEquals(true, hasKeyRef[0]);
        localKeyValueStoreService.delete("key");


        //

        localKeyValueStoreService.putBytesWithConfirmationAndTimeout(
                aBoolean -> {
                }
                , "key", "value".getBytes(), TEN_SECONDS);
        localKeyValueStoreService.hasKey(hasKey -> hasKeyRef[0] = hasKey, "key");

        assertEquals(true, hasKeyRef[0]);

        localKeyValueStoreService.delete("key");


        testTimer.minutes(10);


        localKeyValueStoreService.process();


        localKeyValueStoreServiceBuilder.setFlushCacheDuration(null);
        localKeyValueStoreServiceBuilder.build();

        localKeyValueStoreServiceBuilder.setLocalCacheSize(10);
        localKeyValueStoreServiceBuilder.setReactor(ReactorBuilder.reactorBuilder().build());


        localKeyValueStoreServiceBuilder.setTimer(null);

        localKeyValueStoreServiceBuilder.setStatsCollector(null);
        localKeyValueStoreServiceBuilder.build();

        assertNotNull(localKeyValueStoreServiceBuilder.getTimer());
    }
}