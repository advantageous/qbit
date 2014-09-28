package org.qbit.spi;

import org.qbit.message.Message;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;

import java.util.List;

/**
 * Created by Richard on 9/26/14.
 */
public interface ProtocolEncoder {



    String encodeAsString(Response<Object> response);

    String encodeAsString(MethodCall<Object> methodCall);


    String encodeAsString(List<Message<Object>> methodCalls);

}
