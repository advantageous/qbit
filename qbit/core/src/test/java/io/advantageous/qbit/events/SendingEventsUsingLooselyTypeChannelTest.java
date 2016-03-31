package io.advantageous.qbit.events;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.Listen;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.system.QBitSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;

public class SendingEventsUsingLooselyTypeChannelTest {

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

    @Before
    public void setup() {
        systemManager = new QBitSystemManager();

        eventManager = EventManagerBuilder
                .eventManagerBuilder().setName("Event Bus")
                .build();


        eventServiceQueue = ServiceBuilder.serviceBuilder().setServiceObject(eventManager).buildAndStartAll();


        serviceB = new ServiceB();

        serviceA = new ServiceA(eventServiceQueue.createProxy(EventManager.class));

        serviceBuilder = ServiceBuilder.serviceBuilder()
                .setServiceObject(serviceA)
                .setJoinEventManager(false)
                .setEventManager(eventManager).setSystemManager(systemManager);

        final ServiceQueue serviceAQueue = serviceBuilder.buildAndStartAll();


        ServiceBuilder.serviceBuilder()
                .setServiceObject(serviceB)
                .setJoinEventManager(false)
                .setEventManager(eventManager)
                .setSystemManager(systemManager)
                .buildAndStartAll();


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


        final EventManager eventManager;

        public ServiceA(EventManager eventManager) {
            this.eventManager = eventManager;
        }

        /**
         * Send record over record event channel.
         */
        public void addRecord(final Record record) {
            eventManager.send("NEW_RECORD", record);
        }


        /**
         * Send records over record event channel.
         */
        public void addRecords(List<Record> records) {
            eventManager.sendArguments("NEW_RECORD_LIST", records);
        }


        /**
         * flush events.
         */
        @QueueCallback({QueueCallbackType.LIMIT, QueueCallbackType.EMPTY})
        void process() {
            ServiceProxyUtils.flushServiceProxy(eventManager);
        }
    }

    /**
     * Service B listens to events over the event channel.
     */
    public static class ServiceB {

        final ArrayBlockingQueue<Record> blockingQueue = new ArrayBlockingQueue<>(10);


        @Listen("NEW_RECORD")
        public void newRecord(final Record record) {
            puts("GOT NEW RECORD", record);
            blockingQueue.add(record);
        }


        @Listen("NEW_RECORD_LIST")
        public void newRecords(List<Record> records) {
            puts("GOT NEW RECORDS", records);
            records.forEach(blockingQueue::add);
        }
    }

}
