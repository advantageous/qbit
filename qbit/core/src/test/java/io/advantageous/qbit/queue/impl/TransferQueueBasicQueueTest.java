package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.QueueBuilder;
import org.junit.Before;

import java.util.concurrent.TimeUnit;

public class TransferQueueBasicQueueTest extends BasicSendReceiveTest {


    @Before
    public void setup() {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder();

        queue = queueBuilder.setLinkTransferQueue().setBatchSize(50)
                .setCheckEvery(5).setCheckIfBusy(true)
                .setName("Queue test").setPollTimeUnit(TimeUnit.MILLISECONDS)
                .setPollWait(50).build();

        receiveQueue = queue.receiveQueue();
        sendQueue = queue.sendQueue();
    }
}
