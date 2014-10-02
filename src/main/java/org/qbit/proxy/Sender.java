package org.qbit.proxy;

/**
 * Created by Richard on 10/1/14.
 */
public interface Sender <T>{

    void send(String returnAddress, T buffer);
}
