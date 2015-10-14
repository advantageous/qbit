package io.advantageous.qbit.jms;

import io.advantageous.qbit.queue.SendQueue;

/**
 * Adapts a JMS destination as a QBit `SendQueue`.
 *
 * @see SendQueue
 */
public class JmsTextSenderQueue implements SendQueue<String> {


    private final JmsService jmsService;

    public JmsTextSenderQueue(final JmsService jmsService) {
        this.jmsService = jmsService;
    }

    @Override
    public boolean send(final String item) {
        jmsService.sendTextMessage(item);
        return true;
    }


    @Override
    public void stop() {
        jmsService.stop();
    }
}
