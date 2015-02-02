package io.advantageous.qbit.server;

/**
 * Represents a server that marshals method calls to a service.
 * Created by Richard on 11/14/14.
 */
public interface ServiceServer extends Server {
    default ServiceServer initServices(Object... services) {
        throw new IllegalStateException("Not implemented");
    }


    default ServiceServer initServices(Iterable services) {
        throw new IllegalStateException("Not implemented");
    }

    default ServiceServer flush() {
        throw new IllegalStateException("Not implemented");
    }


    default ServiceServer startServer() {
        start();
        return this;
    }
}
