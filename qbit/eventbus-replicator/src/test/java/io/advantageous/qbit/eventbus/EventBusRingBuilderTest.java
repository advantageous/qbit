package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.message.Event;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.util.PortUtils.useOneOfThePortsInThisRange;

public class EventBusRingBuilderTest {


    EventBusCluster eventBusCluster;

    int port = 0;

    public static void main(String... args) {

        EventBusRingBuilderTest test = new EventBusRingBuilderTest();
        test.setup();
        test.test();

    }

    @Before
    public void setup() {

        port = useOneOfThePortsInThisRange(9000, 9900);
        EventBusClusterBuilder eventBusClusterBuilder = EventBusClusterBuilder.eventBusClusterBuilder();
        eventBusClusterBuilder.setReplicationPortLocal(port);

        eventBusCluster = eventBusClusterBuilder.build();
        eventBusCluster.start();

    }

    //@Test
    public void fakeTest() {
    }

    @Test
    public void test() {

        eventBusCluster.eventManager().register("mom", new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event.channel(), event.body());
            }
        });

        for (int index = 0; index < 10000; index++) {
            Sys.sleep(1);
            eventBusCluster.eventManager().send("mom", "hi mom " + port);

            if (index % 3 == 0) {
                puts();
            }
        }

    }


}