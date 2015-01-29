package io.advantageous.qbit.vertx.builders;

import io.advantageous.qbit.http.*;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

public class HttpServerVertxEmbeddedBuilderTest {

    HttpServerVertxEmbeddedBuilder builder;
    HttpServer server;

    HttpClient client;

    boolean ok;


    public static class HttpServerHttpHandlerMock implements HttpServerHttpHandler {

        @Override
        public Callback<HttpRequest> httpRequestConsumer() {

            return request -> {

                puts("GOT REQUEST", request);
                request.getResponse().response(200, "application/json", "\"hi http\"");
            };
        }

        @Override
        public Callback<WebSocketMessage> webSocketConsumer() {


            return message -> {


                puts("GOT WEB_SOCKET", message);
                message.getSender().send("\"hi websocket\"");
            };
        }

        @Override
        public Callback<WebSocketMessage> webSocketClosed() {
            return new Callback<WebSocketMessage>() {
                @Override
                public void accept(WebSocketMessage webSocketMessage) {
                    puts("WEBSOCKET CLOSED " + webSocketMessage.getRemoteAddress());
                }
            };
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

    @Before
    public void setUp() throws Exception {

        builder = new HttpServerVertxEmbeddedBuilder();

        builder.setPort(9896);

        builder.setHandlerCallbackClass(HttpServerHttpHandlerMock.class);

        server = builder.build();
        server.start();

        Sys.sleep(200);

        client = new HttpClientBuilder().setPort(9896).setAutoFlush(true).build();


        client.start();

        Sys.sleep(200);


    }

    @Test
    public void test() throws Exception {

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        client.sendHttpRequest(new HttpRequestBuilder()
                .setUri("/foo").setTextResponse((code, mimeType, body) -> queue.add(body)).build());



        client.sendWebSocketMessage(new WebSocketMessageBuilder().setUri("/foo").setMessage("HI MOM")
                .setSender(message -> {
                    queue.add(message);
                })
                .build());


        client.flush();

        Sys.sleep(2000);

        ok = queue.size() == 2 || die(queue.size());

        for (String str : queue) {
            puts(str);
        }


        ok = queue.size() == 2 || die();

        final String returnWebSocket = queue.poll();
        final String  returnHttp = queue.poll();


        ok = returnWebSocket.equals("\"hi websocket\"") || die(returnWebSocket);

        ok = returnHttp.equals("\"hi http\"") || die(returnHttp);

    }
    @After
    public void tearDown() throws Exception {

        server.stop();

    }
}