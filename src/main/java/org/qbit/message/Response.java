package org.qbit.message;


import org.qbit.message.Message;

/**
 * Created by Richard on 7/21/14.
 */
public interface Response <T> extends Message<T> {

    void body(T body);
}

