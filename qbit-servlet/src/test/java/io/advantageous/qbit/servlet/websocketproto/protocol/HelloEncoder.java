package io.advantageous.qbit.servlet.websocketproto.protocol;

import io.advantageous.qbit.servlet.websocketproto.model.Hello;
import org.boon.Boon;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import static org.boon.Boon.puts;

/**
* Created by rhightower on 2/12/15.
*/
public class HelloEncoder implements Encoder.Text<Hello> {
    @Override
    public void init(final EndpointConfig config) {
    }

    @Override
    public String encode(final Hello hello) throws EncodeException {

        String json =  Boon.toJson(hello);
        puts("ENCODER CALLED", json);
        return json;

    }

    @Override
    public void destroy() {
    }
}
