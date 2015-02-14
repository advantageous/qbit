package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.WebSocketMessage;
import org.boon.Str;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/14/15.
 */
public class JettyQBitHttpClient implements HttpClient {


    private final org.eclipse.jetty.client.HttpClient httpClient = new
            org.eclipse.jetty.client.HttpClient();
    private final WebSocketClient client = new WebSocketClient();


    private final String host;
    private final int port;

    public JettyQBitHttpClient(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void sendHttpRequest(HttpRequest request) {


            final String uri = Str.add("http://", host, ":", Integer.toString(port), request.getUri());


            final String method = request.getMethod();
            HttpMethod jettyMethod=HttpMethod.GET;

            switch (method) {
                case "GET":
                    jettyMethod = HttpMethod.GET;
                    break;
                case "POST":
                    jettyMethod = HttpMethod.POST;
                    break;
                case "HEAD":
                    jettyMethod = HttpMethod.HEAD;
                    break;
                case "PUT":
                    jettyMethod = HttpMethod.PUT;
                    break;
                case "OPTIONS":
                    jettyMethod = HttpMethod.OPTIONS;
                    break;
                case "DELETE":
                    jettyMethod = HttpMethod.DELETE;
                    break;
                case "TRACE":
                    jettyMethod = HttpMethod.TRACE;
                    break;
                case "CONNECT":
                    jettyMethod = HttpMethod.CONNECT;
                    break;
                case "MOVE":
                    jettyMethod = HttpMethod.MOVE;
                    break;
                case "PROXY":
                    jettyMethod = HttpMethod.PROXY;
                    break;
            }




            httpClient.newRequest(uri)
                    .method(jettyMethod).send(new BufferingResponseListener(1_000_000) {

                @Override
                public void onComplete(Result result) {

                    if (!result.isFailed())
                    {
                        byte[] responseContent = getContent();

                        if (request.getResponse().isText()) {
                            String responseString = new String(responseContent, StandardCharsets.UTF_8);

                            request.getResponse().response(result.getResponse().getStatus(),
                                    result.getResponse().getHeaders().get(HttpHeader.CONTENT_TYPE),
                                    responseString);
                        } else {
                            request.getResponse().response(result.getResponse().getStatus(),
                                    result.getResponse().getHeaders().get(HttpHeader.CONTENT_TYPE),
                                    responseContent);

                        }
                    }

                }
            });

    }




    @Override
    public void sendWebSocketMessage(final WebSocketMessage webSocketMessage) {

        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        final String uri = Str.add("ws://", host, ":", Integer.toString(port), webSocketMessage.getUri());


        WebSocketListener webSocketListener
        = new WebSocketListener() {
            Session session;
            @Override
            public void onWebSocketBinary(byte[] payload, int offset, int len) {

            }

            @Override
            public void onWebSocketClose(int statusCode, String reason) {
                puts("CLIENT WEB_SOCKET CLOSE");


            }

            @Override
            public void onWebSocketConnect(Session session) {

                puts("CLIENT WEB_SOCKET CONNECT");
                this.session = session;
                session.getRemote().sendStringByFuture(webSocketMessage.getMessage());
            }

            @Override
            public void onWebSocketError(Throwable cause) {
                puts("CLIENT WEB_SOCKET ERROR", cause);

            }

            @Override
            public void onWebSocketText(String message) {

                puts("CLIENT GOT MESSAGE", message);

                webSocketMessage.getSender().send(message);
            }
        };

        try {

            client.connect(webSocketListener, new URI(uri), request);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {

    }

    @Override
    public HttpClient start() {
        try {
            httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    @Override
    public void flush() {

    }

    @Override
    public void stop() {

        try {
            httpClient.stop();
            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
