package io.advantageous.qbit.boon.spi;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.message.*;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoonProtocolEncoderTest {

    BoonProtocolEncoder boonProtocolEncoder = new BoonProtocolEncoder();
    BoonProtocolParser boonProtocolParser = new BoonProtocolParser();

    @Test
    public void methodCall() throws Exception {

        final String returnAddress = UUID.randomUUID().toString();
        MethodCallBuilder methodCallBuilder = MethodCallBuilder.methodCallBuilder()
                .setName("trade")
                .setObjectName("tradeservice")
                .setAddress("/foo/bar/service/trade")
                .setReturnAddress(returnAddress)
                .setTimestamp(System.currentTimeMillis())
                .setBody(new Trade()).setId(2L);

        MethodCall<Object> methodCall = methodCallBuilder.build();
        String string = boonProtocolEncoder.encodeMethodCalls(returnAddress, Lists.list(methodCall));

        List<Message<Object>> parse = boonProtocolParser.parse("/foo/bar/service/trade", string);

        assertEquals(1, parse.size());

        Message<Object> objectMessage = parse.get(0);

        assertTrue((objectMessage instanceof MethodCall));

        MethodCall<Object> afterParse = (MethodCall<Object>) objectMessage;
        assertEquals(methodCall.name(), afterParse.name());
        assertEquals(methodCall.address(), afterParse.address());
        assertEquals(methodCall.returnAddress(), afterParse.returnAddress());
        assertEquals(methodCall.timestamp(), afterParse.timestamp());
        assertEquals(methodCall.id(), afterParse.id());


    }

    @Test
    public void response() throws Exception {

        final String returnAddress = UUID.randomUUID().toString();
        ResponseBuilder responseBuilder = ResponseBuilder.responseBuilder().setReturnAddress(returnAddress)
                .setAddress("/foo/bar/service/trade").setBody(true).setId(999).setTimestamp(200);


        Response<Object> response = responseBuilder.build();
        String string = boonProtocolEncoder.encodeResponses(returnAddress, Lists.list(response));

        List<Message<Object>> parse = boonProtocolParser.parse("/foo/bar/service/trade", string);

        assertEquals(1, parse.size());

        Message<Object> objectMessage = parse.get(0);

        assertTrue((objectMessage instanceof Response));

        Response<Object> afterParse = (Response<Object>) objectMessage;
        assertEquals(response.address(), afterParse.address());
//        assertEquals(response.returnAddress(), afterParse.returnAddress());
        assertEquals(response.timestamp(), afterParse.timestamp());
        assertEquals(response.id(), afterParse.id());
        assertEquals(response.body().toString().toLowerCase(), afterParse.body().toString().toLowerCase());


    }

    public static class Trade {
        private String name = "IBM";
        private long id = 1L;
    }

}