package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.message.Event;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.eventbus.EventBusClusterBuilder.eventBusRingBuilder;
import static io.advantageous.qbit.util.PortUtils.useOneOfThePortsInThisRange;
import static org.junit.Assert.*;

public class EventBusRingBuilderTest {


    EventBusCluster eventBusCluster;

    int port = 0;
    //@Before
    public void setup() {

        port = useOneOfThePortsInThisRange(9000, 9900);
        EventBusClusterBuilder eventBusClusterBuilder = eventBusRingBuilder();
        eventBusClusterBuilder.setConsulHost("localhost");
        eventBusClusterBuilder.setReplicationPortLocal(port);

        eventBusCluster = eventBusClusterBuilder.build();
        eventBusCluster.start();

    }

    //@Test
    public void fakeTest() {
    }
    //@Test
    public void test() {

        eventBusCluster.eventManager().register("mom", new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event.channel(), event.body());
            }
        });

        for (int index = 0; index < 100; index++) {
            Sys.sleep(1000);
            eventBusCluster.eventManager().send("mom", "hi mom " + port);


        }

    }

    public static void main(String... args) {

        EventBusRingBuilderTest test = new EventBusRingBuilderTest();
        test.setup();
        test.test();

    }



}