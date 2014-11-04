package io.advantageous.qbit;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.util.MultiMap;

/**
 * Main factory for QBit. This gets used internally to create / parse methods.
 * @author rhightower
 */
public interface Factory {

    /**
     * Create a method call based on a body that we are parsing from  a POST body or WebSocket message for example.
     * @param address address of method (this can override what is in the body)
     * @param returnAddress return address, which is a moniker for where we want to return the results
     * @param objectName name of the object (optional)
     * @param methodName name of the method (optional)
     * @param args arguments and possibly more (could be whole message encoded)
     * @param params params, usually request parameters
     * @return new method call object returned.
     */
    MethodCall<Object> createMethodCallToBeParsedFromBody(String address,
                                                          String returnAddress,
                                                          String objectName,
                                                          String methodName,
                                                          Object args,
                                                          MultiMap<String, String> params);

    /**
     * Create a method call based on a body that we are parsing from  a POST body or WebSocket message for example.
     * @param address address of method (this can override what is in the body)
     * @param returnAddress return address, which is a moniker for where we want to return the results
     * @param args arguments and possibly more (could be whole message encoded)
     * @param params params, usually request parameters
     * @return new method call object returned.
     */
    MethodCall<Object> createMethodCallByAddress(String address,
                                                 String returnAddress,
                                                 Object args,
                                                 MultiMap<String, String> params);

    /**
     * Create a method call based on a body that we are parsing from  a POST body or WebSocket message for example.
     * @param objectName name of the object (optional)
     * @param methodName name of the method (optional)
     * @param returnAddress return address, which is a moniker for where we want to return the results
     * @param args arguments and possibly more (could be whole message encoded)
     * @param params params, usually request parameters
     * @return new method call object returned.
     */

    MethodCall<Object> createMethodCallByNames(
            String methodName, String objectName, String returnAddress, Object args,
            MultiMap<String, String> params);

    /**
     * Create a service bundle.
     * @param path path to bundle (base URI really)
     * @return new service bundle
     */
    ServiceBundle createServiceBundle(String path);


    /**
     * Create a service
     * @param rootAddress base URI
     * @param serviceAddress service address URI
     * @param object object that implements the service
     * @param responseQueue the response queue.
     * @return new Service that was created
     *
     *
     *
     */
    Service createService(String rootAddress, String serviceAddress, Object object, Queue<Response<Object>> responseQueue);


    /**
     * Create an encoder.
     * @return encoder.
     */
    ProtocolEncoder createEncoder();

    /**
     * Creates a method call to be encoded and sent. This is usually called by a client (local or remote proxy).
     * @param id id of method call
     * @param address address of method
     * @param returnAddress return address, which is a moniker for where we want to return the results
     * @param objectName name of the object (optional)
     * @param methodName name of the method (optional)
     * @param timestamp when we sent this message
     * @param body arguments (could be a list or an array)
     * @param params additional parameters associated with this method call.
     * @return method call that we are sending
     */
    MethodCall<Object> createMethodCallToBeEncodedAndSent(long id, String address,
                                                          String returnAddress,
                                                          String objectName,
                                                          String methodName,
                                                          long timestamp,
                                                          Object body,
                                                          MultiMap<String, String> params);


    /**
     * Create a local client proxy
     * @param serviceInterface client interface to service
     * @param serviceName name of the service that we are proxying method calls to.
     * @param serviceBundle name of service bundle
     * @param <T> type of proxy
     * @return new proxy object
     */
    <T> T createLocalProxy(Class<T> serviceInterface, String serviceName, ServiceBundle serviceBundle);

    /**
     * Create a response object from a string (HTTP response, Websocket body)
     * @param text of response message
     * @return response object
     */
    Response<Object> createResponse(String text);

    /**
     * Create a remote proxy using a sender that knows how to send method body over wire
     * @param serviceInterface client view of service
     * @param uri uri of service
     * @param serviceName name of the service that we are proxying method calls to.
     * @param returnAddressArg return address
     * @param sender how we are sending the message over the wire
     * @param beforeMethodCall before method call
     * @param <T> type of service
     * @return remote proxy
     */
    <T> T createRemoteProxyWithReturnAddress(Class<T> serviceInterface, String uri, String serviceName, String returnAddressArg,
                                             Sender<String> sender,
                                             BeforeMethodCall beforeMethodCall);

    /**
     * Parses a method call using an address prefix and a body.
     * Useful for Websocket calls and POST calls (if you don't care about request params).
     * @param addressPrefix prefix of the address
     * @param message message that we are sending
     * @param originatingRequest the request that caused this method to be created
     * @return method call that we just created
     */
    MethodCall<Object> createMethodCallToBeParsedFromBody(String addressPrefix, Object message, Request<Object> originatingRequest);

    /**
     * Request request
     * @param request incoming request that we want to create a MethodCall from.
     * @param args args
     * @return request
     */
    MethodCall<Object> createMethodCallFromHttpRequest(Request<Object> request, Object args);
}
