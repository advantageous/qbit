package io.advantageous.qbit.example.jms;

import io.advantageous.qbit.jms.JmsServiceBuilder;
import io.advantageous.qbit.jms.JmsTextQueue;
import io.advantageous.qbit.queue.JsonQueue;
import io.advantageous.qbit.queue.Queue;

public class JmsUtil {


    public static final String TODO_QUEUE = "todoQueue";

    public static Queue<Todo> createQueue() {

        /** Create a new JMS Builder which can emit JmsService objects. */
        final JmsServiceBuilder jmsBuilder = JmsServiceBuilder
                .newJmsServiceBuilder().setPort(5000)
                .setDefaultDestination(TODO_QUEUE);

        /** Create a QBit Queue that talks to JMS. */
        final Queue<String> textQueue = new JmsTextQueue(jmsBuilder);


        /**
         * Queue
         */
        final Queue<Todo> queue = new JsonQueue<>(Todo.class, textQueue);


        return queue;
    }

}
