package io.advantageous.qbit.spi;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * SPI interface to register default implementations of built-in factories and services.
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public class FactorySPI {

    private static final AtomicReference<Factory> ref = new AtomicReference<>();

    private static final AtomicReference<HttpServerFactory> httpServerFactoryRef = new AtomicReference<>();

    private static final AtomicReference<ClientFactory> clientFactoryRef = new AtomicReference<>();
    private static final AtomicReference<HttpClientFactory> httpClientFactoryRef = new AtomicReference<>();


    private static final AtomicReference<EventManagerFactory> eventManagerFactoryRef = new AtomicReference<>();


    public static void setEventManagerFactory(EventManagerFactory factory) {
        eventManagerFactoryRef.set(factory);
    }



    public static EventManagerFactory getEventManagerFactory() {
        return eventManagerFactoryRef.get();
    }

    public static Factory getFactory() {
        return ref.get();
    }


    public static void setFactory(Factory factory) {
        ref.set(factory);
    }


    public static HttpServerFactory getHttpServerFactory() {
        return httpServerFactoryRef.get();
    }


    public static void setHttpServerFactory(HttpServerFactory factory) {
        httpServerFactoryRef.set(factory);
    }


    public static void setHttpClientFactory(HttpClientFactory factory) {
        httpClientFactoryRef.set(factory);
    }

    public static HttpClientFactory getHttpClientFactory() {
        return httpClientFactoryRef.get();
    }


    public static ClientFactory getClientFactory() {
        return clientFactoryRef.get();
    }

    public static void setClientFactory(ClientFactory clientFactory) {
        clientFactoryRef.set(clientFactory);

    }
}
