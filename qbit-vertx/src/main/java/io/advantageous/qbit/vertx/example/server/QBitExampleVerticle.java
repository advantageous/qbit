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

package io.advantageous.qbit.vertx.example.server;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.vertx.example.model.EmployeeManagerImpl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.platform.Verticle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.boon.Boon.puts;

public class QBitExampleVerticle extends Verticle {

    private ServiceBundle serviceBundle;

    private  HttpServer httpServer;

    private ReceiveQueue<Response<Object>> responses;

    private ProtocolEncoder encoder;


    private Map<String, ServerWebSocket> webSocketMap = new ConcurrentHashMap<>();

    public void start() {


        container.logger().info("QBitVerticle started");

        serviceBundle = QBit.factory().createServiceBundle("/services");

        serviceBundle.addService("/employeeService", new EmployeeManagerImpl());

        encoder = QBit.factory().createEncoder();

        httpServer = vertx.createHttpServer();
        httpServer.setTCPKeepAlive(true);
        httpServer.setTCPNoDelay(true);
        httpServer.setSoLinger(0);
        httpServer.setMaxWebSocketFrameSize(100_000_000);


        httpServer.websocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(ServerWebSocket event) {

                puts("GOT CONNECTION", event.path(), event.uri(), serviceBundle.address());

                if (event.uri().startsWith(serviceBundle.address())) {
                    handleWebSocket(event);
                }
            }
        }).requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {

                event.response().end("pong\n");
            }
        });

        httpServer.listen(8080);

        vertx.setPeriodic(50, new Handler<Long>() {
            @Override
            public void handle(Long event) {
                handleServiceBundleFlush();
            }
        });

        vertx.setPeriodic(5, new Handler<Long>() {
            @Override
            public void handle(Long event) {
                drainServiceQueue();
            }
        });


        responses = serviceBundle.responses();



    }

    private void drainServiceQueue() {
        final Iterable<Response<Object>> responsesBatch = responses.readBatch();

        for (Response<Object> response : responsesBatch) {
            final ServerWebSocket serverWebSocket = webSocketMap.get(response.returnAddress());

            if (serverWebSocket != null) {
                String responseAsText = encoder.encodeAsString(response);
                serverWebSocket.writeTextFrame(responseAsText);
            }
        }

    }


    private  void handleWebSocket(final ServerWebSocket websocket) {

        websocket.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {
                handleWebSocketData(websocket, event.toString());
            }
        });

        websocket.closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {

                handleWebSocketClosed(websocket);
            }
        });

    }

    private  void handleWebSocketClosed(ServerWebSocket websocket) {

    }

    private  void handleWebSocketData(ServerWebSocket websocket, String message) {

        final WebSocketMessage webSocketMessage = new WebSocketMessage(websocket.uri(), message, websocket.remoteAddress().toString(), null);

        final MethodCall<Object> methodCall = QBit.factory().createMethodCallToBeParsedFromBody(websocket.remoteAddress().toString(), message, webSocketMessage);
        serviceBundle.call(methodCall);

        puts("Websocket data", methodCall.returnAddress(), websocket, message);

        webSocketMap.put(methodCall.returnAddress(), websocket);


    }


    private void handleServiceBundleFlush() {
        serviceBundle.flushSends();
    }

}
