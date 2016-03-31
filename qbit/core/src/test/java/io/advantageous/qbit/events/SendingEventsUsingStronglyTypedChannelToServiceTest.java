package io.advantageous.qbit.events;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.EventChannel;
import io.advantageous.qbit.annotation.Listen;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SendingEventsUsingStronglyTypedChannelToServiceTest {

    private QBitSystemManager systemManager;
    private EventManager eventManager;
    private ServiceBuilder serviceBuilder;
    private ServiceQueue eventServiceQueue;
    private ServiceB serviceB;
    private ServiceA serviceA;
    private ServiceAInterface serviceAQueueProxy;

    @Test
    public void test() {

        /* Add a record which triggers an event over the event channel. */
        serviceAQueueProxy.addRecord(new Record("Foo"));
        ServiceProxyUtils.flushServiceProxy(serviceAQueueProxy);

        /* Check if we have the record by asking Service B. */
        for (int index = 0; index < 10; index++) {
            Sys.sleep(100);
            if (serviceB.blockingQueue.size() > 0) {
                break;
            }
        }

        /** Make your assertions. */
        assertEquals(1, serviceB.blockingQueue.size());
        final Record record = serviceB.blockingQueue.poll();
        assertEquals("Foo", record.value);

    }

    @Test
    public void testList() {

        /* Send a list of records. */
        serviceAQueueProxy.addRecords(Lists.list(new Record("Foo"),
                new Record("Bar"), new Record("Baz")));
        ServiceProxyUtils.flushServiceProxy(serviceAQueueProxy);



        /* Check if we have the record by asking Service B. */
        for (int index = 0; index < 10; index++) {
            Sys.sleep(100);
            if (serviceB.blockingQueue.size() == 3) {
                break;
            }
        }
        assertEquals(3, serviceB.blockingQueue.size());
        Record record = serviceB.blockingQueue.poll();
        assertEquals("Foo", record.value);
        record = serviceB.blockingQueue.poll();
        assertEquals("Bar", record.value);
        record = serviceB.blockingQueue.poll();
        assertEquals("Baz", record.value);
    }

    @Test
    public void testSendSimple() throws Exception {
        final EventManager eventManager = eventServiceQueue.createProxy(EventManager.class);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Event<Object>> ref = new AtomicReference<>();

        eventManager.register("c1", event -> {
            latch.countDown();
            ref.set(event);
        });

        eventManager.send("c1", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManager);
        latch.await(1, TimeUnit.SECONDS);

        assertNotNull(ref.get());
    }

    @Before
    public void setup() {
        systemManager = new QBitSystemManager();

        eventManager = EventManagerBuilder
                .eventManagerBuilder().setName("Event Bus")
                .build();


        serviceBuilder = ServiceBuilder.serviceBuilder()
                .setSystemManager(systemManager);

        eventServiceQueue = serviceBuilder.setServiceObject(eventManager).build().startServiceQueue();


        serviceBuilder = ServiceBuilder.serviceBuilder()
                .setSystemManager(systemManager).setEventManager(eventManager);

        serviceB = new ServiceB();
        serviceBuilder.setServiceObject(serviceB).buildAndStartAll();


        serviceBuilder = ServiceBuilder.serviceBuilder()
                .setSystemManager(systemManager).setEventManager(eventManager);
        serviceA = new ServiceA(eventServiceQueue.createProxyWithAutoFlush(EventManager.class, Duration.SECOND),
                QBit.factory().eventBusProxyCreator());


        final ServiceQueue serviceAQueue = serviceBuilder
                .setServiceObject(serviceA).buildAndStartAll();


        serviceAQueueProxy = serviceAQueue.createProxy(ServiceAInterface.class);


    }

    @After
    public void cleanup() {
        systemManager.shutDown();
    }


    /**
     * Interface for Service A.
     */
    public interface ServiceAInterface {

        void addRecord(final Record record);

        void addRecords(final List<Record> records);
    }


    /**
     * Event channel.
     */
    @EventChannel
    public interface RecordListener {

        @EventChannel
        void newRecord(Record record);


        @EventChannel
        void newRecords(List<Record> records);
    }

    /* Object that will be sent over the event. */
    public static class Record {

        private final String value;

        public Record(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Record{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    /**
     * Service A will send events to services B via an event channel.
     */
    public static class ServiceA implements ServiceAInterface {

        private final RecordListener recordChannel;

        public ServiceA(EventManager eventManager, EventBusProxyCreator eventBusProxyCreator) {
            recordChannel = eventBusProxyCreator.createProxy(eventManager, RecordListener.class);
        }

        /**
         * Send record over record event channel.
         */
        public void addRecord(final Record record) {
            recordChannel.newRecord(record);
            ServiceProxyUtils.flushServiceProxy(recordChannel);
        }


        /**
         * Send records over record event channel.
         */
        public void addRecords(List<Record> records) {

            recordChannel.newRecords(records);
        }


        /**
         * flush events.
         */
        @QueueCallback({QueueCallbackType.LIMIT, QueueCallbackType.EMPTY})
        void process() {
            ServiceProxyUtils.flushServiceProxy(recordChannel);
        }
    }

    /**
     * Service B listens to events over the event channel.
     */
    public static class ServiceB implements RecordListener {

        final ArrayBlockingQueue<Record> blockingQueue = new ArrayBlockingQueue<>(10);

        @Listen
        @Override
        public void newRecord(final Record record) {
            puts("GOT NEW RECORD", record);
            blockingQueue.add(record);
        }


        @Listen
        @Override
        public void newRecords(List<Record> records) {
            puts("GOT NEW RECORDS", records);
            records.forEach(blockingQueue::add);
        }
    }

}
