package org.qbit.spi;

import org.boon.collections.MultiMap;
import org.qbit.message.MethodCall;

/**
 * Created by Richard on 9/26/14.
 */
public interface ProtocolParser {

    boolean supports(Object object, MultiMap<String, String> params);

    MethodCall<Object> parse(String address, String objectName, String methodName, Object args, MultiMap<String, String> params);
}
