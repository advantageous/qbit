package io.advantageous.qbit.sender;


/**
 * Created by Richard on 10/1/14.
 * @author Rick Hightower
 */
public interface Sender <T>{

    void send(String returnAddress, T buffer);
}
