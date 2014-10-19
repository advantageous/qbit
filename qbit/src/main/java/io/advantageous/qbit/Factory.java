package io.advantageous.qbit;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;

/**
 * @author rhightower
 */
public interface Factory {

    MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
                                                          String returnAddress,
                                                          String objectName,
                                                          String methodName,
                                                          Object args,
                                                          MultiMap<String, String> params);

    MethodCall<Object> createMethodCallByAddress(String address,
                                                 String returnAddress,
                                                 Object args,
                                                 MultiMap<String, String> params);

    MethodCall<Object> createMethodCallByNames(
            String methodName, String objectName, String returnAddress, Object args,
            MultiMap<String, String> params);

    ServiceBundle createBundle(String path);

    Service createService(String rootAddress, String serviceAddress, Object object, Queue<Response<Object>> responseQueue);


    ProtocolEncoder createEncoder();

    MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address,
                                                          String returnAddress,
                                                          String objectName,
                                                          String methodName,
                                                          long timestamp,
                                                          Object body,
                                                          MultiMap<String, String> params);


    <T> T createLocalProxy(Class<T> serviceInterface, String serviceName, ServiceBundle serviceBundle);

    Response<Object> createResponse(String text);

    <T> T createRemoteProxyWithReturnAddress(Class<T> serviceInterface, String uri, String serviceName, String returnAddressArg,
                                             Sender<String> sender,
                                             BeforeMethodCall beforeMethodCall);

    MethodCall<Object> createMethodCallToBeParsedFromBody(String addressPrefix, Object message);
}
