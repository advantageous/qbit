package io.advantageous.qbit.reakt;


import io.advantageous.qbit.service.*;
import io.advantageous.qbit.time.Duration;
import io.advantageous.reakt.CallbackHandle;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;
import io.advantageous.reakt.promise.Promises;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class ReaktInterfaceTest {

    final URI successResult = URI.create("http://localhost:8080/employeeService/");

    ServiceDiscovery serviceDiscovery;
    ServiceDiscovery serviceDiscoveryStrongTyped;
    ServiceDiscovery serviceDiscoveryServiceBundle;


    ServiceDiscoveryImpl impl;
    URI empURI;
    CountDownLatch latch;
    AtomicReference<URI> returnValue;
    AtomicReference<Throwable> errorRef;
    ServiceBundle serviceBundle;
    ServiceQueue serviceQueue;
    ServiceQueue serviceQueue2;

    @Before
    public void before() {


        latch = new CountDownLatch(1);
        returnValue = new AtomicReference<>();
        errorRef = new AtomicReference<>();
        impl = new ServiceDiscoveryImpl();
        empURI = URI.create("marathon://default/employeeService?env=staging");


        ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.getRequestQueueBuilder().setArrayBlockingQueue().setBatchSize(10);

        serviceQueue = serviceBuilder.setServiceObject(impl).buildAndStartAll();

        ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();
        serviceBundleBuilder.getRequestQueueBuilder().setArrayBlockingQueue().setBatchSize(10);
        serviceBundle = serviceBundleBuilder.build();
        serviceBundle.addServiceObject("myservice", impl);
        serviceQueue2 = ServiceBuilder.serviceBuilder().setInvokeDynamic(false).setServiceObject(impl)
                .buildAndStartAll();


        serviceDiscoveryServiceBundle = serviceBundle.createLocalProxy(ServiceDiscovery.class, "myservice");
        serviceBundle.start();

        serviceDiscovery = serviceQueue.createProxyWithAutoFlush(ServiceDiscovery.class, Duration.TEN_MILLIS);
        serviceDiscoveryStrongTyped = serviceQueue2.createProxyWithAutoFlush(ServiceDiscovery.class,
                Duration.TEN_MILLIS);

    }

    @After
    public void after() {
        serviceQueue2.stop();
        serviceQueue.stop();
        serviceBundle.stop();

    }

    public void await() {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testServiceWithReturnPromiseSuccess() {
        testSuccess(serviceDiscovery);
        testSuccess(serviceDiscoveryStrongTyped);
        testSuccess(serviceDiscoveryServiceBundle);

    }

    @Test
    public void testOk() {
        testOk(serviceDiscovery);
        testOk(serviceDiscoveryStrongTyped);
        testOk(serviceDiscoveryServiceBundle);

    }


    @Test
    public void test5() {
        test5(serviceDiscovery);
        test5(serviceDiscoveryStrongTyped);
        test5(serviceDiscoveryServiceBundle);

    }

    private void testSuccess(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupService(empURI).then(this::handleSuccess)
                .catchError(this::handleError).invoke();
        await();
        assertNotNull("We have a return from local", returnValue.get());
        assertNull("There were no errors from local ", errorRef.get());
        assertEquals("The result is the expected result from local", successResult, returnValue.get());
    }

    private void testOk(ServiceDiscovery serviceDiscovery) {

        final Promise<Boolean> promise = Promises.blockingPromiseBoolean();
        serviceDiscovery.ok().invokeWithPromise(promise);

        ServiceProxyUtils.flushServiceProxy(serviceDiscovery);

        assertTrue(promise.success());
        assertTrue(promise.get());


    }


    private void test5(ServiceDiscovery serviceDiscovery) {

        final Promise<Integer> promise = Promises.blockingPromiseInt();
        serviceDiscovery.five().invokeWithPromise(promise);

        ServiceProxyUtils.flushServiceProxy(serviceDiscovery);

        assertTrue(promise.success());
        assertEquals(new Integer(5), promise.get());


    }

    @Test
    public void testServiceWithReturnPromiseFail() {
        testFail(serviceDiscovery);
        testFail(serviceDiscoveryStrongTyped);
        testFail(serviceDiscoveryServiceBundle);
    }

    private void testFail(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupService(null).then(this::handleSuccess)
                .catchError(this::handleError).invoke();

        await();
        assertNull("We do not have a return", returnValue.get());
        assertNotNull("There were  errors", errorRef.get());
    }


    @Test
    public void testServiceWithReturnPromiseHandleFail() {
        testFailHandle(serviceDiscovery);
        testFailHandle(serviceDiscoveryStrongTyped);
        testFailHandle(serviceDiscoveryServiceBundle);
    }

    private void testFailHandle(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupServiceByPromiseHandle(null).then(this::handleSuccess)
                .catchError(this::handleError).invoke();

        await();
        assertNull("We do not have a return", returnValue.get());
        assertNotNull("There were  errors", errorRef.get());
    }


    @Test
    public void testServiceWithReturnPromiseHandleSuccess() {
        testSuccessHandle(serviceDiscovery);
        testSuccessHandle(serviceDiscoveryStrongTyped);
        testSuccessHandle(serviceDiscoveryServiceBundle);
    }

    private void testSuccessHandle(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupServiceByPromiseHandle(URI.create("http://localhost/foo")).then(this::handleSuccess)
                .catchError(this::handleError).invoke();

        await();
        assertNotNull("We do not have a return", returnValue.get());
        assertNull("There were  errors", errorRef.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testServiceWithReturnPromiseSuccessInvokeTwice() {
        final Promise<URI> promise = serviceDiscovery.lookupService(empURI).then(this::handleSuccess)
                .catchError(this::handleError);
        promise.invoke();
        promise.invoke();
    }

    @Test
    public void testIsInvokable() {
        final Promise<URI> promise = serviceDiscovery.lookupService(empURI).then(this::handleSuccess)
                .catchError(this::handleError);

        assertTrue("Is this an invokable promise", promise.isInvokable());
    }


    private void handleError(Throwable error) {
        errorRef.set(error);
        latch.countDown();
    }

    private void handleSuccess(URI uri) {
        returnValue.set(uri);
        latch.countDown();
    }


    interface ServiceDiscovery {
        Promise<URI> lookupService(URI uri);

        Promise<Boolean> ok();

        Promise<Integer> five();


        PromiseHandle<URI> lookupServiceByPromiseHandle(final URI uri);
    }

    public class ServiceDiscoveryImpl {
        @SuppressWarnings("unused")
        public void lookupService(final io.advantageous.qbit.reactive.Callback<URI> callback, final URI uri) {
            if (uri == null) {
                callback.reject("uri can't be null");
            } else {
                callback.resolve(successResult);
            }
        }

        public PromiseHandle<URI> lookupServiceByPromiseHandle(final URI uri) {
            return Promises.deferCall(callback -> {
                    if (uri == null) {
                        callback.reject("uri can't be null");
                    } else {
                        callback.resolve(successResult);
                    }
                });
        }

        public void ok(final io.advantageous.qbit.reactive.Callback<Boolean> callback) {
            callback.resolve(true);

        }


        public void five(final io.advantageous.qbit.reactive.Callback<Integer> callback) {
            callback.resolve(5);

        }
    }
}
