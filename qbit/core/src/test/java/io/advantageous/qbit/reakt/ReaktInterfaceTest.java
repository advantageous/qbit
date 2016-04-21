package io.advantageous.qbit.reakt;


import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.time.Duration;
import io.advantageous.reakt.Callback;
import io.advantageous.reakt.promise.Promise;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReaktInterfaceTest {

    final URI successResult = URI.create("http://localhost:8080/employeeService/");
    ServiceDiscovery serviceDiscovery;
    ServiceDiscoveryImpl impl;
    URI empURI;
    CountDownLatch latch;
    AtomicReference<URI> returnValue;
    AtomicReference<Throwable> errorRef;
    ServiceQueue serviceQueue;

    @Before
    public void before() {
        latch = new CountDownLatch(1);
        returnValue = new AtomicReference<>();
        errorRef = new AtomicReference<>();
        impl = new ServiceDiscoveryImpl();
        empURI = URI.create("marathon://default/employeeService?env=staging");
        serviceQueue = ServiceBuilder.serviceBuilder().setServiceObject(impl).buildAndStartAll();
        serviceDiscovery = serviceQueue.createProxyWithAutoFlush(ServiceDiscovery.class, Duration.TEN_MILLIS);
    }

    @After
    public void after() {
        serviceQueue.stop();
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
        serviceDiscovery.lookupService(empURI).then(this::handleSuccess)
                .catchError(this::handleError).invoke();
        await();
        assertNotNull("We have a return", returnValue.get());
        assertNull("There were no errors", errorRef.get());
        assertEquals("The result is the expected result", successResult, returnValue.get());
    }


    @Test
    public void testServiceWithReturnPromiseFail() {


        serviceDiscovery.lookupService(null).then(this::handleSuccess)
                .catchError(this::handleError).invoke();

        await();
        assertNull("We do not have a return", returnValue.get());
        assertNotNull("There were  errors", errorRef.get());
    }


    @Test (expected = IllegalStateException.class)
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
    }

    public class ServiceDiscoveryImpl  {
        public void lookupService(final io.advantageous.qbit.reactive.Callback<URI> callback, final URI uri) {
            if (uri == null) {
                callback.reject("uri can't be null");
            } else {
                callback.resolve(successResult);
            }
        }
    }
}
