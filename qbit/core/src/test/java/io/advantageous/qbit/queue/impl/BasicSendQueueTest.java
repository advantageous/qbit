package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.QueueException;
import io.advantageous.qbit.queue.SendQueue;
import org.junit.Test;

public class BasicSendQueueTest {

    @Test(expected = QueueException.class)
    public void testSendTooMany() throws Exception {

        final Queue<Object> queue = QueueBuilder.queueBuilder()
                .setEnqueueTimeout(1)
                .setName("unable to send")
                .setSize(10)
                .setBatchSize(10)
                .build();

        final SendQueue<Object> sendQueue = queue.sendQueue();


        for (int index = 0; index < 10_000; index++) {
            sendQueue.send("sendme");
        }

    }


    @Test //unbounded queue
    public void testSendTooManyWithLinkTransfer() throws Exception {

        final Queue<Object> queue = QueueBuilder.queueBuilder()
                .setEnqueueTimeout(1)
                .setName("unable to send")
                .setSize(10)
                .setBatchSize(10)
                .setLinkTransferQueue()
                .build();

        final SendQueue<Object> sendQueue = queue.sendQueue();


        for (int index = 0; index < 10_000; index++) {
            sendQueue.send("sendme");
        }

    }


    @Test //unbounded queue
    public void testWithCheckBusy() throws Exception {

        final Queue<Object> queue = QueueBuilder.queueBuilder()
                .setEnqueueTimeout(1)
                .setName("unable to send")
                .setSize(10)
                .setBatchSize(10)
                .setCheckIfBusy(true)
                .setCheckEvery(4)
                .setLinkTransferQueue()
                .build();

        final SendQueue<Object> sendQueue = queue.sendQueue();


        for (int index = 0; index < 100_000; index++) {
            sendQueue.send("sendme");
        }

    }


    @Test //unbounded queue
    public void testWithCheckBusyAndTryTransfer() throws Exception {

        final Queue<Object> queue = QueueBuilder.queueBuilder()
                .setEnqueueTimeout(1)
                .setName("unable to send")
                .setSize(10)
                .setBatchSize(10)
                .setCheckIfBusy(true)
                .setCheckEvery(4)
                .setTryTransfer(true)
                .setLinkTransferQueue()
                .build();

        final SendQueue<Object> sendQueue = queue.sendQueue();


        for (int index = 0; index < 100_000; index++) {
            sendQueue.send("sendme");
        }

    }
}