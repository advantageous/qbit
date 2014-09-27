package org.qbit.spi;

import org.boon.json.JsonSerializer;
import org.boon.json.JsonSerializerFactory;
import org.boon.primitive.CharBuf;
import org.qbit.message.MethodCall;

import static org.qbit.service.Protocol.*;

/**
 * Created by Richard on 9/26/14.
 */
public class ProtocolEncoderVersion1 implements ProtocolEncoder {

    private static ThreadLocal<JsonSerializer> jsonSerializer = new ThreadLocal<JsonSerializer>(){
        @Override
        protected JsonSerializer initialValue() {
            return new JsonSerializerFactory().create();
        }
    };

    @Override
    public String encodeAsString(MethodCall<Object> methodCall) {

        CharBuf buf = CharBuf.createCharBuf();

        buf.addChar(PROTOCOL_MARKER);
        buf.addChar(PROTOCOL_VERSION_1);
        buf.addChar(PROTOCOL_SEPARATOR);


        buf.add(methodCall.id());
        buf.addChar(PROTOCOL_SEPARATOR);

        buf.add(methodCall.address());
        buf.addChar(PROTOCOL_SEPARATOR);


        buf.add(methodCall.returnAddress());
        buf.addChar(PROTOCOL_SEPARATOR);


        buf.addChar(PROTOCOL_SEPARATOR); //RESERVED


        buf.add(methodCall.objectName());
        buf.addChar(PROTOCOL_SEPARATOR);

        buf.add(methodCall.name());
        buf.addChar(PROTOCOL_SEPARATOR);


        final Object body = methodCall.body();



        final JsonSerializer serializer = jsonSerializer.get();
        if (body instanceof Iterable) {

            Iterable iter = (Iterable) body;


            for (Object bodyPart : iter) {

                serializer.serialize(buf, bodyPart);
                buf.addChar(PROTOCOL_ARG_SEPARATOR);
            }
        } else if (body!=null) {
            serializer.serialize(buf, body);
        }

        return buf.toString();

    }
}
