/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.spi;

import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.boon.Boon;
import org.boon.Lists;
import org.boon.Str;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class ProtocolParserVersion1Test {

    boolean ok;

    MethodCall<Object> methodCall;

    @Before
    public void setup() {
        methodCall = new MethodCallBuilder().setId(99L).setAddress("addr_").setReturnAddress("return_").setObjectName("oname_").setName("mname_").setTimestamp(0L).setBody("args_").build();
    }

    @Test
    public void test() {
        BoonProtocolParser parserVersion1 = new BoonProtocolParser();
        final MethodCall<Object> methodCall = parserVersion1.parseMethodCall("");


        ok = methodCall == null || die();

    }


    @Test
    public void testEncodeParseResponse() {

        BoonProtocolEncoder encoder = new BoonProtocolEncoder();

        ResponseImpl<Object> response = new ResponseImpl<>(1L, 2L, "addr", "Raddr", null, "body", null, false);

        final String s = encoder.encodeAsString(response);

        puts(s);


        ProtocolParser parser = new BoonProtocolParser();
        final Response<Object> objectResponse = parser.parseResponse(s);

        Boon.equalsOrDie(response.id(), objectResponse.id());
        Boon.equalsOrDie(response.timestamp(), objectResponse.timestamp());
        Boon.equalsOrDie(response.address(), objectResponse.address());
        Boon.equalsOrDie(response.returnAddress(), objectResponse.returnAddress());
        Boon.equalsOrDie(response.body(), objectResponse.body().toString());

    }

    @Test
    public void testEncodeDecodeManyMethods() {

        BoonProtocolEncoder encoder = new BoonProtocolEncoder();

        ProtocolParser parser = new BoonProtocolParser();
        MultiMap<String, String> multiMap = new MultiMapImpl(ArrayList.class);
        multiMap.add("fruit", "apple");
        multiMap.add("fruit", "pair");
        multiMap.add("fruit", "watermelon");

        multiMap.put("veggies", "yuck");


        MethodCall<Object> method = new MethodCallBuilder().setName("foo1").setBody("bar1").setAddress("somebody1").setParams(multiMap).setHeaders(multiMap).build();


        //long id, String address, String returnAddress, String objectName, String methodName,
        //long timestamp, Object args, MultiMap<String, String> params


        MethodCall<Object> method2 = new MethodCallBuilder().setId(1L).setAddress("addr").setReturnAddress("__RETURNaddr__").setObjectName("__objectNAME__").setName("__MEHTOD_NAME__").setTimestamp(100L).setBody("ARGS").setParams(multiMap).build();


        MethodCall<Object> method3 = new MethodCallBuilder().setName("foo3").setBody("bar3").setAddress("somebody3").setParams(multiMap).build();


        MethodCall<Object> method4 = new MethodCallBuilder().setName("foo4").setBody("bar4").setAddress("somebody4").setParams(multiMap).build();

        ResponseImpl<Object> response1 = new ResponseImpl<>(method4, new Exception());


        ResponseImpl<Object> response2 = new ResponseImpl<>(method2, new Exception());

        final List<Message<Object>> list = Lists.list(response2, method, method2, method3, response1);


        final String s = encoder.encodeAsString(list);

        puts(s);

        final List<MethodCall<Object>> methodCalls = parser.parseMethods(s);
        puts(methodCalls);
        Boon.equalsOrDie(method3.address(), methodCalls.get(3).address());

        Boon.equalsOrDie(method3.name(), methodCalls.get(3).name());


        final MultiMap<String, String> params = methodCalls.get(2).params();

        final List<String> fruit = ( List<String> ) params.getAll("fruit");

        Boon.equalsOrDie(Lists.list("apple", "pair", "watermelon"), fruit);


        Boon.equalsOrDie("yuck", params.get("veggies"));


        final MultiMap<String, String> headers = methodCalls.get(1).headers();


        final List<String> hfruit = ( List<String> ) headers.getAll("fruit");

        Boon.equalsOrDie(Lists.list("apple", "pair", "watermelon"), hfruit);


        Boon.equalsOrDie("yuck", headers.get("veggies"));


        List<Message<Object>> messages = parser.parse("", s);

        final Message<Object> message = messages.get(0);
        ok = message instanceof Response;

        Response respParsed = ( Response ) message;
        Boon.equalsOrDie(method2.id(), respParsed.id());

        Boon.equalsOrDie(method2.address(), respParsed.address());

        Boon.equalsOrDie(method2.returnAddress(), respParsed.returnAddress());

        Boon.equalsOrDie(method2.timestamp(), respParsed.timestamp());


    }

    @Test
    public void testEncodeDecodeMap() {
        MultiMap<String, String> multiMap = new MultiMapImpl<>(ArrayList.class);
        multiMap.add("fruit", "apple");
        multiMap.add("fruit", "pair");
        multiMap.add("fruit", "watermelon");

        multiMap.put("veggies", "yuck");

        MethodCall<Object> method = new MethodCallBuilder().setName("foo").setBody("bar").setAddress("somebody").setParams(multiMap).build();


        BoonProtocolEncoder encoder = new BoonProtocolEncoder();

        final String s = encoder.encodeAsString(method);

        puts(s);

        ProtocolParser parser = new BoonProtocolParser();

        final MethodCall<Object> parse = parser.parseMethodCall(s);

        final List<String> fruit = ( List<String> ) parse.params().getAll("fruit");

        Boon.equalsOrDie(Lists.list("apple", "pair", "watermelon"), fruit);


        Boon.equalsOrDie("yuck", parse.params().get("veggies"));


    }


    @Test
    public void testEncodeDecode() {

        BoonProtocolEncoder encoder = new BoonProtocolEncoder();
        final String s = encoder.encodeAsString(methodCall);
        puts(s);

        BoonProtocolParser parserVersion1 = new BoonProtocolParser();
        final MethodCall<Object> methodCallParsed = parserVersion1.parseMethodCall(s);


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
