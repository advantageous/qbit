package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.HttpServerHttpHandler;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.vertx.builders.HttpServerVertxEmbeddedBuilder;
import org.boon.Boon;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/27/15.
 * engine1:wrk rhightower$ ./wrk -c 500 -d 10s http://localhost:8080/1 -H "X_USER_ID: RICK"  --timeout 100000s -t 8
 engine1:wrk rhightower$ ./wrk -c 500 -d 10s http://localhost:7070/1 -H "X_USER_ID: RICK"  --timeout 100000s -t 8
 */
public class WebServerRepeaterApplication {



    public static class MyHttpServerHttpHandler implements HttpServerHttpHandler {

        @Override
        public Callback<HttpRequest> httpRequestConsumer() {

            return request -> {

                request.getResponse().response(200, "application/json", "\"ok\"");
            };
        }

        @Override
        public Callback<WebSocketMessage> webSocketConsumer() {


            return message -> {


                message.getSender().send("\"hi websocket\"");
            };
        }

        @Override
        public Callback<WebSocketMessage> webSocketClosed() {
            return webSocketMessage -> puts("WEBSOCKET CLOSED " + webSocketMessage.getRemoteAddress());
        }

        @Override
        public Callback<Void> webSocketQueueIdle() {
            return new Callback<Void>() {
                @Override
                public void accept(Void aVoid) {

                }
            };
        }

        @Override
        public Callback<Void> requestQueueIdle() {
            return new Callback<Void>() {
                @Override
                public void accept(Void aVoid) {

                }
            };
        }
    }


    public static void main(final String... args) throws Exception {




        final HttpServer httpServer = new HttpServerVertxEmbeddedBuilder().setHttpWorkers(4)
                .setHandlerCallbackClass(MyHttpServerHttpHandler.class)
                .setPort(7070)
                .setFlushInterval(500)
                .setPollTime(20)
                .setManageQueues(true)
                .build();

        httpServer.start();

        Boon.gets();
    }

}

