package io.advantageous.qbit.http;

/**
 * Created by rhightower on 10/24/14.
 * @author rhightower
 */
public class WebSocketMessageBuilderTmp {


    private String uri;
    private String message;
    private WebsSocketSender sender;
    private String remoteAddress;

    public String getUri() {
        return uri;
    }

    public WebSocketMessageBuilderTmp setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public WebSocketMessageBuilderTmp setMessage(String message) {
        this.message = message;
        return this;

    }

    public WebsSocketSender getSender() {
        return sender;
    }

    public WebSocketMessageBuilderTmp setSender(WebsSocketSender sender) {
        this.sender = sender;
        return this;

    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public WebSocketMessageBuilderTmp setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;

    }

    public WebSocketMessage build() {
        return new WebSocketMessage(uri, message, remoteAddress, sender);
    }
}
