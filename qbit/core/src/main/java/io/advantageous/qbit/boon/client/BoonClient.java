/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
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
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.boon.client;

import io.advantageous.boon.core.*;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.primitive.Arry;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.sender.Sender;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.qbit.service.Protocol.PROTOCOL_ARG_SEPARATOR;


/**
 * Factory to createWithWorkers client proxies using interfaces.
 * created by Richard on 10/2/14.
 *
 * @author Rick Hightower
 */
public class BoonClient implements Client {

    private final String uri;
    /**
     * The server we are calling.
     */
    private final HttpClient httpServerProxy;
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(BoonClient.class);

    /**
     * Request batch size for queuing.
     */
    private final int requestBatchSize;
    private final boolean debug = logger.isDebugEnabled();
    private final BeforeMethodSent beforeMethodSent;
    /**
     * Map of handlers so we can do the whole async call back thing.
     */
    private final Map<HandlerKey, Callback<Object>> handlers = new ConcurrentHashMap<>();
    /**
     * List of client proxies that we are managing for periodic flush.
     */
    private final List<ClientProxy> clientProxies = new CopyOnWriteArrayList<>();
    private final AtomicBoolean connected = new AtomicBoolean();
    /**
     * Holds on to Boon cache so we don't have to recreate reflected gak.
     */
    Object context = Sys.contextToHold();
    private WebSocket webSocket;

    /**
     * @param httpClient       httpClient
     * @param uri              uri
     * @param requestBatchSize request batch size
     * @param beforeMethodSent before method sent interceptor
     */
    public BoonClient(final String uri,
                      final HttpClient httpClient,
                      final int requestBatchSize,
                      final BeforeMethodSent beforeMethodSent) {

        this.httpServerProxy = httpClient;
        this.uri = uri;
        this.requestBatchSize = requestBatchSize;
        this.beforeMethodSent = beforeMethodSent;
    }


    /**
     * Stop client. Stops processing call backs.
     */
    public void stop() {
        flush();
        if (httpServerProxy != null) {
            try {
                httpServerProxy.stop();
            } catch (Exception ex) {

                logger.warn("Problem closing httpServerProxy ", ex);
            }
        }
    }

    @Override
    public boolean connected() {
        return connected.get();
    }


    /**
     * Handles WebSocket messages and parses them into responses.
     * This does not handle batching or rather un-batching which we need for performance
     * we do handle batching in the parser/encoder.
     *
     * @param webSocketText websocket text
     */
    private void handleWebSocketReplyMessage(final String webSocketText) {


        final List<Message<Object>> messages = QBit.factory().createProtocolParser().parse("", webSocketText);


        //noinspection Convert2streamapi
        for (Message<Object> message : messages) {
            if (message instanceof Response) {
                @SuppressWarnings("unchecked") final Response<Object> response = ((Response) message);
                final String[] split = StringScanner.split(response.returnAddress(), (char) PROTOCOL_ARG_SEPARATOR);
                final HandlerKey key = split.length == 2 ? new HandlerKey(split[1], response.id()) : new HandlerKey(split[0], response.id());
                final Callback<Object> handler = handlers.get(key);

                if (handler != null) {
                    handleAsyncCallback(response, handler);
                    handlers.remove(key);
                } // else there was no handler, it was a one way method.
            }
        }
    }

    /**
     * Handles an async callbackWithTimeout.
     */
    private void handleAsyncCallback(final Response<Object> response, final Callback<Object> handler) {
        if (response.wasErrors()) {
            handler.onError(new Exception(response.body().toString()));
        } else {
            handler.accept(response.body());
        }
    }

    /**
     * Flush the calls and flush the proxy.
     */
    public void flush() {
        //noinspection Convert2streamapi
        for (ClientProxy clientProxy : clientProxies) {
            clientProxy.clientProxyFlush();
        }
        httpServerProxy.flush();
    }


