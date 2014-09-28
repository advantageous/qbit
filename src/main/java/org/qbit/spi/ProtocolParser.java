package org.qbit.spi;

import org.boon.collections.MultiMap;
import org.qbit.message.Message;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;

import java.util.List;

/**
 * Created by Richard on 9/26/14.
 */
public interface ProtocolParser {

    boolean supports(Object object, MultiMap<String, String> params);

    MethodCall<Object> parseMethodCall(Object body);


    List<Message<Object>> parse(Object body);


    List<MethodCall<Object>> parseMethods(Object body);



    Response<Object> parseResponse(Object body);
}
