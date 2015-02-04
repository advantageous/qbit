package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventBus;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.events.EventSubscriber;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.Callback;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.events.EventUtils.callbackEventListener;
import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class EventBusImplTest {

    EventBus eventBus;
    EventBusImpl eventBusImpl;
    String returnValue;
    int subscriberMessageCount;
    int consumerCount;

    boolean ok;


    @Before
    public void setup() {
        eventBusImpl = new EventBusImpl();
        eventBus = eventBusImpl;
        subscriberMessageCount = 0;
        consumerCount = 0;

    }

    @Test
    public void test() {

        String hello = "hello";

        eventBus.register("rick", new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event);
                returnValue = event.body().toString();
                subscriberMessageCount++;
            }
        });

        eventBus.register("rick", new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event);
                subscriberMessageCount++;
            }
        });

        eventBus.register("bob", callbackEventListener(o -> {
            puts(o);
            subscriberMessageCount++;
        }));


        eventBus.register("rick", callbackEventListener(o -> {
            puts(o, o.getClass());
            subscriberMessageCount++;
        }));

        eventBus.register("rick", new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event);
                consumerCount++;
            }
        });

        eventBus.send("rick", hello);


        ok = returnValue == hello || die();
        ok = consumerCount == 1 || die();
        ok = subscriberMessageCount == 3 || die();





    }

}