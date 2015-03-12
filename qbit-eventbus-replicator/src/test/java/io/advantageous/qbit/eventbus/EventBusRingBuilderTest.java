package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.message.Event;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.eventbus.EventBusRingBuilder.eventBusRingBuilder;
import static io.advantageous.qbit.util.PortUtils.useOneOfThePortsInThisRange;
import static org.junit.Assert.*;

public class EventBusRingBuilderTest {


    EventBusRing eventBusRing;

    int port = 0;
    //@Before
    public void setup() {

        port = useOneOfThePortsInThisRange(9000, 9900);
        EventBusRingBuilder eventBusRingBuilder = eventBusRingBuilder();
        eventBusRingBuilder.setConsulHost("localhost");
        eventBusRingBuilder.setReplicationPortLocal(port);

        eventBusRing = eventBusRingBuilder.build();
        eventBusRing.start();

    }

    @Test
    public void fakeTest() {
    }
        //@Test
    public void test() {

        eventBusRing.eventManager().register("mom", new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event.channel(), event.body());
            }
        });

        for (int index = 0; index < 100; index++) {
            Sys.sleep(1000);
            eventBusRing.eventManager().send("mom", "hi mom " + port);


        }

    }



}