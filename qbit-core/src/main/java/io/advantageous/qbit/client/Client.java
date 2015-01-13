package io.advantageous.qbit.client;

/**
 * This is the main interface for accessing the server from a client perspective.
 * With this interface you can create a client proxy.
 * A client proxy is an interface that will marshall calls to a remote server.
 *
 * @author rhightower
 */
public interface Client {



    /**
     * Creates a new client proxy given a client interface.
     *
     * @param serviceInterface client interface
     * @param serviceName      client name
     * @param <T>              class type of interface
     * @return new client proxy.. calling methods on this proxy marshals method calls to httpServer.
     */
    <T> T createProxy(final Class<T> serviceInterface,
                             final String serviceName);


    void flush();

    void start();

    void stop();

}
