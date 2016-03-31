package io.advantageous.qbit.vertx.http.websocket;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketTextQueue;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class WebSocketTest {


    @Test
    public void testText() throws Exception {

        final int port = PortUtils.findOpenPortStartAt(4000);
        final HttpServer httpServer = HttpServerBuilder.httpServerBuilder().setPort(port).build();
        final AtomicReference<Object> bodyRef = new AtomicReference<>();
        final AtomicReference<String> messageRef = new AtomicReference<>();

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        httpServer.setWebSocketMessageConsumer(webSocketMessage -> {
            bodyRef.set(webSocketMessage.body());
            webSocketMessage.getSender().sendText("world");
            countDownLatch.countDown();
        });

        httpServer.startServerAndWait();


        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(port).buildAndStart();
        final WebSocket webSocket = httpClient.createWebSocket("/foo");


        webSocket.setTextMessageConsumer(message -> {

            messageRef.set(message);
            countDownLatch.countDown();
        });

        webSocket.openAndWait();

        webSocket.sendText("hello");

        countDownLatch.await(5, TimeUnit.SECONDS);


        assertEquals("world", messageRef.get());
        assertEquals("hello", bodyRef.get().toString());


    }

    @Test
    public void testTextQueue() throws Exception {

        final int port = PortUtils.findOpenPortStartAt(4000);
        final HttpServer httpServer = HttpServerBuilder.httpServerBuilder().setPort(port).build();
        final AtomicReference<Object> bodyRef = new AtomicReference<>();

        httpServer.setWebSocketMessageConsumer(webSocketMessage -> {
            bodyRef.set(webSocketMessage.body());
            webSocketMessage.getSender().sendText("world");
        });

        httpServer.startServerAndWait();


        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(port).buildAndStart();
        final WebSocket webSocket = httpClient.createWebSocket("/foo");


        final WebSocketTextQueue queue = new WebSocketTextQueue(webSocket);


        webSocket.openAndWait();

        webSocket.sendText("hello");


        String message = queue.receiveQueue().pollWait();


        assertEquals("world", message);
        assertEquals("hello", bodyRef.get().toString());


    }


    @Test
    public void testTextQueueWithBatchSize() throws Exception {

        final int port = PortUtils.findOpenPortStartAt(4000);
        final HttpServer httpServer = HttpServerBuilder.httpServerBuilder().setPort(port).build();
        final AtomicReference<Object> bodyRef = new AtomicReference<>();

        httpServer.setWebSocketMessageConsumer(webSocketMessage -> {
            bodyRef.set(webSocketMessage.body());
            webSocketMessage.getSender().sendText("world");
        });

        httpServer.startServerAndWait();


        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(port).buildAndStart();
        final WebSocket webSocket = httpClient.createWebSocket("/foo");


        final WebSocketTextQueue queue = new WebSocketTextQueue(webSocket, 100, 100, TimeUnit.MILLISECONDS);


        webSocket.openAndWait();

        webSocket.sendText("hello");


        String message = queue.receiveQueue().pollWait();


        assertEquals("world", message);
        assertEquals("hello", bodyRef.get().toString());


    }

    @Test
    public void testBinary() throws Exception {

        final int port = PortUtils.findOpenPortStartAt(4001);
        final HttpServer httpServer = HttpServerBuilder.httpServerBuilder().setPort(port).build();
        final AtomicReference<Object> bodyRef = new AtomicReference<>();
        final AtomicReference<byte[]> messageRef = new AtomicReference<>();

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        httpServer.setWebSocketMessageConsumer(webSocketMessage -> {
            bodyRef.set(webSocketMessage.body());
            webSocketMessage.getSender().sendBytes("world".getBytes());
            countDownLatch.countDown();
        });

        httpServer.startServerAndWait();


        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(port).buildAndStart();
        final WebSocket webSocket = httpClient.createWebSocket("/foo");
        webSocket.setBinaryMessageConsumer(message -> {

            messageRef.set(message);
            countDownLatch.countDown();
        });

        webSocket.openAndWait();

        webSocket.sendBinary("hello".getBytes());
        countDownLatch.await(5, TimeUnit.SECONDS);


        assertEquals("world", new String(messageRef.get(), StandardCharsets.UTF_8));
        assertEquals("hello", new String(((byte[]) bodyRef.get()), StandardCharsets.UTF_8));


    }

}
