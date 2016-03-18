package io.advantageous.qbit.example.event.bus;


import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.Listen;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.DELETE;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.POST;
import io.advantageous.qbit.eventbus.EventBusCluster;
import io.advantageous.qbit.eventbus.EventBusClusterBuilder;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * curl http://localhost:8080/event/
 * curl -X POST  -H "Content-Type: application/json"  http://localhost:8080/event -d '{"id":"123", "message":"hello"}'
 */
@RequestMapping("/")
public class EventExampleService extends BaseService{

    private final EventManager eventManager;
    private final List<MyEvent> events = new ArrayList<>();

    public EventExampleService(final EventManager eventManager,
                               final String statKeyPrefix,
                               final Reactor reactor,
                               final Timer timer,
                               final StatsCollector statsCollector) {
        super(statKeyPrefix, reactor, timer, statsCollector);
        this.eventManager = eventManager;
        reactor.addServiceToFlush(eventManager);
    }

    @POST("/event")
    public boolean sendEvent(MyEvent event) {
        eventManager.sendArguments("myevent", event);
        return true;
    }


    @DELETE("/event/")
    public boolean clearEvents() {
         events.clear();
         return true;
    }

    @GET("/event/")
    public List<MyEvent> getEvents() {
        return events;
    }

    @Listen("myevent")
    public void listenEvent(final MyEvent event) {
        events.add(event);
    }

    public static void run(final int webPort, final int replicatorPort)  {

        final EventBusClusterBuilder eventBusClusterBuilder = EventBusClusterBuilder.eventBusClusterBuilder();
        eventBusClusterBuilder.setEventBusName("event-bus");
        eventBusClusterBuilder.setReplicationPortLocal(replicatorPort);
        final EventBusCluster eventBusCluster = eventBusClusterBuilder.build();
        eventBusCluster.start();


        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder
                .managedServiceBuilder().setRootURI("/")
                .setEventManager(eventBusCluster.eventManagerImpl())
                .setPort(webPort);

        final EventExampleService eventExampleService = new EventExampleService(
                eventBusCluster.createClientEventManager(),
                "event.",
                ReactorBuilder.reactorBuilder().build(),
                Timer.timer(),
                managedServiceBuilder.getStatServiceBuilder().buildStatsCollector());
        managedServiceBuilder.addEndpointService(eventExampleService);

        managedServiceBuilder.getEndpointServerBuilder().build().startServerAndWait();



    }
    public static void main(final String... args)  {

        run(8080, 7070);

    }
}
