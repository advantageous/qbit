package io.advantageous.qbit.spi;

import io.advantageous.qbit.MultiMap;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

import java.util.List;

/**
 * This parses the wire format to get method calls.  Could also be called a decoder.
 * <p>
 * Created by Richard on 9/26/14.
 */
public interface ProtocolParser {

    boolean supports(Object object, MultiMap<String, String> params);

    MethodCall<Object> parseMethodCall(Object body);

    List<Message<Object>> parse(Object body);

    List<MethodCall<Object>> parseMethods(Object body);

    Response<Object> parseResponse(Object body);
}
