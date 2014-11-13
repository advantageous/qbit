/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.qbit.vertx;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.*;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.method.impl.MethodCallImpl;
import io.advantageous.qbit.vertx.http.HttpClientVertx;
import org.boon.Boon;
import org.boon.Logger;
import org.boon.Str;
import org.boon.StringScanner;
import org.boon.core.Conversions;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MapObjectConversion;
import org.boon.core.reflection.MethodAccess;
import org.boon.primitive.Arry;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static io.advantageous.qbit.service.Protocol.PROTOCOL_ARG_SEPARATOR;
import static org.boon.Exceptions.die;


/**
 * Factory to create client proxies using interfaces.
 * Created by Richard on 10/2/14.
 *
 * @author Rick Hightower
 */
public class Client {

    /**
     * Host to connect to.
     */
    private final String host;
    /**
     * Port of host to connect to.
     */
    private final int port;
    /**
     * Uri at the host to connect to.
     */
    private final String uri;
    /**
     * Map of handlers so we can do the whole async call back thing.
     */
    private Map<HandlerKey, Callback<Object>> handlers = new ConcurrentHashMap<>();
    /**
     * Logger.
     */
    private Logger logger = Boon.logger(Client.class);


    /**
     * scheduledFuture, we need to shut this down on close.
     */
    private ScheduledFuture<?> scheduledFuture;
    private HttpClient httpServer;


    /**
     * @param host  host to connect to
     * @param port  port on host
     * @param uri   uri to connect to
     */
    public Client(String host, int port, String uri) {
        this.host = host;
        this.port = port;
        this.uri = uri;
        connect();
    }


    /**
     * Stop client. Stops processing call backs.
     */
    public void stop() {

        if (httpServer!=null) {
            try {
                httpServer.stop();
            } catch (Exception ex) {

                logger.warn(ex, "Problem closing httpServer connection", port, host);
            }
        }
    }


    /**
     * Handles websocket messages and parses them into responses.
     * This does not handle batching or rather un-batching which we need for performance
     * we do handle batching in the parser/encoder.
     *
     * @param websocketText websocket text
     */
    private void handleWebsocketQueueResponses(String websocketText) {
    /* Message comes in as a string but we parse it into a Response object. */
        final Response<Object> response = QBit.factory().createResponse(websocketText);


        final String[] split = StringScanner.split(response.returnAddress(),
                (char) PROTOCOL_ARG_SEPARATOR);
        HandlerKey key = new HandlerKey(split[1], response.id());


        final Callback<Object>  handler = handlers.get(key);

        if (handler != null) {

            handleAsyncCallback(response, handler);
        }
    }

    /**
     * Handles an async callback.
     */
    private void handleAsyncCallback(Response<Object> response, Callback<Object> handler) {

            if (response.wasErrors()) {
                handler.onError(new Exception(response.body().toString()));
            } else {
                handler.accept(response.body());
            }
    }

    public void flush() {

        httpServer.flush();
    }


    /**
     * Key to store callback in call back map.
     */
    private class HandlerKey {
        /**
         * Return address
         */
        final String returnAddress;
        /**
         * Message id
         */
        final long messageId;

        private HandlerKey(String returnAddress, long messageId) {
            this.returnAddress = returnAddress;
            this.messageId = messageId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HandlerKey that = (HandlerKey) o;
            return messageId == that.messageId
                    && !(returnAddress != null
                    ? !returnAddress.equals(that.returnAddress)
                    : that.returnAddress != null);
        }

        @Override
        public int hashCode() {
            int result = returnAddress != null ? returnAddress.hashCode() : 0;
            result = 31 * result + (int) (messageId ^ (messageId >>> 32));
            return result;
        }
    }


    /**
     * Sends a message over websocket.
     * @param message message to send over WebSocket
     *
     * @param serviceName message to send over WebSocket
     */
    public void send(String serviceName, String message) {


        final WebSocketMessage webSocketMessage = new WebSocketMessageBuilder()
                .setUri(Str.add(uri, "/", serviceName))
                .setMessage(message)
                .setSender(this::handleWebsocketQueueResponses).build();
        httpServer.sendWebSocketMessage(webSocketMessage);
    }


