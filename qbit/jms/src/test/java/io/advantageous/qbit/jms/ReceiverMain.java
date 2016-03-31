package io.advantageous.qbit.jms;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;

import static java.lang.System.out;

public class ReceiverMain {

    public static void main(final String... args) {

        /** Create a new JMS Builder which can emit JmsService objects. */
        final JmsServiceBuilder jmsBuilder = JmsServiceBuilder
                .newJmsServiceBuilder()
                .setDefaultDestination("foobarQueue");

        /** Create a QBit Queue that talks to JMS. */
        final Queue<String> textQueue = new JmsTextQueue(jmsBuilder);


        /** Create a QBit ReceiveQueue that talks to JMS. */
        final ReceiveQueue<String> receiveQueue = textQueue.receiveQueue();


        /** Get a message from JMS. */
        String message = receiveQueue.pollWait();
        out.println(message);


        /** Keep getting messages. */
        while (message != null) {
            message = receiveQueue.poll();
            out.println(message);
        }

        out.println("DONE");

    }
}