/*******************************************************************************

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
 *  ________ __________.______________
 *  \_____  \\______   \   \__    ___/
 *   /  / \  \|    |  _/   | |    |  ______
 *  /   \_/.  \    |   \   | |    | /_____/
 *  \_____\ \_/______  /___| |____|
 *         \__>      \/
 *  ___________.__                  ____.                        _____  .__                                             .__
 *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 *  .____    ._____.
 *  |    |   |__\_ |__
 *  |    |   |  || __ \
 *  |    |___|  || \_\ \
 *  |_______ \__||___  /
 *          \/       \/
 *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 *  __________           __  .__              __      __      ___.
 *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 *          \/     \/             \/     \/         \/       \/    \/
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
 *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by rhightower on 1/27/15.
 */
public class WebSocketServiceServerHandler {


    protected final int batchSize;
    protected final ProtocolEncoder encoder;
    protected final ProtocolParser parser;
    protected final long flushResponseInterval = 200;
    private final Logger logger = LoggerFactory.getLogger(WebSocketServiceServerHandler.class);
    private final boolean debug = logger.isDebugEnabled();
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final Map<String, WebSocketDelegate> webSocketDelegateMap = new ConcurrentHashMap<>(100);
    protected volatile long flushResponseLastTimestamp = 0;


    public WebSocketServiceServerHandler(int batchSize, ServiceBundle serviceBundle, ProtocolEncoder encoder, ProtocolParser parser) {
        this.batchSize = batchSize;
        this.encoder = encoder;
        this.parser = parser;

        this.methodCallSendQueue = serviceBundle.methodSendQueue();
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
     * @param webSocketMessage
     */
    public void handleWebSocketCall(final WebSocketMessage webSocketMessage) {

        if ( GlobalConstants.DEBUG ) logger.info("WebSocket message: " + webSocketMessage);


        WebSocketDelegate webSocketDelegate = webSocketDelegateMap.get(webSocketMessage.getRemoteAddress());

        if ( webSocketDelegate == null ) {
            webSocketDelegate = new WebSocketDelegate(batchSize, webSocketMessage);
            webSocketDelegateMap.put(webSocketMessage.getRemoteAddress(), webSocketDelegate);
        }


        final List<MethodCall<Object>> methodCallListToBeParsedFromBody = createMethodCallListToBeParsedFromBody(webSocketMessage.getRemoteAddress(), webSocketMessage.getMessage(), webSocketMessage);


        methodCallSendQueue.sendBatch(methodCallListToBeParsedFromBody);

    }

    public void handleResponseFromServiceBundleToWebSocketSender(Response<Object> response, WebSocketMessage originatingRequest) {
        final WebSocketMessage webSocketMessage = originatingRequest;
        try {

            //uts("handle WebSocket response", webSocketMessage.getRemoteAddress());
            final WebSocketDelegate webSocketDelegate = this.webSocketDelegateMap.get(webSocketMessage.getRemoteAddress());

            if ( webSocketDelegate == null ) {
                String responseAsText = encoder.encodeAsString(response);


                webSocketMessage.getSender().sendText(responseAsText);
            } else {
                webSocketDelegate.send(response);
            }
        } catch ( Exception ex ) {
            logger.warn("websocket unable to sendText response", ex);
        }
    }

    public List<MethodCall<Object>> createMethodCallListToBeParsedFromBody(String addressPrefix, Object body, Request<Object> originatingRequest) {

        List<MethodCall<Object>> methodCalls;

        if ( body != null ) {

            methodCalls = parser.parseMethodCallListUsingAddressPrefix(addressPrefix, body);

        } else {
            methodCalls = Collections.emptyList();

        }

        if ( methodCalls == null || methodCalls.size() == 0 ) {

            if ( originatingRequest instanceof WebSocketMessage ) {
                WebSocketMessage webSocketMessage = ( ( WebSocketMessage ) originatingRequest );

                final Response<Object> response = ResponseImpl.response(-1, Timer.timer().now(), "SYSTEM", "ERROR", "CAN'T HANDLE CALL", originatingRequest, true);
                final WebSocketSender sender = webSocketMessage.getSender();
                sender.sendText(encoder.encodeAsString(response));

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

            //uts("*** SENDING MESSAGES buildAndSendMessages", outputMessages.size() + 1);

            Response<Object> currentMessage = outputMessages.poll();

            while ( currentMessage != null ) {

                messages.add(currentMessage);
                currentMessage = outputMessages.poll();

            }

            if ( message != null ) {
                messages.add(message);
            }


            final String textMessage = encoder.encodeAsString(( Collection<Message<Object>> ) ( Object ) messages);


            serverWebSocket.getSender().sendText(textMessage);

            lastSend = now;


            //uts("*** JUST SENT buildAndSendMessages", messages.size(), lastSend);
        }


    }


}
