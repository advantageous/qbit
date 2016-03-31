package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.QueueException;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ArrayBlockingBasicQueueTest extends BasicSendReceiveTest {


    @Before
    public void setup() {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder();

        queue = queueBuilder.setArrayBlockingQueue().setBatchSize(50)
                .setCheckEvery(5).setCheckIfBusy(true)
                .setEnqueueTimeoutTimeUnit(null).setEnqueueTimeout(0)
                .setName("Queue test").setPollTimeUnit(TimeUnit.MILLISECONDS)
                .setPollWait(50).build();

        receiveQueue = queue.receiveQueue();
        sendQueue = queue.sendQueue();
    }


    @Test(expected = QueueException.class)
    public void testTimeout() {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder();

        queue = queueBuilder.setArrayBlockingQueue().setBatchSize(5).setSize(5)
                .setEnqueueTimeout(1).setEnqueueTimeoutTimeUnit(TimeUnit.SECONDS)
                .setName("Queue test").setPollTimeUnit(TimeUnit.MILLISECONDS)
                .setPollWait(50).build();

        receiveQueue = queue.receiveQueue();
        sendQueue = queue.sendQueue();


        for (int index = 0; index < 2000; index++) {
            sendQueue.send("" + index);
        }
    }


}