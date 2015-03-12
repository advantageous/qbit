package io.advantageous.qbit.client;

public interface RemoteTCPClientProxy extends ClientProxy {
    int port();
    String host();
    boolean connected();
}
