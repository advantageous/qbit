package io.advantageous.qbit.jms;

import static java.lang.System.out;

public class ReceiverMain  {

    public static void main(final String... args) {
        final JmsService jmsService = JmsServiceBuilder.newJmsServiceBuilder()
                .setDefaultDestination("foobarQueue").build();

        String message;
        do {
            message = jmsService.receiveTextMessage();
            out.println(message);
        } while (message!=null);

        out.println("DONE");

    }
}