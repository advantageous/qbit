package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;

/**
 * Created by Richard on 10/1/14.
 */
public interface EndPoint {


    String address();


    void call(MethodCall<Object> methodCall);

}
