package io.advantageous.qbit.sender;


/**
 * Created by Richard on 10/1/14.
 * This could be a TCP/IP connection, a websocket, an HTTP long poll, etc.
 * It just represents some sort of output stream.
 * We use this so our code is not tied to for example vertx.
 * @author Rick Hightower
 */
public interface Sender <T>{

    void send(String returnAddress, T buffer);
}
