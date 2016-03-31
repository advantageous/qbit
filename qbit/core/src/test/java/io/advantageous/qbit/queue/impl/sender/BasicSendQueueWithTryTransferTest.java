package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.qbit.queue.QueueBuilder;
import org.junit.Before;

import java.util.concurrent.ArrayBlockingQueue;


/**
 * Created by rick on 10/12/15.
 */
public class BasicSendQueueWithTryTransferTest extends BasicSendQueueWithTransferQueueTest {

    @Before
    public void setup() {

        queueBuilder = QueueBuilder.queueBuilder().setLinkTransferQueue()
                .setCheckEvery(10)
                .setBatchSize(1_000)
                .setCheckIfBusy(true)
                .setTryTransfer(true);

        queue = queueBuilder.build();

        sendQueue = queue.sendQueue();
        abq = new ArrayBlockingQueue<>(100_000);


    }

}