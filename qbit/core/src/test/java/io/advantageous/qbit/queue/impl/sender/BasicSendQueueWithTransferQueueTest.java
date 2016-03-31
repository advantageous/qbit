package io.advantageous.qbit.queue.impl.sender;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;

/**
 * Created by rick on 10/12/15.
 */
public class BasicSendQueueWithTransferQueueTest {

    protected QueueBuilder queueBuilder;
    protected Queue<String> queue;
    protected SendQueue<String> sendQueue;
    protected ArrayBlockingQueue<String> abq;

    @Before
    public void setup() {

        queueBuilder = QueueBuilder.queueBuilder().setLinkTransferQueue()
                .setCheckEvery(10)
                .setBatchSize(1_000);

        queue = queueBuilder.build();

        sendQueue = queue.sendQueue();
        abq = new ArrayBlockingQueue<>(100_000);


    }

    @Test
    public void test() {


        queue.startListener(abq::add);

        for (int index = 0; index < 100_000; index++) {
            sendQueue.send("" + index);
        }

        sendQueue.flushSends();

        Sys.sleep(100);


        assert (abq.size() > 1_000);

        if (abq.size() < 100_000) {

            Sys.sleep(200);

            assertEquals(100_000, abq.size());
        }
    }


    @Test
    public void test2() {


        queue.startListener(new ReceiveQueueListener<String>() {

            int count;

            @Override
            public void receive(String item) {
                count++;
                if (count % 10_000 == 0) {
                    Sys.sleep(10);
                }
                abq.add(item);
            }
        });

        for (int index = 0; index < 100_000; index++) {
            sendQueue.send("" + index);
        }

        sendQueue.flushSends();

        Sys.sleep(100);


        assert (abq.size() > 1_000);

        if (abq.size() < 100_000) {

            Sys.sleep(200);

            assertEquals(100_000, abq.size());
        }
    }


    @Test
    public void test3() {


        queue.startListener(new ReceiveQueueListener<String>() {

            int count;

            @Override
            public void receive(String item) {
                count++;
                if (count % 10_000 == 0) {
                    Sys.sleep(10);
                }
                abq.add(item);
            }
        });

        for (int index = 0; index < 100_000; index++) {
            sendQueue.send("" + index);
            if (index % 10 == 0) {
                if (!sendQueue.shouldBatch()) {
                    sendQueue.flushSends();
                }
            }
        }

        sendQueue.flushSends();

        Sys.sleep(100);


        assert (abq.size() > 1_000);

        if (abq.size() < 100_000) {

            Sys.sleep(200);

            assertEquals(100_000, abq.size());
        }
    }

    @After
    public void tearDown() {
        queue.stop();
    }
}