package io.advantageous.qbit.client;

/**
 *
 * All clients implement this method which when called flushes outstanding calls to the server.
 *
 * @author rhightower
 *
 * Created by rhightower on 12/4/14.
 */
public interface ClientProxy {

    void clientProxyFlush();
}
