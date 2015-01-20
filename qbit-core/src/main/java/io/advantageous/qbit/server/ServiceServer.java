package io.advantageous.qbit.server;

/**
 * Represents a server that marshals method calls to a service.
 * Created by Richard on 11/14/14.
 */
public interface ServiceServer extends Server {
    void initServices(Object... services);

    void initServices(Iterable services);

    void flush();
}
