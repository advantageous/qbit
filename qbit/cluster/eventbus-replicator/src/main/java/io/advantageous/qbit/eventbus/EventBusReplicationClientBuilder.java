package io.advantageous.qbit.eventbus;


import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.events.spi.EventConnector;

public class EventBusReplicationClientBuilder {

    public static EventBusReplicationClientBuilder eventBusReplicationClientBuilder() {
        return new EventBusReplicationClientBuilder();
    }

    private ClientBuilder clientBuilder = ClientBuilder.clientBuilder();
    private String name =  "eventReplicator";

    public ClientBuilder clientBuilder() {
        return clientBuilder;
    }

    public ClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    public EventBusReplicationClientBuilder setClientBuilder(ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
        return this;
    }

    public String getName() {
        return name;
    }

    public EventBusReplicationClientBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public Client build() {

        return clientBuilder.build();
    }


    public EventConnector build(final Client client) {

        return client.createProxy(EventConnector.class, getName());
    }

}