    /**
     * Sends a message over websocket.
     *
     * @param message     message to sendText over WebSocket
     * @param serviceName message to sendText over WebSocket
     */
    private void send(final String serviceName, final String message, final Consumer<Exception> exceptionConsumer) {



        if (webSocket == null) {
            String webSocketURI;
            if (serviceName.startsWith(uri)) {
                webSocketURI = serviceName;
            } else {
                webSocketURI = Str.add(uri, "/", serviceName);
            }
            this.webSocket = httpServerProxy.createWebSocket(webSocketURI);
            wireWebSocket(serviceName, message);
        }

        try {
            if (webSocket.isClosed() && connected()) {
                this.webSocket.openAndNotify(netSocket -> {
                    connected.set(true);
                    webSocket.sendText(message, exceptionConsumer);
                }, exceptionConsumer);
            } else {
                webSocket.sendText(message);
            }
        } catch (Exception ex) {
            this.connected.set(false);
            if (debug) throw ex;
        }
    }

    private void wireWebSocket(final String serviceName, final String message) {

        this.webSocket.setErrorConsumer(error -> {

            if (error instanceof ConnectException) {
                connected.set(false);
            }
            logger.error(sputs(this.getClass().getName(),
                    "::Exception calling WebSocket from client proxy",
                    "\nService Name", serviceName, "\nMessage", message), error);
        });

        //noinspection Convert2MethodRef
        this.webSocket.setTextMessageConsumer(messageFromServer -> handleWebSocketReplyMessage(messageFromServer));
    }

    /**
     * Creates a new client proxy given a client interface.
     *
     * @param serviceInterface client interface
     * @param serviceName      client name
     * @param <T>              class type of interface
     * @return new client proxy.. calling methods on this proxy marshals method calls to httpServerProxy.
     */
    public <T> T createProxy(final Class<T> serviceInterface, final String serviceName) {


        return createProxy(serviceInterface, serviceName, Str.join('-', uri, serviceName, UUID.randomUUID().toString()));
    }

    /**
     * @param serviceInterface client interface
     * @param serviceName      client name
     * @param returnAddressArg specify a specific return address
     * @param <T>              class type of client interface
     * @return proxy object
     */
    public <T> T createProxy(final Class<T> serviceInterface, final String serviceName, final String returnAddressArg) {

        if (!serviceInterface.isInterface()) {
            die("QBitClient:: The service interface must be an interface");
        }

        /** Use this before call to register an async handler with the handlers map. */
        BeforeMethodCall beforeMethodCall = call -> {
            registerCallback(serviceInterface, call);
            prepareBody(call);
            return true;
        };


        final Sender<String> sender = new Sender<String>() {

            @Override
            public void send(String returnAddress, String buffer, Consumer<Exception> exceptionConsumer) {
                BoonClient.this.send(serviceName, buffer, exceptionConsumer);
            }

            @Override
            public void stop() {
                BoonClient.this.stop();
            }
        };

        T proxy = QBit.factory().createRemoteProxyWithReturnAddress(serviceInterface, uri, serviceName,
                httpServerProxy.getHost(),
                httpServerProxy.getPort(),
                connected,
                returnAddressArg, sender, beforeMethodCall, requestBatchSize, beforeMethodSent);

        if (proxy instanceof ClientProxy) {
            clientProxies.add((ClientProxy) proxy);
        }

        return proxy;
    }

