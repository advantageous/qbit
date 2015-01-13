package io.advantageous.qbit.http;

/**
 * Allows one to create a WebSocket message to send.
 * Created by rhightower on 10/24/14.
 * @author rhightower
 */
public class WebSocketMessageBuilder {


    private String uri;
    private String message;
    private WebsSocketSender sender;
    private String remoteAddress;

    public String getUri() {
        return uri;
    }

    public WebSocketMessageBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public WebSocketMessageBuilder setMessage(String message) {
        this.message = message;
        return this;

    }

    public WebsSocketSender getSender() {
        return sender;
    }

    public WebSocketMessageBuilder setSender(WebsSocketSender sender) {
        this.sender = sender;
        return this;

    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public WebSocketMessageBuilder setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;

    }

    public WebSocketMessage build() {
        return new WebSocketMessage(uri, message, remoteAddress, sender);
    }
}
