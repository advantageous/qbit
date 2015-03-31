package io.advantageous.qbit.client;

public interface RemoteTCPClientProxy extends ClientProxy {
    default int port() {return 0;}
    default String host() {return "localProxy";}
    default boolean connected() {return true;}
    default boolean remote() {return true;}
}
