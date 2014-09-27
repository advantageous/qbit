package org.qbit.spi;

import org.qbit.message.MethodCall;

/**
 * Created by Richard on 9/26/14.
 */
public interface ProtocolEncoder {

    String encodeAsString(MethodCall<Object> methodCall);

}
