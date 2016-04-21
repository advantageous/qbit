package io.advantageous.qbit.boon.service.impl;

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.reakt.Callback;
import io.advantageous.reakt.Invokable;
import io.advantageous.reakt.promise.Promise;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BoonInvocationHandlerForSendQueueTest {

    BoonInvocationHandlerForSendQueue boonInvocationHandlerForSendQueue;
    SendQueue<MethodCall<Object>> sendQueue;
    Class<?> serviceInterface;
    String serviceName;
    BeforeMethodSent beforeMethodSent;
    Method methodFoo;
    Method methodFooPromise;

    @Before
    public void setup() throws Exception {
        sendQueue = mock(SendQueue.class);
        serviceInterface = FooService.class;
        serviceName = "foo";
        beforeMethodSent = mock(BeforeMethodSent.class);
        methodFoo = FooService.class.getMethod("foo", Callback.class, int.class, int.class);
        methodFooPromise = FooService.class.getMethod("fooPromise", int.class, int.class);

        boonInvocationHandlerForSendQueue = new BoonInvocationHandlerForSendQueue(sendQueue, serviceInterface,
                serviceName, beforeMethodSent);
    }

    @Test
    public void testInvoke() throws Exception {

        try {
            boonInvocationHandlerForSendQueue.invoke(new Object(), methodFoo, new Object[]{1, 2});
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }

        verify(sendQueue, times(1)).send(any());
    }

    @Test
    public void testInvokeWithPromise() throws Exception {


        final Object returnObject;
        try {
            returnObject = boonInvocationHandlerForSendQueue.invoke(new Object(),
                    methodFooPromise, new Object[]{1, 2});
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }

        /* Nothing should be called yet. */
        verify(sendQueue, never()).send(any());

        /* return Object is a promise. */
        assertTrue(returnObject instanceof Promise);


        /* return Object is a invokable promise. */
        assertTrue(returnObject instanceof Invokable);

        final Promise<String> promise = (Promise<String>) returnObject;

        verify(sendQueue, never()).send(any());
        promise.invoke();
        verify(sendQueue, times(1)).send(any());

    }


    public interface FooService {
        void foo(Callback<String> callback, int i, int ii);

        Promise<String> fooPromise(int i, int ii);
    }
}