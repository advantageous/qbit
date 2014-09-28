package org.qbit.spi;

import org.boon.Boon;
import org.boon.Lists;
import org.boon.Str;
import org.boon.collections.MultiMap;
import org.boon.collections.MultiMaps;
import org.junit.Before;
import org.junit.Test;
import org.qbit.message.Message;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.service.method.impl.ResponseImpl;

import java.util.List;

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
        methodCall = MethodCallImpl.method(99L, "addr_", "return_", "oname_", "mname_", 0L, "args_", null);
    }

    @Test
    public void test() {
        ProtocolParserVersion1 parserVersion1 = new ProtocolParserVersion1();
        final MethodCall<Object> methodCall = parserVersion1.parseMethodCall("");


        ok = methodCall == null || die();

    }


    @Test
    public void testEncodeParseResponse() {

        ProtocolEncoderVersion1 encoder = new ProtocolEncoderVersion1();

        ResponseImpl<Object> response = new ResponseImpl<>(1L, 2L,
                "addr", "Raddr", null, "body");

        final String s = encoder.encodeAsString(response);

        puts(s);


        ProtocolParser parser = new ProtocolParserVersion1();
        final Response<Object> objectResponse = parser.parseResponse(s);

        Boon.equalsOrDie(response.id(), objectResponse.id());
        Boon.equalsOrDie(response.timestamp(), objectResponse.timestamp());
        Boon.equalsOrDie(response.address(), objectResponse.address());
        Boon.equalsOrDie(response.returnAddress(), objectResponse.returnAddress());
        Boon.equalsOrDie(response.body(), objectResponse.body().toString());

    }

    @Test
    public void testEncodeDecodeManyMethods() {

        ProtocolEncoderVersion1 encoder = new ProtocolEncoderVersion1();

        ProtocolParser parser = new ProtocolParserVersion1();
        MultiMap<String, String> multiMap = MultiMaps.multiMap();
        multiMap.add("fruit", "apple");
        multiMap.add("fruit", "pair");
        multiMap.add("fruit", "watermelon");

        multiMap.put("veggies", "yuck");


        MethodCallImpl method = (MethodCallImpl) MethodCallImpl.method("foo1", "bar1", "somebody1");
        method.params(multiMap);
        method.headers(multiMap);


        //long id, String address, String returnAddress, String objectName, String methodName,
        //long timestamp, Object args, MultiMap<String, String> params

        MethodCallImpl method2 = (MethodCallImpl) MethodCallImpl.method(
                1L, "addr", "__RETURNaddr__", "__objectNAME__", "__MEHTOD_NAME__",
                100L, "ARGS", null);
        method2.params(multiMap);



        MethodCallImpl method3 = (MethodCallImpl) MethodCallImpl.method("foo3", "bar3", "somebody3");
        method3.params(multiMap);

        MethodCallImpl method4 = (MethodCallImpl) MethodCallImpl.method("foo4", "bar4", "somebody4");
        method3.params(multiMap);

        ResponseImpl<Object> response1 = new ResponseImpl<>(method4, new Exception());


        ResponseImpl<Object> response2 = new ResponseImpl<>(method2, new Exception());

        final List<Message<Object>> list = Lists.list(
                response2, method, method2, method3, response1);




        final String s = encoder.encodeAsString(list);

        puts(s);

        final List<MethodCall<Object>> methodCalls = parser.parseMethods(s);
        puts(methodCalls);
        Boon.equalsOrDie(method3.address(), methodCalls.get(3).address());

        Boon.equalsOrDie(method3.name(), methodCalls.get(3).name());


        final MultiMap<String, String> params = methodCalls.get(2).params();

        final List<String> fruit = (List<String>) params.getAll("fruit");

        Boon.equalsOrDie(Lists.list("apple", "pair", "watermelon"), fruit);


        Boon.equalsOrDie("yuck", params.get("veggies"));


        final MultiMap<String, String> headers = methodCalls.get(1).headers();


        final List<String> hfruit = (List<String>) headers.getAll("fruit");

        Boon.equalsOrDie(Lists.list("apple", "pair", "watermelon"), hfruit);


        Boon.equalsOrDie("yuck", headers.get("veggies"));





        List<Message<Object>> messages = parser.parse(s);

        final Message<Object> message = messages.get(0);
        ok = message instanceof Response;

        Response respParsed = (Response) message;
        Boon.equalsOrDie(method2.id(), respParsed.id());

        Boon.equalsOrDie(method2.address(), respParsed.address());

        Boon.equalsOrDie(method2.returnAddress(), respParsed.returnAddress());

        Boon.equalsOrDie(method2.timestamp(), respParsed.timestamp());



    }

    @Test
    public void testEncodeDecodeMap() {
        MultiMap<String, String> multiMap = MultiMaps.multiMap();
        multiMap.add("fruit", "apple");
        multiMap.add("fruit", "pair");
        multiMap.add("fruit", "watermelon");

        multiMap.put("veggies", "yuck");

        final MethodCallImpl method = (MethodCallImpl) MethodCallImpl.method("foo", "bar", "");

        method.params(multiMap);

        ProtocolEncoderVersion1 encoder = new ProtocolEncoderVersion1();

        final String s = encoder.encodeAsString(method);

        puts(s);

        ProtocolParser parser = new ProtocolParserVersion1();

        final MethodCall<Object> parse = parser.parseMethodCall(s);

        final List<String> fruit = (List<String>) parse.params().getAll("fruit");

        Boon.equalsOrDie(Lists.list("apple", "pair", "watermelon"), fruit);


        Boon.equalsOrDie("yuck", parse.params().get("veggies"));



    }


    @Test
    public void testEncodeDecode() {

        ProtocolEncoderVersion1 encoder = new ProtocolEncoderVersion1();
        final String s = encoder.encodeAsString(methodCall);
        puts(s);

        ProtocolParserVersion1 parserVersion1 = new ProtocolParserVersion1();
        final MethodCall<Object> methodCallParsed = parserVersion1.parseMethodCall(
                s);


        puts(methodCall);

        Str.equalsOrDie(methodCall.name(), methodCallParsed.name());
        Str.equalsOrDie(methodCall.address(), methodCallParsed.address());
        Str.equalsOrDie(methodCall.objectName(), methodCallParsed.objectName());
        Str.equalsOrDie(methodCall.returnAddress(), methodCallParsed.returnAddress());
        puts(methodCallParsed.body(), methodCall.body());

//        Str.equalsOrDie(methodCall.returnAddress(), methodCallParsed.returnAddress());

//        Boon.equalsOrDie("\"" +  methodCall.body() + "\"", methodCallParsed.body());

        puts(methodCallParsed.body());

    }


}
