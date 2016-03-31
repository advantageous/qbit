package io.advantageous.qbit.queue;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonListQueueTest {


    private Queue<List<Person>> personQueue;
    private SendQueue<List<Person>> personSendQueue;
    private ReceiveQueue<List<Person>> personReceiveQueue;

    @Before
    public void setUp() throws Exception {


        personQueue = JsonQueue.createListQueue(Person.class, QueueBuilder.queueBuilder()
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

        final List<Person> noPerson = personReceiveQueue.poll();
        assertNull(noPerson);

        personSendQueue.send(Lists.list(new Person("Geoff")));
        personSendQueue.send(Lists.list(new Person("Rick")));
        personSendQueue.flushSends();

        final List<Person> geoff = personReceiveQueue.pollWait();
        final List<Person> rick = personReceiveQueue.pollWait();

        assertEquals("Geoff", geoff.get(0).name);
        assertEquals("Rick", rick.get(0).name);


        assertEquals(false, personQueue.started());

    }


    @Test
    public void testSendConsume2() throws Exception {


        personSendQueue.sendAndFlush(Lists.list(new Person("Geoff")));
        personSendQueue.sendAndFlush(Lists.list(new Person("Rick")));


        final List<Person> geoff = personReceiveQueue.pollWait();
        Sys.sleep(100);
        final List<Person> rick = personReceiveQueue.pollWait();

        assertEquals("Geoff", geoff.get(0).name);
        assertEquals("Rick", rick.get(0).name);


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