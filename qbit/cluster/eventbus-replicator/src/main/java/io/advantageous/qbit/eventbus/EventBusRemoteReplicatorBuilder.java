package io.advantageous.qbit.eventbus;

import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;

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
    private ServiceServerBuilder serviceServerBuilder;

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

    public ServiceServerBuilder getServiceServerBuilder() {

        if (serviceServerBuilder==null) {
            serviceServerBuilder = ServiceServerBuilder.serviceServerBuilder();

        }
        return serviceServerBuilder;
    }

    public EventBusRemoteReplicatorBuilder setServiceServerBuilder(ServiceServerBuilder serviceServerBuilder) {
        this.serviceServerBuilder = serviceServerBuilder;
        return this;
    }



    public ServiceServerBuilder serviceServerBuilder() {
        return getServiceServerBuilder();
    }


    public ServiceServer build() {

        ServiceServer serviceServer = serviceServerBuilder().build();

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

        serviceServer.addServiceObject(this.getName(), eventRemoteReplicatorService);
        return serviceServer;
    }
}
