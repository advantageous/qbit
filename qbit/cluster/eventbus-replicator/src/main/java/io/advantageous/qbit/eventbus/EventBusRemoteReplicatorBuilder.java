package io.advantageous.qbit.eventbus;

import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.EndpointServer;

/**
 * @author Rick Hightower
 */
public class EventBusRemoteReplicatorBuilder {


    public static EventBusRemoteReplicatorBuilder eventBusRemoteReplicatorBuilder() {
        return new EventBusRemoteReplicatorBuilder();
    }

    private EventManager eventManager;
    private EventConnector eventConnector;
    private String name = "eventReplicator";
    private EndpointServerBuilder endpointServerBuilder;

    public EventManager getEventManager() {
        return eventManager;
    }

    public EventBusRemoteReplicatorBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public String getName() {
        return name;
    }

    public EventBusRemoteReplicatorBuilder setName(String name) {
        this.name = name;
        return this;
    }


    public EventConnector getEventConnector() {
        return eventConnector;
    }

    public EventBusRemoteReplicatorBuilder setEventConnector(EventConnector eventConnector) {
        this.eventConnector = eventConnector;
        return this;
    }

    public EndpointServerBuilder getEndpointServerBuilder() {

        if (endpointServerBuilder ==null) {
            endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();

        }
        return endpointServerBuilder;
    }

    public EventBusRemoteReplicatorBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }



    public EndpointServerBuilder serviceServerBuilder() {
        return getEndpointServerBuilder();
    }


    public EndpointServer build() {

        EndpointServer endpointServer = serviceServerBuilder().build();

        if (this.getEventConnector()!=null && this.getEventManager() !=null) {
            throw new IllegalStateException("Only event connector or event manager can be set, not both");
        }

        EventRemoteReplicatorService eventRemoteReplicatorService;

        if (this.getEventConnector()==null && this.getEventManager() ==null) {
            eventRemoteReplicatorService = new EventRemoteReplicatorService();
        } else if (this.getEventConnector()!=null){
            eventRemoteReplicatorService = new EventRemoteReplicatorService(this.getEventConnector());
        } else {
            eventRemoteReplicatorService = new EventRemoteReplicatorService(this.getEventManager());
        }

        endpointServer.addServiceObject(this.getName(), eventRemoteReplicatorService);
        return endpointServer;
    }
}
