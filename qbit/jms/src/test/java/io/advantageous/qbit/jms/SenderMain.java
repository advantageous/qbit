package io.advantageous.qbit.jms;

public class SenderMain {
    public static void main(final String... args) {
        final JmsService jmsService = JmsServiceBuilder.newJmsServiceBuilder()
                .setDefaultDestination("foobarQueue").build();
        jmsService.sendTextMessage("foo" );
        for (int i=0; i < 10; i++) {
            jmsService.sendTextMessage("foo" + i);
        }
    }
}
