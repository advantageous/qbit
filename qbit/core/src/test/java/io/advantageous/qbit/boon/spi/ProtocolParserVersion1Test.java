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

package io.advantageous.qbit.boon.spi;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Str;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.core.IO.puts;


/**
 * created by Richard on 9/26/14.
 */
public class ProtocolParserVersion1Test {

    boolean ok;

    MethodCall<Object> methodCall;

    @Before
    public void setup() {
        methodCall = new MethodCallBuilder().setId(99L).setAddress("addr_").setReturnAddress("return_").setObjectName("oname_").setName("mname_").setTimestamp(0L).setBody("args_").build();
    }


    @Test
    public void testEncodeParseResponse() {

        BoonProtocolEncoder encoder = new BoonProtocolEncoder();

        ResponseImpl<Object> response = new ResponseImpl<>(1L, 2L, "addr", "Raddr", null, "body", null, false);

        final String s = encoder.encodeResponses("Raddr", Lists.list(response));


        ProtocolParser parser = new BoonProtocolParser();
        final Response<Object> objectResponse = parser.parseResponses("", s).get(0);

        Assert.assertEquals(response.id(), objectResponse.id());
        Assert.assertEquals(response.timestamp(), objectResponse.timestamp());
        Assert.assertEquals(response.address(), objectResponse.address());
        Assert.assertEquals(response.body(), objectResponse.body().toString());
        Assert.assertEquals(response.returnAddress(), objectResponse.returnAddress());

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

        final String s = encoder.encodeMethodCalls("", Lists.list(method));

        puts(s);

        ProtocolParser parser = new BoonProtocolParser();

        final MethodCall<Object> parse = parser.parseMethodCalls("", s).get(0);

        final List<String> fruit = (List<String>) parse.params().getAll("fruit");

        Assert.assertEquals(Lists.list("apple", "pair", "watermelon"), fruit);


        Assert.assertEquals("yuck", parse.params().get("veggies"));


    }


    @Test
    public void testEncodeDecode() {

        BoonProtocolEncoder encoder = new BoonProtocolEncoder();
        final String s = encoder.encodeMethodCalls("return_", Lists.list(methodCall));
        puts(s);

        BoonProtocolParser parserVersion1 = new BoonProtocolParser();
        final MethodCall<Object> methodCallParsed = parserVersion1.parseMethodCalls("return_", s).get(0);


        puts(methodCall);

        Str.equalsOrDie(methodCall.name(), methodCallParsed.name());
        Str.equalsOrDie(methodCall.address(), methodCallParsed.address());
        Str.equalsOrDie(methodCall.objectName(), methodCallParsed.objectName());
        puts(methodCallParsed.body(), methodCall.body());
        Assert.assertEquals(methodCall.returnAddress(), methodCallParsed.returnAddress());

//        Str.equalsOrDie(methodCall.returnAddress(), methodCallParsed.returnAddress());

//        Boon.equalsOrDie("\"" +  methodCall.body() + "\"", methodCallParsed.body());

        puts(methodCallParsed.body());

    }


}
