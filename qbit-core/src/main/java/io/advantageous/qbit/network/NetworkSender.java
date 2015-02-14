package io.advantageous.qbit.network;

/**
 * Created by rhightower on 2/14/15.
 */
public interface NetworkSender {

    void sendText(String message);
    default void sendBytes(byte[] message) {
        throw new UnsupportedOperationException();
    }
    default void close() {
    }
    default void open(NetSocket netSocket) {
    }
}
