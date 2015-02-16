/*******************************************************************************

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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.http;

import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.http.server.websocket.WebSocketMessageBuilder;
import io.advantageous.qbit.http.websocket.WebSocketSender;
import org.boon.core.reflection.BeanUtils;
import org.junit.Before;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class WebSocketMessageTest {


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

        webSocketMessage.getSender().sendText("hello mom");
        webSocketMessage.handled();


        ok |= webSocketMessage.isHandled() || die();


        ok |= called || die();

        ok |= lastMessage.equals("hello mom") || die();

        puts(webSocketMessage);

        ok |= webSocketMessage.equals(BeanUtils.copy(webSocketMessage)) || die();

        ok |= webSocketMessage.hashCode() == BeanUtils.copy(webSocketMessage).hashCode() || die();


        ok |= webSocketMessage.timestamp() > 0 || die();


        ok |= webSocketMessage.id() >= 0 || die();

        ok |= !webSocketMessage.hasParams() && webSocketMessage.params().size() == 0 || die();


        ok |= !webSocketMessage.hasHeaders() && webSocketMessage.headers().size() == 0 || die();


        final WebSocketMessageBuilder webSocketMessageBuilder = new WebSocketMessageBuilder();
        webSocketMessageBuilder.getMessage();
        webSocketMessageBuilder.getRemoteAddress();
        webSocketMessageBuilder.getSender();
        webSocketMessageBuilder.getUri();


    }

    public class WebsSocketSenderMock implements WebSocketSender {

        @Override
        public void sendText(String message) {


            lastMessage = message;
            called = true;
        }

        @Override
        public void sendBytes(byte[] message) {

        }
    }

}