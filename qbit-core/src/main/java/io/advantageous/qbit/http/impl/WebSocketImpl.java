package io.advantageous.qbit.http.impl;

import io.advantageous.qbit.http.WebSocket;
import io.advantageous.qbit.http.WebSocketSender;
import io.advantageous.qbit.network.impl.NetSocketBase;

/**
 * Created by rhightower on 2/14/15.
 */
public class WebSocketImpl extends NetSocketBase implements WebSocket {
    public WebSocketImpl(String remoteAddress, String uri, boolean open, boolean binary, WebSocketSender webSocketSender) {
        super(remoteAddress, uri, open, binary, webSocketSender);
    }
}
