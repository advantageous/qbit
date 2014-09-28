package org.qbit.spi;

import org.qbit.message.MethodCall;

import java.util.List;

/**
 * Created by Richard on 9/26/14.
 */
public interface ProtocolEncoder {

    String encodeAsString(MethodCall<Object> methodCall);


    String encodeAsString(List<MethodCall<Object>> methodCalls);

}
