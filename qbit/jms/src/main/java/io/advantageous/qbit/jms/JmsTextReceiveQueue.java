package io.advantageous.qbit.jms;

import io.advantageous.qbit.queue.ReceiveQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapts a JMS Destination as a QBit `ReceiveQueue`.
 *
 * @see ReceiveQueue
 */
public class JmsTextReceiveQueue implements ReceiveQueue<String> {


    private final JmsService service;

    public JmsTextReceiveQueue(JmsService service) {
        this.service = service;
    }

    @Override
    public String pollWait() {

        return service.receiveTextMessage();
    }

    @Override
    public String poll() {
        return service.receiveTextMessageWithTimeout(0);
    }

    @Override
    public String take() {
        return service.receiveTextMessageWithTimeout(1_000);
    }

    @Override
    public Iterable<String> readBatch(int max) {

        String item = this.poll();
        if (item == null) {
            return Collections.emptyList();
        } else {
            List<String> batch = new ArrayList<>();
            batch.add(item);
            while ((item = this.poll()) != null) {
                batch.add(item);
            }
            return batch;
        }
    }

    @Override
    public Iterable<String> readBatch() {
        return readBatch(10);
    }


    @Override
    public void stop() {
        service.stop();
    }
}
