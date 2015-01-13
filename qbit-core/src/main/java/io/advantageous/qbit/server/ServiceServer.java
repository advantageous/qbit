package io.advantageous.qbit.server;

/**
 * Represents a server that marshals method calls to a service.
 * Created by Richard on 11/14/14.
 */
public interface ServiceServer extends Server {
    public void initServices(Object... services);

    public void initServices(Iterable services);
}
