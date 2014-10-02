package org.qbit.proxy;

import org.qbit.message.MethodCall;
import org.qbit.service.EndPoint;
import org.qbit.spi.ProtocolEncoder;

/**
 * Created by Richard on 10/1/14.
 */
public class SenderEndPoint implements EndPoint {


    final ProtocolEncoder encoder;
    final String address;
    private final Sender<String> sender;


    public SenderEndPoint(ProtocolEncoder encoder, String address, Sender<String> sender) {
        this.encoder = encoder;
        this.address = address;

        this.sender = sender;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public void call(MethodCall<Object> methodCall) {

        sender.send(methodCall.returnAddress(), encoder.encodeAsString(methodCall));
    }
}
