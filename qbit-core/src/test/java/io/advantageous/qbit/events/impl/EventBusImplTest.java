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

package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventBus;
import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.events.EventSubscriber;
import io.advantageous.qbit.message.Event;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.qbit.events.EventUtils.callbackEventListener;
import static io.advantageous.boon.Boon.puts;

public class EventBusImplTest {

    EventBus eventBus;
    EventBusImpl eventBusImpl;
    String returnValue;
    int subscriberMessageCount;
    int consumerCount;

    boolean ok;


    @Before
    public void setup() {
        eventBusImpl = new EventBusImpl();
        eventBus = eventBusImpl;
        subscriberMessageCount = 0;
        consumerCount = 0;

    }

    @Test
    public void test() {

        String hello = "hello";

        eventBus.register("rick", new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event);
                returnValue = event.body().toString();
                subscriberMessageCount++;
            }
        });

        eventBus.register("rick", new EventSubscriber<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event);
                subscriberMessageCount++;
            }
        });

        eventBus.register("bob", callbackEventListener(o -> {
            puts(o);
            subscriberMessageCount++;
        }));


        eventBus.register("rick", callbackEventListener(o -> {
            puts(o, o.getClass());
            subscriberMessageCount++;
        }));

        eventBus.register("rick", new EventConsumer<Object>() {
            @Override
            public void listen(Event<Object> event) {
                puts(event);
                consumerCount++;
            }
        });

        eventBus.send("rick", hello);


        ok = returnValue == hello || die();
        ok = consumerCount == 1 || die();
        ok = subscriberMessageCount == 3 || die();


    }

}