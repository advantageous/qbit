package io.advantageous.qbit.http;

import io.advantageous.qbit.network.NetworkSender;


/**
 * Created by rhightower on 2/14/15.
 */
public class WebSocketBuilder {

    private  String remoteAddress;
    private  String uri;
    private  WebSocketSender networkSender;
    private  boolean binary;
    private  HttpServer server;

    public HttpServer getServer() {
        return server;
    }

    public void setServer(HttpServer server) {
        this.server = server;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public WebSocketSender getNetworkSender() {
        return networkSender;
    }

    public void setNetworkSender(WebSocketSender networkSender) {
        this.networkSender = networkSender;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }


}
