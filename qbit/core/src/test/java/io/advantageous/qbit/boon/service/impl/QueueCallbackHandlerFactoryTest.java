package io.advantageous.qbit.boon.service.impl;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QueueCallbackHandlerFactoryTest {


    @Test
    public void testInit() {

        class MyService {

            boolean initCalled;

            @QueueCallback(QueueCallbackType.INIT)
            private void init() {


                initCalled = true;
            }

        }
        MyService myService = new MyService();
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();

        assertTrue(myService.initCalled);
    }

    @Test
    public void testQueueInitDynamic() {

        class MyService {

            boolean initCalled;

            @QueueCallback()
            private void queueInit() {


                initCalled = true;
            }

        }
        MyService myService = new MyService();
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();

        assertTrue(myService.initCalled);
    }


    @Test
    public void testLimit() {

        class MyService {

            boolean limitCalled;

            @QueueCallback(QueueCallbackType.LIMIT)
            private void limit() {


                limitCalled = true;
            }

        }
        MyService myService = new MyService();
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();
        queueCallbackHandler.queueEmpty();
        queueCallbackHandler.queueIdle();
        assertFalse(myService.limitCalled);


        queueCallbackHandler.queueLimit();

        assertTrue(myService.limitCalled);
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
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();
        queueCallbackHandler.queueEmpty();
        queueCallbackHandler.queueLimit();
        assertFalse(myService.called);


        queueCallbackHandler.queueIdle();

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
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();
        queueCallbackHandler.queueEmpty();
        queueCallbackHandler.queueLimit();

        queueCallbackHandler.queueIdle();
        assertFalse(myService.called);


        queueCallbackHandler.queueShutdown();

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
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();
        queueCallbackHandler.queueLimit();

        queueCallbackHandler.queueShutdown();
        queueCallbackHandler.queueIdle();
        assertFalse(myService.called);


        queueCallbackHandler.queueEmpty();

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
        final QueueCallBackHandler queueCallbackHandler = QueueCallbackHandlerFactory.createQueueCallbackHandler(myService);

        queueCallbackHandler.queueInit();
        queueCallbackHandler.queueLimit();

        queueCallbackHandler.queueEmpty();
        queueCallbackHandler.queueShutdown();
        queueCallbackHandler.queueIdle();
        assertFalse(myService.called);


        queueCallbackHandler.queueStartBatch();
        assertTrue(myService.called);
    }
}