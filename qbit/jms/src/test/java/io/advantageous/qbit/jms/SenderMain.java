package io.advantageous.qbit.jms;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;

public class SenderMain {
    public static void main(final String... args) {

        final JmsServiceBuilder jmsBuilder = JmsServiceBuilder.newJmsServiceBuilder()
                .setDefaultDestination("foobarQueue");
        final Queue<String> textQueue = new JmsTextQueue(jmsBuilder);
        final SendQueue<String> sendQueue = textQueue.sendQueue();

        sendQueue.send("foo");
        for (int i = 0; i < 10; i++) {
            sendQueue.send("foo" + i);
        }
    }
}
