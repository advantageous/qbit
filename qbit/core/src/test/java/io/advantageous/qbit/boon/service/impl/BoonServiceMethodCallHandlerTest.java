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

package io.advantageous.qbit.boon.service.impl;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.Promises;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * created by Richard on 9/26/14.
 */
public class BoonServiceMethodCallHandlerTest {



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


    @Test
    public void testCallMethod() {

        class MyServiceForInvoke {

            boolean called;

            public void method() {
                called = true;
            }

        }
        MyServiceForInvoke myService = new MyServiceForInvoke();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {
        });

        final Response<Object> response = boonServiceMethodCallHandler.receiveMethodCall(MethodCallBuilder.methodCallBuilder().setName("method").build());


        assertFalse(response.wasErrors());
        assertTrue(myService.called);


        boonServiceMethodCallHandler.startBatch();
        assertTrue(myService.called);
    }


    @Test
    public void testCallMethodWithCallback() {

        class MyServiceForInvoke {

            boolean called;

            public void method(Callback<Boolean> callback) {
                called = true;
                callback.resolve(true);
            }

        }
        MyServiceForInvoke myService = new MyServiceForInvoke();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        Promise<Boolean> promise = Promises.blockingPromiseBoolean();
        initHandlerWithPromise(myService, boonServiceMethodCallHandler, promise);

        final MethodCall<Object> method = MethodCallBuilder.methodCallBuilder().setName("method").build();
        final Response<Object> response = boonServiceMethodCallHandler.receiveMethodCall(method);


        assertFalse(response.wasErrors());
        assertTrue(myService.called);
        assertTrue(promise.success());

    }


    @Test
    public void testCallMethodWithPromise() {

        class MyServiceForInvokablePromise {

            boolean called;

            public Promise<Boolean> method() {
                return Promises.invokablePromise(promise -> {
                    called = true;
                    promise.resolve(true);
                });
            }

        }
        MyServiceForInvokablePromise myService = new MyServiceForInvokablePromise();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(true);
        Promise<Boolean> promise = Promises.blockingPromiseBoolean();

        initHandlerWithPromise(myService, boonServiceMethodCallHandler, promise);


        final MethodCall<Object> method = MethodCallBuilder.methodCallBuilder().setName("method").build();
        final Response<Object> response = boonServiceMethodCallHandler.receiveMethodCall(method);


        assertFalse(response.wasErrors());
        assertTrue(myService.called);
        assertTrue(promise.success());


    }


    @Test
    public void testCallMethodWithPromiseNonDynamic() {

        class MyServiceForInvokablePromiseNonDynamic {

            boolean called;

            public Promise<Boolean> method() {
                return Promises.invokablePromise(promise -> {
                    called = true;
                    promise.resolve(true);
                });
            }

        }
        MyServiceForInvokablePromiseNonDynamic myService = new MyServiceForInvokablePromiseNonDynamic();

        final BoonServiceMethodCallHandler boonServiceMethodCallHandler = new BoonServiceMethodCallHandler(false);
        Promise<Boolean> promise = Promises.blockingPromiseBoolean();

        initHandlerWithPromise(myService, boonServiceMethodCallHandler, promise);


        final MethodCall<Object> method = MethodCallBuilder.methodCallBuilder().setName("method").setBody(new Object[]{}).build();
        final Response<Object> response = boonServiceMethodCallHandler.receiveMethodCall(method);


        assertFalse(response.wasErrors());
        assertTrue(myService.called);
        assertTrue(promise.success());


    }

    private void initHandlerWithPromise(Object myService, BoonServiceMethodCallHandler boonServiceMethodCallHandler, final Promise<Boolean> promise) {
        boonServiceMethodCallHandler.init(myService, "", "", new SendQueue<Response<Object>>() {
            @Override
            public boolean send(Response<Object> item) {
                if (item.wasErrors()) {
                    promise.reject((Throwable) item.body());
                } else {
                    promise.resolve(((Boolean) item.body()));
                }
                return true;
            }
        });
    }
}
