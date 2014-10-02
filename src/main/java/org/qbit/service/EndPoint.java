package org.qbit.service;

import org.qbit.message.MethodCall;

/**
 * Created by Richard on 10/1/14.
 */
public interface EndPoint {


    String address();


    void call(MethodCall<Object> methodCall);

}
