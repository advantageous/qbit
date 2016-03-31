package io.advantageous.qbit.client;

/**
 * Remote clients and even non-remote clients can implement this
 * interface. This allows detection of host/port and the ability to close
 * the client without keeping a reference to the Client object.
 *
 * @author rick hightower
 */
public interface RemoteTCPClientProxy extends ClientProxy {
    default int port() {
        return 0;
    }

    default String host() {
        return "localProxy";
    }

    default boolean connected() {
        return true;
    }

    default boolean remote() {
        return true;
    }

    default void silentClose() {
    }
}
