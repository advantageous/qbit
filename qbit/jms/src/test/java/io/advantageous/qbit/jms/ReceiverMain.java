package io.advantageous.qbit.jms;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;

import static java.lang.System.out;

public class ReceiverMain  {

    public static void main(final String... args) {
        final JmsServiceBuilder jmsBuilder = JmsServiceBuilder.newJmsServiceBuilder()
                .setDefaultDestination("foobarQueue");

        final Queue<String> textQueue = new JmsTextQueue(jmsBuilder);
        final ReceiveQueue<String> receiveQueue = textQueue.receiveQueue();
        String message  = receiveQueue.pollWait();
        out.println(message);

        while (message!=null) {
            message = receiveQueue.poll();
            out.println(message);
        }

        out.println("DONE");

    }
}