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

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.boon.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.SendQueue;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * created by Richard on 9/26/14.
 */
public class BoonServiceMethodCallHandlerTest {


    boolean methodCalled;
    boolean ok;

    @Test
    public void testInit() {

        class MyService {

            boolean called;

            @QueueCallback(QueueCallbackType.INIT)
            private void init() {


                called = true;
            }

        }


        MyService myService = new MyService();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {

        });

        boonServiceMethodCallHandler.limit();
        boonServiceMethodCallHandler.empty();
        boonServiceMethodCallHandler.startBatch();
        boonServiceMethodCallHandler.shutdown();
        boonServiceMethodCallHandler.idle();
        assertFalse(myService.called);


        boonServiceMethodCallHandler.init();
        assertTrue(myService.called);
    }

    @Test
    public void testLimit() {

        class MyService {

            boolean called;

            @QueueCallback(QueueCallbackType.LIMIT)
            private void limit() {


                called = true;
            }

        }

        MyService myService = new MyService();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {

        });

        boonServiceMethodCallHandler.init();
        boonServiceMethodCallHandler.empty();
        boonServiceMethodCallHandler.startBatch();
        boonServiceMethodCallHandler.shutdown();
        boonServiceMethodCallHandler.idle();
        assertFalse(myService.called);


        boonServiceMethodCallHandler.limit();
        assertTrue(myService.called);
    }

    @Test
    public void testIdle() {

        class MyService {

            boolean called;

            @QueueCallback(QueueCallbackType.IDLE)
            private void idle() {


                called = true;
            }

        }

        MyService myService = new MyService();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {

        });

        boonServiceMethodCallHandler.init();
        boonServiceMethodCallHandler.limit();
        boonServiceMethodCallHandler.empty();
        boonServiceMethodCallHandler.startBatch();
        boonServiceMethodCallHandler.shutdown();
        assertFalse(myService.called);


        boonServiceMethodCallHandler.idle();
        assertTrue(myService.called);
    }

    @Test
    public void testShutdown() {

        class MyService {

            boolean called;

            @QueueCallback(QueueCallbackType.SHUTDOWN)
            private void shutdown() {


                called = true;
            }

        }

        MyService myService = new MyService();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {

        });

        boonServiceMethodCallHandler.init();
        boonServiceMethodCallHandler.limit();
        boonServiceMethodCallHandler.empty();
        boonServiceMethodCallHandler.idle();
        boonServiceMethodCallHandler.startBatch();
        assertFalse(myService.called);


        boonServiceMethodCallHandler.shutdown();
        assertTrue(myService.called);
    }

    @Test
    public void testEmpty() {

        class MyService {

            boolean called;

            @QueueCallback(QueueCallbackType.EMPTY)
            private void method() {


                called = true;
            }

        }

        MyService myService = new MyService();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {

        });

        boonServiceMethodCallHandler.init();
        boonServiceMethodCallHandler.limit();
        boonServiceMethodCallHandler.shutdown();
        boonServiceMethodCallHandler.idle();
        boonServiceMethodCallHandler.startBatch();
        assertFalse(myService.called);


        boonServiceMethodCallHandler.empty();
        assertTrue(myService.called);
    }

    @Test
    public void testStartBatch() {

        class MyService {

            boolean called;

            @QueueCallback(QueueCallbackType.START_BATCH)
            private void method() {


                called = true;
            }

        }
        MyService myService = new MyService();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {

        });

        boonServiceMethodCallHandler.init();
        boonServiceMethodCallHandler.limit();
        boonServiceMethodCallHandler.empty();
        boonServiceMethodCallHandler.shutdown();
        boonServiceMethodCallHandler.idle();
        assertFalse(myService.called);


        boonServiceMethodCallHandler.startBatch();
        assertTrue(myService.called);
    }

    @RequestMapping("/boo/baz")
    class Foo {

        @RequestMapping("/baaah/pluck")
        public void foo() {

            methodCalled = true;
            puts("foo");
        }


        @RequestMapping("/geoff/chandles/twoargs/{0}/{1}/")
        public void geoff(String a, int b) {

            methodCalled = true;
            puts("geoff a", a, "b", b);
        }

        @RequestMapping("/geoff/chandles/")
        public void someMethod(String a, int b) {

            methodCalled = true;
            puts("geoff");
        }


        public void someMethod2(String a, int b) {

            methodCalled = true;
            puts("geoff", a, b);
        }


        public void someMethod3() {

            methodCalled = true;
        }
    }

}
