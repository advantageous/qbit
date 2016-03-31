package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.qbit.queue.QueueBuilder;
import org.junit.Before;

import java.util.concurrent.ArrayBlockingQueue;

public class NoBatchQueueTest extends BasicSendQueueWithTransferQueueTest {

    @Before
    public void setup() {

        queueBuilder = QueueBuilder.queueBuilder().setLinkTransferQueue()
                .setBatchSize(1);

        queue = queueBuilder.build();

        sendQueue = queue.sendQueue();
        abq = new ArrayBlockingQueue<>(100_000);


    }

}