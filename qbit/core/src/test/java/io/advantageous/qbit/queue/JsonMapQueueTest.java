package io.advantageous.qbit.queue;

import io.advantageous.boon.core.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonMapQueueTest {


    private Queue<Map<String, Person>> personQueue;
    private SendQueue<Map<String, Person>> personSendQueue;
    private ReceiveQueue<Map<String, Person>> personReceiveQueue;

    @Before
    public void setUp() throws Exception {


        personQueue = JsonQueue.createMapQueue(String.class, Person.class, QueueBuilder.queueBuilder()
                .setName("FOO").build());
        personSendQueue = personQueue.sendQueue();
        personReceiveQueue = personQueue.receiveQueue();


        personSendQueue.shouldBatch();
        personSendQueue.name();
        personSendQueue.size();
        personQueue.name();
        personQueue.size();
    }

    @Test
    public void testSendConsume() throws Exception {

        final Map<String, Person> noPerson = personReceiveQueue.poll();
        assertNull(noPerson);

        personSendQueue.send(Maps.map("Geoff", new Person("Geoff")));
        personSendQueue.send(Maps.map("Rick", new Person("Rick")));
        personSendQueue.flushSends();

        final Map<String, Person> geoff = personReceiveQueue.pollWait();
        final Map<String, Person> rick = personReceiveQueue.pollWait();

        assertEquals("Geoff", geoff.get("Geoff").name);
        assertEquals("Rick", rick.get("Rick").name);


        assertEquals(false, personQueue.started());

    }

    @After
    public void tearDown() throws Exception {

        personQueue.stop();
    }


    private static class Person {
        final String name;

        private Person(String name) {
            this.name = name;
        }
    }
}