package io.advantageous.qbit.jms;

import io.advantageous.qbit.queue.ReceiveQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JmsTextRecieveQueue implements ReceiveQueue<String> {


    private final JmsService service;

    public JmsTextRecieveQueue(JmsService service) {
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
    public Iterable<String> readBatch(final int max) {

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

        String item = this.poll();
        if (item == null) {
            return Collections.emptyList();
        } else {
            List<String> batch = new ArrayList<>();
            batch.add(item);
            while ((item = this.poll()) != null) {
                batch.add(item);
                if (batch.size() > 10) {
                    break;
                }
            }
            return batch;
        }
    }
}
