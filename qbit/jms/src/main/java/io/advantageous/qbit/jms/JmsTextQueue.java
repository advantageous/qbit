package io.advantageous.qbit.jms;

import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.SendQueue;

import java.util.concurrent.TimeUnit;

/**
 * Adapts JMS as QBit `Queue`.
 *
 * @see Queue
 */
public class JmsTextQueue implements Queue<String> {

    private final JmsServiceBuilder builder;

    public JmsTextQueue(JmsServiceBuilder builder) {
        this.builder = builder;
    }

    @Override
    public ReceiveQueue<String> receiveQueue() {

        return new JmsTextReceiveQueue(builder.build());
    }

    @Override
    public SendQueue<String> sendQueue() {

        return new JmsTextSenderQueue(builder.build());

    }

    @Override
    public SendQueue<String> sendQueueWithAutoFlush(int interval, TimeUnit timeUnit) {
        return sendQueue();
    }

    @Override
    public SendQueue<String> sendQueueWithAutoFlush(PeriodicScheduler periodicScheduler, int interval, TimeUnit timeUnit) {
        return sendQueue();
    }

    @Override
    public void startListener(final ReceiveQueueListener<String> listener) {

        final JmsService jmsService = builder.build();

        jmsService.listenTextMessages(listener::receive);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean started() {

        return true;
    }

    @Override
    public String name() {
        return builder.getDefaultDestination();
    }

    @Override
    public void stop() {

    }
}
