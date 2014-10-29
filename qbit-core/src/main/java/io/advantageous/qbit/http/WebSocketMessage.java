package io.advantageous.qbit.http;

/**
 * Created by rhightower on 10/22/14.
 * @author rhightower
 */
public class WebSocketMessage {

    private final String uri;
    private final String message;
    private final WebsSocketSender sender;
    private final String remoteAddress;


    public WebSocketMessage(
            final String uri, final String message, final String remoteAddress, final WebsSocketSender sender) {
        this.uri = uri;
        this.message = message;
        this.sender = sender;
        this.remoteAddress = remoteAddress;
    }

    public String getUri() {
        return uri;
    }

    public String getMessage() {
        return message;
    }

    public WebsSocketSender getSender() {
        return sender;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "uri='" + uri + '\'' +
                ", message='" + message + '\'' +
                ", sender=" + sender +
                ", remoteAddress='" + remoteAddress + '\'' +
                '}';
    }
}

