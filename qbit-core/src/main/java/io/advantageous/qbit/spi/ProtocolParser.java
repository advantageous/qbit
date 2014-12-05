package io.advantageous.qbit.spi;

import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

import java.util.List;

/**
 * This parses the wire format to get method calls.  Could also be called a decoder.
 * <p>
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public interface ProtocolParser {

    boolean supports(Object object, MultiMap<String, String> params);

    MethodCall<Object> parseMethodCall(Object body);

    MethodCall<Object> parseMethodCallUsingAddressPrefix(String addressPrefix, Object body);

    List<Message<Object>> parse(String address, Object body);

    List<MethodCall<Object>> parseMethods(Object body);


    List<MethodCall<Object>> parseMethodCallListUsingAddressPrefix(String addressPrefix, Object body);

    Response<Object> parseResponse(Object body);
}
