package io.advantageous.qbit.http.websocket;

/**
 * Allows one to createWithWorkers a WebSocket message to send.
 * Created by rhightower on 10/24/14.
 * @author rhightower
 */
public class WebSocketMessageBuilder {


    public static  WebSocketMessageBuilder webSocketMessageBuilder() {
        return new WebSocketMessageBuilder();
    }

    private String uri;
    private Object message;
    private WebSocketSender sender;
    private String remoteAddress;
    private long messageId = -1;
    private long timestamp;

    public String getUri() {
        return uri;
    }

    public WebSocketMessageBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Object getMessage() {
        return message;
    }

    public WebSocketMessageBuilder setMessage(Object message) {
        this.message = message;
        return this;

    }

    public WebSocketSender getSender() {
        return sender;
    }

    public WebSocketMessageBuilder setSender(WebSocketSender sender) {
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
        return new WebSocketMessage(messageId, timestamp, uri, message, remoteAddress, sender);
    }

    public WebSocketMessageBuilder setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    public WebSocketMessageBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
