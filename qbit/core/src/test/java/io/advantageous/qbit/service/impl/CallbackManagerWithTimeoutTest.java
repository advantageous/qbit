package io.advantageous.qbit.service.impl;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.ResponseBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.util.TestTimer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CallbackManagerWithTimeoutTest {

    private static CountDownLatch continueMethod;
    private CallbackManagerWithTimeout callbackManagerWithTimeout;
    private CallbackManager callbackManager;
    private TestTimer testTimer;
    private MethodCallBuilder methodCallBuilder;
    private CallbackBuilder callbackBuilder;
    private AtomicReference<Object> result;


    @Before
    public void setUp() throws Exception {

        testTimer = new TestTimer();
        testTimer.setTime();
        callbackManager = CallbackManagerBuilder.callbackManagerBuilder()
                .setTimer(testTimer).setName("testBuilder").build();
        callbackManagerWithTimeout = ((CallbackManagerWithTimeout) callbackManager);
        methodCallBuilder = MethodCallBuilder.methodCallBuilder();
        callbackBuilder = CallbackBuilder.newCallbackBuilder();
        result = new AtomicReference<>("none");

        continueMethod = new CountDownLatch(1);

    }


    @Test
    public void testWithMethodCallAndResponseObject() {

        methodCallBuilder.setAddress("/hello/world");
        methodCallBuilder.setName("foo");
        methodCallBuilder.setTimestamp(testTimer.now());
        callbackBuilder.setCallback(Object.class, result::set);
        methodCallBuilder.setBody(
                Lists.list(callbackBuilder.build()));

        final MethodCall<Object> methodCall = methodCallBuilder.build();
        callbackManager.registerCallbacks(methodCall);

        assertEquals(1, callbackManagerWithTimeout.outstandingCallbacksCount());


        callbackManager.handleResponse(
                ResponseBuilder.fromMethodCall(methodCall, "GOT IT"));

        assertEquals("GOT IT", result.get());

        assertEquals(0, callbackManagerWithTimeout.outstandingCallbacksCount());


    }


    @Test
    public void testDefaultTimeout() {

        final int callCount = 40_000;

        for (int index = 0; index < callCount; index++) {
            methodCallBuilder.setAddress("/hello/world");
            methodCallBuilder.setName("foo");
            methodCallBuilder.setTimestamp(testTimer.now());
            methodCallBuilder.setId(index);
            callbackBuilder.setCallback(Object.class, result::set);
            methodCallBuilder.setBody(
                    Lists.list(callbackBuilder.build()));

            final MethodCall<Object> methodCall = methodCallBuilder.build();
            callbackManager.registerCallbacks(methodCall);
        }

        assertEquals(40_000, callbackManagerWithTimeout.outstandingCallbacksCount());
        callbackManagerWithTimeout.process(0);
        assertEquals(40_000, callbackManagerWithTimeout.outstandingCallbacksCount());
        testTimer.minutes(5);
        callbackManagerWithTimeout.process(0);
        assertEquals(0, callbackManagerWithTimeout.outstandingCallbacksCount());


    }


    @Test
    public void testWithTimeouts() {

        callbackManager = CallbackManagerBuilder.callbackManagerBuilder()
                .setTimeOutMS(30_000).setCheckInterval(5_000).setHandleTimeouts(true)
                .setTimer(testTimer).setName("testBuilder").build();
        callbackManagerWithTimeout = ((CallbackManagerWithTimeout) callbackManager);


        final int callCount = 40_000;

        for (int index = 0; index < callCount; index++) {
            methodCallBuilder.setAddress("/hello/world");
            methodCallBuilder.setName("foo");
            methodCallBuilder.setTimestamp(testTimer.now());
            methodCallBuilder.setId(index);
            callbackBuilder.setCallback(Object.class, result::set);
            methodCallBuilder.setBody(
                    Lists.list(callbackBuilder.build()));

            final MethodCall<Object> methodCall = methodCallBuilder.build();
            callbackManager.registerCallbacks(methodCall);
        }

        assertEquals(40_000, callbackManagerWithTimeout.outstandingCallbacksCount());
        callbackManagerWithTimeout.process(0);
        assertEquals(40_000, callbackManagerWithTimeout.outstandingCallbacksCount());
        testTimer.seconds(1);
        callbackManagerWithTimeout.process(0);
        assertEquals(40_000, callbackManagerWithTimeout.outstandingCallbacksCount());
        testTimer.seconds(30);
        callbackManagerWithTimeout.process(0);
        assertEquals(0, callbackManagerWithTimeout.outstandingCallbacksCount());


    }


    public void testWithServiceQueue() throws Exception {

        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setCallbackManager(callbackManager);

        final ServiceQueue serviceQueue = serviceBuilder.setServiceObject(new MyService()).buildAndStartAll();
        final IMyService myService = serviceQueue.createProxy(IMyService.class);

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        callbackBuilder.setCallback(Object.class, o -> {
            result.set(o);
            countDownLatch.countDown();
        });

        myService.method1(callbackBuilder.build());

        ServiceProxyUtils.flushServiceProxy(myService);

        Sys.sleep(500);
        assertEquals(1, callbackManagerWithTimeout.outstandingCallbacksCount());

        continueMethod.countDown();

        countDownLatch.await(20, TimeUnit.SECONDS);
        assertEquals(0, callbackManagerWithTimeout.outstandingCallbacksCount());

        assertEquals("METHOD 1 RETURN", result.get());

    }

    @Test
    public void testWithServiceBundle() throws Exception {

        final ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();
        serviceBundleBuilder.setCallbackManager(callbackManager);

        final ServiceBundle serviceBundle = serviceBundleBuilder.build();

        serviceBundle.addServiceObject("abc", new MyService());

        final IMyService myService = serviceBundle.createLocalProxy(IMyService.class, "abc");
        serviceBundle.start();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        callbackBuilder.setCallback(Object.class, o -> {
            result.set(o);
            countDownLatch.countDown();
        });

        myService.method1(callbackBuilder.build());

        ServiceProxyUtils.flushServiceProxy(myService);

        Sys.sleep(500);
        assertEquals(1, callbackManagerWithTimeout.outstandingCallbacksCount());

        continueMethod.countDown();

        countDownLatch.await(20, TimeUnit.SECONDS);
        assertEquals(0, callbackManagerWithTimeout.outstandingCallbacksCount());

        assertEquals("METHOD 1 RETURN", result.get());

    }

    @Test
    public void testWithServiceBundleManyMethods() throws Exception {

        final ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();
        serviceBundleBuilder.setCallbackManager(callbackManager);

        final ServiceBundle serviceBundle = serviceBundleBuilder.build();

        serviceBundle.addServiceObject("abc", new MyService());

        final IMyService myService = serviceBundle.createLocalProxy(IMyService.class, "abc");
        serviceBundle.start();

        final int callCount = 1_000_000;
        final CountDownLatch countDownLatch = new CountDownLatch(callCount);
        callbackBuilder.setCallback(Object.class, o -> {
            result.set(o);
            countDownLatch.countDown();
        });

        for (int index = 0; index < callCount; index++) {
            myService.method1(callbackBuilder.build());
        }

        ServiceProxyUtils.flushServiceProxy(myService);

        Sys.sleep(500);
        assertTrue(callbackManagerWithTimeout.outstandingCallbacksCount() > 1);

        continueMethod.countDown();

        countDownLatch.await(200, TimeUnit.SECONDS);
        assertEquals(0, callbackManagerWithTimeout.outstandingCallbacksCount());

        assertEquals("METHOD 1 RETURN", result.get());

    }


    public interface IMyService {
        void method1(Callback<String> callback);
    }

    public static class MyService {

        public String method1() throws Exception {
            continueMethod.await();
            return "METHOD 1 RETURN";
        }
    }
}