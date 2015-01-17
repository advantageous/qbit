package io.advantageous.qbit.http;

import org.boon.core.reflection.BeanUtils;
import org.junit.Before;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class WebSocketMessageTest  {



    WebSocketMessage webSocketMessage;
    boolean called;
    boolean ok = true;
    String lastMessage = "";

    @Before
    public void setUp() throws Exception {

        webSocketMessage = new WebSocketMessageBuilder()
                .setUri("/foo")
                .setRemoteAddress("/blah")
                .setSender(new WebsSocketSenderMock())
                .setMessage("foo").build();
    }


    @Test
    public void test() {

        ok |= webSocketMessage.body().equals("foo") || die();

        ok |= webSocketMessage.isSingleton() || die();

        ok |= webSocketMessage.address().equals("/foo") || die();

        ok |= webSocketMessage.getUri().equals("/foo") || die();

        ok |= webSocketMessage.returnAddress().equals("/blah") || die();
        ok |= !webSocketMessage.isHandled() || die();

        webSocketMessage.getSender().send("hello mom");
        webSocketMessage.handled();



        ok |= webSocketMessage.isHandled() || die();


        ok |= called || die();

        ok |= lastMessage.equals("hello mom") || die();

        puts(webSocketMessage);

        ok |= webSocketMessage.equals(BeanUtils.copy(webSocketMessage)) || die();

        ok |= webSocketMessage.hashCode() == BeanUtils.copy(webSocketMessage).hashCode() || die();


        ok |= webSocketMessage.timestamp() > 0 || die();


        ok |= webSocketMessage.id() >= 0 || die();

        ok |= !webSocketMessage.hasParams() && webSocketMessage.params().size()==0 || die();


        ok |= !webSocketMessage.hasHeaders() && webSocketMessage.headers().size()==0 || die();


        final WebSocketMessageBuilder webSocketMessageBuilder = new WebSocketMessageBuilder();
        webSocketMessageBuilder.getMessage();
        webSocketMessageBuilder.getRemoteAddress();
        webSocketMessageBuilder.getSender();
        webSocketMessageBuilder.getUri();


    }

    public  class WebsSocketSenderMock implements WebsSocketSender {

        @Override
        public void send(String message) {


            lastMessage = message;
            called = true;
        }
    }

}