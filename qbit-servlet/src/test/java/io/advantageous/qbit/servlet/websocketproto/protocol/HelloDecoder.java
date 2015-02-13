package io.advantageous.qbit.servlet.websocketproto.protocol;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import org.boon.Boon;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import static org.boon.Boon.puts;

/**
* Created by rhightower on 2/12/15.
*/
public class HelloDecoder implements Decoder.Text<Hello> {

    @Override
    public void init(final EndpointConfig config) {
    }

    @Override
    public Hello decode(final String value) throws DecodeException {
        Hello hello = Boon.fromJson(value, Hello.class);
        puts("DECODER CALLED", hello);
        return hello;
     }

    @Override
    public boolean willDecode(final String str) {
        return true;
    }

    @Override
    public void destroy() {
    }
}
