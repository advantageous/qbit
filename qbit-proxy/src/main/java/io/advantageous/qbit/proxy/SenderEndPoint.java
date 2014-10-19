package io.advantageous.qbit.proxy;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.proxy.Sender;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.qbit.service.impl.NoOpBeforeMethodCall;
import io.advantageous.qbit.spi.ProtocolEncoder;

/**
 * Created by Richard on 10/1/14.
 *  @author Rick Hightower
 */
public class SenderEndPoint implements EndPoint {


    final ProtocolEncoder encoder;
    final String address;
    private final Sender<String> sender;
    private final BeforeMethodCall beforeMethodCall;


    public SenderEndPoint(ProtocolEncoder encoder, String address, Sender<String> sender, BeforeMethodCall beforeMethodCall) {
        this.encoder = encoder;
        this.address = address;

        this.beforeMethodCall = beforeMethodCall == null ? new NoOpBeforeMethodCall() : beforeMethodCall;
        this.sender = sender;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public void call(MethodCall<Object> methodCall) {

        beforeMethodCall.before(methodCall);
        sender.send(methodCall.returnAddress(), encoder.encodeAsString(methodCall));
    }
}
