package io.advantageous.qbit.util;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;

import java.net.URI;

/**
 * Gets a remote proxy for a type
 *
 * @author gcc@rd.io (Geoff Chandler)
 */
public class RemoteFactoryUtil {

    /**
     * Do not allow this class to be instantiated.
     */
    private RemoteFactoryUtil() {
    }

    public static <T> T getRemote(final URI address, final Class<T> clazz) {
        return getRemoteByName(address, clazz, clazz.getName());
    }

    public static <T> T getRemoteByName(final URI address, final Class<T> clazz, final String name) {

        /* Start QBit client for WebSocket calls. */
        final Client client = ClientBuilder.clientBuilder()
                .setHost(address.getHost())
                .setPort(address.getPort())
                .setUri(address.getPath())
                .build();
        try {
            /* Create a proxy to the service. */
            final T clientProxy = client.createProxy(clazz, name);
            client.start();
            return clientProxy;
        } catch (Exception ex) {
            try {
                client.stop();
            } catch (Exception ex2) {
                //don't log, we are cleaning up.
            }
            /* Wrong place to handle the exception so just rethrow it. */
            throw new IllegalStateException(ex);
        }
    }
}
