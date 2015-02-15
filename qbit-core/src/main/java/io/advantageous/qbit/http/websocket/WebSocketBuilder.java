package io.advantageous.qbit.http.websocket;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.websocket.impl.WebSocketImpl;


/**
 * Created by rhightower on 2/14/15.
 */
public class WebSocketBuilder {

    public static WebSocketBuilder webSocketBuilder() {
        return new WebSocketBuilder();
    }

    private  String remoteAddress;
    private  String uri;
    private  boolean open;
    private WebSocketSender webSocketSender;
    private  boolean binary;
    private HttpServer server;
    public HttpServer getServer() {
        return server;
    }

    public WebSocketBuilder setServer(HttpServer server) {
        this.server = server;
        return this;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public WebSocketBuilder setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public WebSocketBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public WebSocketSender getWebSocketSender() {
        return webSocketSender;
    }

    public WebSocketBuilder setWebSocketSender(WebSocketSender webSocketSender) {
        this.webSocketSender = webSocketSender;
        return this;
    }

    public boolean isBinary() {
        return binary;
    }

    public boolean isOpen() {
        return open;
    }

    public WebSocketBuilder setOpen(boolean open) {
        this.open = open;
        return this;
    }

    public WebSocketBuilder setBinary(boolean binary) {
        this.binary = binary;
        return this;
    }

    public WebSocket build() {
        return new WebSocketImpl(getRemoteAddress(), getUri(), isOpen(), isBinary(),
                webSocketSender);
    }


}
