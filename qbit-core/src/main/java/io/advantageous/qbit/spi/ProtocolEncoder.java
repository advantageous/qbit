package io.advantageous.qbit.spi;

import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

import java.util.Collection;
import java.util.List;

/**
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public interface ProtocolEncoder {

    String encodeAsString(Response<Object> response);

    String encodeAsString(MethodCall<Object> methodCall);

    String encodeAsString(Collection<Message<Object>> messages);

}
