package io.advantageous.qbit.queue;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonQueueTest {


    private Queue<Person> personQueue;
    private SendQueue<Person> personSendQueue;
    private ReceiveQueue<Person> personReceiveQueue;

    @Before
    public void setUp() throws Exception {

        personQueue = new JsonQueue<>(Person.class, QueueBuilder.queueBuilder()
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

        final Person noPerson = personReceiveQueue.poll();
        assertNull(noPerson);

        personSendQueue.send(new Person("Geoff"));
        personSendQueue.send(new Person("Rick"));
        personSendQueue.flushSends();

        final Person geoff = personReceiveQueue.pollWait();
        final Person rick = personReceiveQueue.pollWait();

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);


        assertEquals(false, personQueue.started());

    }


    @Test
    public void testSendConsume2() throws Exception {

        personSendQueue.sendAndFlush(new Person("Geoff"));
        personSendQueue.sendAndFlush(new Person("Rick"));

        final Person geoff = personReceiveQueue.pollWait();
        Sys.sleep(100);
        final Person rick = personReceiveQueue.poll();

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

    }


    @Test
    public void testSendConsume3() throws Exception {

        personSendQueue = personQueue.sendQueueWithAutoFlush(10, TimeUnit.MILLISECONDS);

        personSendQueue.sendMany(new Person("Geoff"), new Person("Rick"));
        final Person geoff = personReceiveQueue.take();
        final Person rick = personReceiveQueue.take();

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

    }


    @Test
    public void testSendConsume4() throws Exception {
        personSendQueue = personQueue.sendQueueWithAutoFlush(QBit.factory().periodicScheduler(),
                10, TimeUnit.MILLISECONDS);

        personSendQueue.sendBatch(Lists.list(new Person("Geoff"), new Person("Rick")));
        final Person geoff = personReceiveQueue.take();
        final Person rick = personReceiveQueue.take();

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

    }

    @Test
    public void testSendConsume5() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = () -> list.iterator();


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();

        final Person geoff = personReceiveQueue.pollWait();
        final Person rick = personReceiveQueue.pollWait();

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

    }

    @Test
    public void testSendConsume6() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = () -> list.iterator();


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();

        Sys.sleep(100);
        final List<Person> personsBatch = (List<Person>) personReceiveQueue.readBatch();

        final Person geoff = personsBatch.get(0);
        final Person rick = personsBatch.get(1);

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

    }


    @Test
    public void testSendConsume7() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = () -> list.iterator();


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();

        Sys.sleep(100);
        final List<Person> personsBatch = (List<Person>) personReceiveQueue.readBatch(5);

        final Person geoff = personsBatch.get(0);
        final Person rick = personsBatch.get(1);

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

    }


    @Test
    public void testSendConsume8() throws Exception {
        final List<Person> list = Lists.list(new Person("Geoff"), new Person("Rick"));

        Iterable<Person> persons = () -> list.iterator();


        personSendQueue.sendBatch(persons);
        personSendQueue.flushSends();

        ArrayBlockingQueue<Person> personsABQ = new ArrayBlockingQueue<>(100);

        personQueue.startListener(item -> personsABQ.add(item));

        while (personsABQ.size() != 2) {
            Sys.sleep(1);
        }

        final Person geoff = personsABQ.poll();
        final Person rick = personsABQ.poll();

        assertEquals("Geoff", geoff.name);
        assertEquals("Rick", rick.name);

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