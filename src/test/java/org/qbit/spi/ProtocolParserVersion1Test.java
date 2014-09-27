package org.qbit.spi;

import org.boon.Boon;
import org.boon.Str;
import org.junit.Before;
import org.junit.Test;
import org.qbit.message.MethodCall;
import org.qbit.service.method.impl.MethodCallImpl;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class ProtocolParserVersion1Test {

    boolean ok;

    MethodCallImpl methodCall;

    @Before
    public void setup() {
        methodCall = MethodCallImpl.method(99L, "addr_", "return_", "oname_", "mname_", "args_", null);
    }

    @Test
    public void test() {
        ProtocolParserVersion1 parserVersion1 = new ProtocolParserVersion1();
        final MethodCall<Object> methodCall = parserVersion1.parse(null, null, null, null,
                "", null);


        ok = methodCall == null || die();

    }



    @Test
    public void testEncodeDecode() {

        ProtocolEncoderVersion1 encoder = new ProtocolEncoderVersion1();
        final String s = encoder.encodeAsString(methodCall);
        puts(s);

        ProtocolParserVersion1 parserVersion1 = new ProtocolParserVersion1();
        final MethodCall<Object> methodCallParsed = parserVersion1.parse(null, null, null, null,
                s, null);


        puts(methodCall);

        Str.equalsOrDie(methodCall.name(), methodCallParsed.name());
        Str.equalsOrDie(methodCall.address(), methodCallParsed.address());
        Str.equalsOrDie(methodCall.objectName(), methodCallParsed.objectName());
        Str.equalsOrDie(methodCall.returnAddress(), methodCallParsed.returnAddress());
        Boon.equalsOrDie("\"" +  methodCall.body() + "\"", methodCallParsed.body());

        puts(methodCallParsed.body());

    }


}