    /**
     * Creates a new client proxy given a service interface.
     *
     * @param serviceInterface service interface
     * @param serviceName      service name
     * @param <T>              class type of interface
     * @return new client proxy.. calling methods on this proxy marshals method calls to httpServer.
     */
    public <T> T createProxy(final Class<T> serviceInterface,
                             final String serviceName) {


        return createProxy(serviceInterface, serviceName,
                Str.join('-', uri, serviceName,
                        UUID.randomUUID().toString()));
    }

    /**
     * @param serviceInterface service interface
     * @param serviceName      service name
     * @param returnAddressArg specify a specific return address
     * @param <T>              class type of service interface
     * @return proxy object
     */
    public <T> T createProxy(final Class<T> serviceInterface,
                             final String serviceName,
                             String returnAddressArg
    ) {

        if (!serviceInterface.isInterface()) {
            die("QBitClient:: The service interface must be an interface");
        }

        /** Use this before call to register an async handler with the handlers map. */
        BeforeMethodCall beforeMethodCall = new BeforeMethodCall() {
            @Override
            public boolean before(final MethodCall call) {

                final Object body = call.body();
                if (body instanceof Object[]) {

                    Object[] list = (Object[]) body;

                    if (list.length > 0) {
                        final Object o = list[0];
                        if (o instanceof Callback) {
                            handlers.put(new HandlerKey(call.returnAddress(), call.id()),
                                    createHandler(serviceInterface, call, (Callback) o));

                            if (list.length - 1 == 0) {
                                list = new Object[0];
                            } else {
                                list = Arry.slc(list, 1); //Skip first arg it was a handler.
                            }

                        }
                        if (call instanceof MethodCallImpl) {
                            MethodCallImpl impl = (MethodCallImpl) call;
                            impl.setBody(list);
                        }

                    }
                }

                return true;
            }
        };
        return QBit.factory().createRemoteProxyWithReturnAddress(serviceInterface,
                uri,
                serviceName, returnAddressArg, (returnAddress, buffer) -> Client.this.send(serviceName, buffer), beforeMethodCall
        );
    }

    /**
     * Create an async handler. Uses some generics reflection to see what the actual type is
     *
     * @param serviceInterface service interface
     * @param call             method call object
     * @param handler          handler that will handle the message
     * @param <T>              the class of hte service interface
     * @return the new handler
     */
    private <T> Callback createHandler(final Class<T> serviceInterface, final MethodCall call, final Callback handler) {

        final ClassMeta<T> clsMeta = ClassMeta.classMeta(serviceInterface);
        final MethodAccess method = clsMeta.method(call.name());

        Class<?> returnType = null;

        Class<?> compType = null;
        if (method.parameterTypes().length > 0) {
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            ParameterizedType parameterizedType = genericParameterTypes.length > 0 ?
                    (ParameterizedType) genericParameterTypes[0] : null;

            Type type = (parameterizedType.getActualTypeArguments().length > 0 ? parameterizedType.getActualTypeArguments()[0] : null);

            if (type instanceof ParameterizedType) {
                returnType = (Class) ((ParameterizedType) type).getRawType();
                final Type type1 = ((ParameterizedType) type).getActualTypeArguments()[0];

                if (type1 instanceof Class) {
                    compType = (Class) type1;
                }
            } else if (type instanceof Class) {
                returnType = (Class<?>) type;
            }

        }
        final Class<?> actualReturnType = returnType;

        final Class<?> componentClass = compType;

        /** Create the return handler. */
        Callback<Object> returnHandler = new Callback<Object>() {
            @Override
            public void accept(Object event) {

                if (actualReturnType != null) {

                    if ( componentClass != null && actualReturnType == List.class ) {
                        event = MapObjectConversion.convertListOfMapsToObjects(componentClass, (List) event);
                    } else {
                        event = Conversions.coerce(actualReturnType, event);
                    }
                    handler.accept(event);
                }

            }
        };



        return returnHandler;
    }




    /**
     * Use vertx to connect to websocket httpServer that is hosting this service.
     */
    private void connect() {

        this.httpServer = new HttpClientBuilder().setPort(port).setHost(host).build();


    }

    public void run() {
        this.httpServer.run();
    }
}