    private void prepareBody(MethodCall call) {
        final Object body = call.body();
        if (body instanceof Object[]) {

            Object[] list = (Object[]) body;

            if (list.length > 0) {
                final Object o = list[0];
                if (o instanceof Callback) {
                    //noinspection unchecked
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
    }

    private <T> void registerCallback(Class<T> serviceInterface, MethodCall call) {
        final Callback callback = call.callback();

        if (callback != null) {
            handlers.put(new HandlerKey(call.returnAddress(), call.id()),
                    createHandler(serviceInterface, call, callback));
        }
    }

    /**
     * Create an async handler. Uses some generics reflection to see what the actual type is
     *
     * @param serviceInterface client interface
     * @param call             method call object
     * @param handler          handler that will handle the message
     * @param <T>              the class of hte client interface
     * @return the new handler
     */
    private <T> Callback createHandler(final Class<T> serviceInterface, final MethodCall call,
                                       final Callback handler) {

        final ClassMeta<T> clsMeta = ClassMeta.classMeta(serviceInterface);
        final MethodAccess method = clsMeta.method(call.name());

        Class<?> returnType = null;
        Class<?> compType = null;

        if (PromiseHandle.class.isAssignableFrom(method.returnType()) ) {

            Type t0 = method.method().getGenericReturnType();
            if (t0 instanceof ParameterizedType) {
                ParameterizedType parameterizedType = ((ParameterizedType) t0);
                Type type = ((parameterizedType != null ? parameterizedType.getActualTypeArguments().length : 0) > 0 ? (parameterizedType != null ? parameterizedType.getActualTypeArguments() : new Type[0])[0] : null);

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
        } else {
            if (method.parameterTypes().length > 0) {
                Type[] genericParameterTypes = method.getGenericParameterTypes();
                ParameterizedType parameterizedType = genericParameterTypes.length > 0 ? (ParameterizedType) genericParameterTypes[0] : null;

                Type type = ((parameterizedType != null ? parameterizedType.getActualTypeArguments().length : 0) > 0 ? (parameterizedType != null ? parameterizedType.getActualTypeArguments() : new Type[0])[0] : null);

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
        }
        final Class<?> actualReturnType = returnType;

        final Class<?> componentClass = compType;

        /** Create the return handler. */
        return new Callback<Object>() {
            @Override
            public void accept(Object event) {

                if (actualReturnType != null) {

                    if (componentClass != null && actualReturnType == List.class) {

                        try {
                            //noinspection unchecked
                            event = MapObjectConversion.convertListOfMapsToObjects(componentClass, (List) event);
                        } catch (Exception ex) {
                            if (event instanceof CharSequence) {
                                String errorMessage = event.toString();
                                if (errorMessage.startsWith("java.lang.IllegalState")) {
                                    handler.onError(new IllegalStateException(errorMessage));
                                    return;
                                } else {
                                    handler.onError(new IllegalStateException("Conversion error"));
                                    return;
                                }
                            } else {
                                handler.onError(new IllegalStateException("Conversion error"));
                                return;
                            }
                        }
                    } else {

                        switch (actualReturnType.getName()) {
                            case "java.lang.Boolean":
                                if (event instanceof Value) {
                                    event = ((Value) event).booleanValue();
                                } else {
                                    event = Conversions.coerce(actualReturnType, event);
                                }
                                break;
                            default:
                                event = Conversions.coerce(actualReturnType, event);
                        }
                    }
                    //noinspection unchecked
                    handler.accept(event);
                }


            }

            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }

            @Override
            public void onTimeout() {
                handler.onTimeout();
            }
        };


    }

    public void start() {
        startWithNotify(null);
    }

    public void startWithNotify(final Runnable runnable) {


        /** Adding the weak reference to get rid of the circular dependency
         * which seems to prevent this from getting collected.
         */
        final WeakReference<BoonClient> boonClientWeakReference =
                new WeakReference<>(this);
        this.httpServerProxy.periodicFlushCallback(aVoid -> {
            final BoonClient boonClient = boonClientWeakReference.get();
            if (boonClient != null) {
                boonClient.flush();
            }
        });

        if (runnable == null) {
            this.httpServerProxy.startClient();
        } else {
            this.httpServerProxy.startWithNotify(runnable);
        }
        connected.set(true);

    }

    /**
     * Key to store callbackWithTimeout in call back map.
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
            return messageId == that.messageId && !(returnAddress != null ? !returnAddress.equals(that.returnAddress) : that.returnAddress != null);
        }

        @Override
        public int hashCode() {
            int result = returnAddress != null ? returnAddress.hashCode() : 0;
            result = 31 * result + (int) (messageId ^ (messageId >>> 32));
            return result;
        }
    }
}
