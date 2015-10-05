package io.advantageous.qbit.queue.impl;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.queue.*;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicSendReceiveTest {


    protected Queue<String> queue;
    protected SendQueue<String> sendQueue;
    protected ReceiveQueue<String> receiveQueue;


    @Before
    public void setup() {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder();

        queue = queueBuilder.setArrayBlockingQueue().setBatchSize(50)
                .setCheckEvery(5).setCheckIfBusy(false)
                .setName("Queue test").setPollTimeUnit(TimeUnit.MILLISECONDS)
                .setPollWait(50).build();

        receiveQueue = queue.receiveQueue();
        sendQueue = queue.sendQueue();
    }

    @Test
    public void basicTest() {
        sendQueue.send("hi");
        sendQueue.flushSends();
        final String item = receiveQueue.pollWait();
        assertEquals("hi", item);

        assertTrue(queue.hashCode() != 0);
        assertTrue(queue.name()!=null);
        assertTrue(queue.size()==0);
        assertTrue(sendQueue.shouldBatch());
        assertTrue(sendQueue.size()==0);
        assertTrue(sendQueue.name()!=null);
        assertTrue(sendQueue.hashCode()!=0);

    }


    @Test
    public void basicSendAndFlush() {
        sendQueue.sendAndFlush("hi");
        final String item = receiveQueue.pollWait();
        assertEquals("hi", item);
    }


    @Test
    public void basicSendTwoItemsWithArray() {
        sendQueue.sendMany("hi", "mom");
        sendQueue.flushSends();
        final String item1 = receiveQueue.pollWait();
        final String item2 = receiveQueue.poll();
        assertEquals("hi", item1);
        assertEquals("mom", item2);
    }


    @Test
    public void basicSendTwoItemsWithCollection() {

        sendQueue.sendBatch(Lists.list("hi", "mom"));
        sendQueue.flushSends();
        final String item1 = receiveQueue.pollWait();
        final String item2 = receiveQueue.poll();
        assertEquals("hi", item1);
        assertEquals("mom", item2);
    }


    @Test
    public void basicSendTwoItemsWithIterable() {

        sendQueue.sendBatch((Iterable<String>) Lists.list("hi", "mom"));
        sendQueue.flushSends();
        final String item1 = receiveQueue.pollWait();
        final String item2 = receiveQueue.poll();
        assertEquals("hi", item1);
        assertEquals("mom", item2);
    }



    @Test
    public void sendABunch() {

        final int amount = 10_000;
        int index = 0;
        for (;index< amount; index++) {
            sendQueue.send("" + index);
        }

        sendQueue.flushSends();

        String item = receiveQueue.pollWait();
        assertEquals("0", item);

        do  {
            index--;
            item = receiveQueue.pollWait();
        } while (item!=null);

        assertEquals(0, index);
    }



    @Test
    public void startQueue() throws InterruptedException {


        final int amount = 10_000;

        final AtomicInteger count = new AtomicInteger(amount);

        final CountDownLatch latch = new CountDownLatch(1);

        queue.startListener(item -> {
            count.decrementAndGet();
            if (count.get() == 0) {
                latch.countDown();
            }
        });



        for (int index = 0; index< amount; index++) {
            sendQueue.send("" + index);
        }

        sendQueue.flushSends();


        latch.await(5, TimeUnit.SECONDS);

        assertEquals(0, count.get());
    }
}
