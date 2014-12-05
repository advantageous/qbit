package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;

/**
 * An end point is an address and a way to transmit method calls to a service.
 *
 * Created by Richard on 10/1/14.
 * @author rhightower
 */
public interface EndPoint {


    String address();


    void call(MethodCall<Object> methodCall);

    void flush();

}
