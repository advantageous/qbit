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

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by rhightower on 1/27/15.
 */
public class WebSocketServiceServerHandler {


    protected final int batchSize;
    protected final ThreadLocal<ProtocolEncoder> encoderRef = new ThreadLocal<ProtocolEncoder>(){
        @Override
        protected ProtocolEncoder initialValue() {
            return QBit.factory().createEncoder();
        }
    };
    protected final ThreadLocal<ProtocolParser> parserRef = new ThreadLocal<ProtocolParser>() {
        @Override
        protected ProtocolParser initialValue() {

            return QBit.factory().createProtocolParser();
        }
    };


    protected final long flushResponseInterval = 200;
    private final Logger logger = LoggerFactory.getLogger(WebSocketServiceServerHandler.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final Map<String, WebSocketDelegate> webSocketDelegateMap = new ConcurrentHashMap<>(100);
    protected volatile long flushResponseLastTimestamp = 0;

    private final ExecutorService protocolParserThreadPool;

    private final ExecutorService protocolEncoderThreadPool;


    public WebSocketServiceServerHandler(
            final int batchSize,
            final ServiceBundle serviceBundle,
            final int parseWorkersCount,
            final int encodeWorkersCount) {
        this.batchSize = batchSize;

        this.methodCallSendQueue = serviceBundle.methodSendQueue();

        final AtomicInteger threadId = new AtomicInteger();
        protocolParserThreadPool = Executors.newFixedThreadPool(parseWorkersCount, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("WebSocketProtocolParser-"+threadId.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        protocolEncoderThreadPool = Executors.newFixedThreadPool(encodeWorkersCount, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("WebSocketProtocolEncoder-"+threadId.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });


    }


    public void handleWebSocketClose(final WebSocketMessage webSocketMessage) {
        webSocketDelegateMap.remove(webSocketMessage.getRemoteAddress());
    }

    public void webSocketQueueIdle(Void v) {
        methodCallSendQueue.flushSends();
    }

    /**
     * All WebSocket calls come through here.
     *
     * @param webSocketMessage websocket message
     */
    public void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if ( debug ) logger.info("WebSocket message: " + webSocketMessage);


        WebSocketDelegate webSocketDelegate = webSocketDelegateMap.get(webSocketMessage.getRemoteAddress());

        if ( webSocketDelegate == null ) {
            webSocketDelegate = new WebSocketDelegate(batchSize, webSocketMessage);
            webSocketDelegateMap.put(webSocketMessage.getRemoteAddress(), webSocketDelegate);
        }



        protocolParserThreadPool.execute(() -> {

            try {
                final List<MethodCall<Object>> methodCallListToBeParsedFromBody =
                        createMethodCallListToBeParsedFromBody(webSocketMessage.getRemoteAddress(),
                                webSocketMessage.getMessage(), webSocketMessage);

                methodCallSendQueue.sendBatch(methodCallListToBeParsedFromBody);
            }catch (Exception ex) {
                logger.error("", ex);
            }

        });

    }

    public void handleResponseFromServiceBundleToWebSocketSender(
            final Response<Object> response, final WebSocketMessage originatingRequest) {

        final WebSocketMessage webSocketMessage = originatingRequest;
        try {

            //uts("handle WebSocket response", webSocketMessage.getRemoteAddress());
            final WebSocketDelegate webSocketDelegate = this.webSocketDelegateMap.get(webSocketMessage.getRemoteAddress());

            if ( webSocketDelegate == null ) {



                String responseAsText = encoderRef.get().encodeAsString(response);


                webSocketMessage.getSender().sendText(responseAsText);
            } else {
                webSocketDelegate.send(response);
            }
        } catch ( Exception ex ) {
            logger.warn("websocket unable to sendText response", ex);
        }
    }

    public List<MethodCall<Object>> createMethodCallListToBeParsedFromBody(
            final String addressPrefix,
            final Object body,
            final Request<Object> originatingRequest) {


                List<MethodCall<Object>> methodCalls;

                if ( body != null ) {



                    methodCalls = parserRef.get().parseMethodCallListUsingAddressPrefix(addressPrefix, body);

                } else {
                    methodCalls = Collections.emptyList();

                }

                if ( methodCalls == null || methodCalls.size() == 0 ) {

                    if ( originatingRequest instanceof WebSocketMessage ) {
                        WebSocketMessage webSocketMessage = ( ( WebSocketMessage ) originatingRequest );

                        final Response<Object> response = ResponseImpl.response(-1, Timer.timer().now(), "SYSTEM", "ERROR",
                                "CAN'T HANDLE CALL", originatingRequest, true);
                        final WebSocketSender sender = webSocketMessage.getSender();
                        sender.sendText(encoderRef.get().encodeAsString(response));

                    }

                    return Collections.emptyList();
                }


                for ( MethodCall<Object> methodCall : methodCalls ) {
                    if ( methodCall instanceof MethodCallImpl ) {

                        MethodCallImpl method = ( ( MethodCallImpl ) methodCall );

                        method.originatingRequest(originatingRequest);
                    }
                }

                return methodCalls;





    }

    public void checkResponseBatchSend() {


        final long now = Timer.timer().now();


        long duration = now - flushResponseLastTimestamp;

        if ( duration > flushResponseInterval ) {
            flushResponseLastTimestamp = now;

            final Collection<WebSocketDelegate> values = this.webSocketDelegateMap.values();
            for ( WebSocketDelegate ws : values ) {

                long dur = now - ws.lastSend;

                if ( dur > flushResponseInterval ) {
                    ws.buildAndSendMessages(null, now);
                }
            }
        }


    }

    class WebSocketDelegate {
        final int requestBatchSize;

        final BlockingQueue<Response<Object>> outputMessages;

        final WebSocketMessage serverWebSocket;

        volatile long lastSend;

        private WebSocketDelegate(int requestBatchSize, WebSocketMessage serverWebSocket) {
            this.requestBatchSize = requestBatchSize;
            outputMessages = new ArrayBlockingQueue<>(requestBatchSize);
            this.serverWebSocket = serverWebSocket;
        }


        public void send(final Response<Object> message) {

            if ( !outputMessages.offer(message) ) {
                buildAndSendMessages(message, Timer.timer().now());
            }
        }

        private void buildAndSendMessages(final Response<Object> message, long now) {

            if ( outputMessages.size() == 0 && message == null ) {
                return;
            }

            List<Response<Object>> messages = new ArrayList<>(outputMessages.size() + 1);

            Response<Object> currentMessage = outputMessages.poll();

            while ( currentMessage != null ) {

                messages.add(currentMessage);
                currentMessage = outputMessages.poll();

            }

            if ( message != null ) {
                messages.add(message);
            }


            protocolEncoderThreadPool.execute(() -> {
                final String textMessage = encoderRef.get().encodeAsString((Collection<Message<Object>>) (Object) messages);
                serverWebSocket.getSender().sendText(textMessage);
            });


            lastSend = now;

        }


    }


}
