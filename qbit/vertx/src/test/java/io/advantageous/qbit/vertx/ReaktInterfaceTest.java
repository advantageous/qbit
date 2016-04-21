package io.advantageous.qbit.vertx;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.reakt.promise.Promise;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReaktInterfaceTest {

    final URI successResult = URI.create("http://localhost:8080/employeeService/");

    ServiceDiscovery serviceDiscovery;
    ServiceDiscovery serviceDiscoveryStrongTyped;
    ServiceDiscovery serviceDiscoveryServiceBundle;
    ServiceDiscovery serviceDiscoveryWebSocket;


    ServiceDiscoveryImpl impl;
    URI empURI;
    CountDownLatch latch;
    AtomicReference<URI> returnValue;
    AtomicReference<Throwable> errorRef;

    int port;
    Client client;
    ServiceEndpointServer server;
    ServiceBundle serviceBundle;
    ServiceQueue serviceQueue;
    ServiceQueue serviceQueue2;

    @Before
    public void before() {

        port = PortUtils.findOpenPortStartAt(9000);


        latch = new CountDownLatch(1);
        returnValue = new AtomicReference<>();
        errorRef = new AtomicReference<>();
        impl = new ServiceDiscoveryImpl();
        empURI = URI.create("marathon://default/employeeService?env=staging");


        server = EndpointServerBuilder.endpointServerBuilder()
                .addService("/myservice", impl)
                .setPort(port).build().startServer();

        Sys.sleep(200);

        client = ClientBuilder.clientBuilder().setPort(port).build().startClient();

        serviceQueue = ServiceBuilder.serviceBuilder().setServiceObject(impl).buildAndStartAll();
        serviceBundle = ServiceBundleBuilder.serviceBundleBuilder().build();
        serviceBundle.addServiceObject("myservice", impl);
        serviceQueue2 = ServiceBuilder.serviceBuilder().setInvokeDynamic(false).setServiceObject(impl)
                .buildAndStartAll();


        serviceDiscoveryServiceBundle = serviceBundle.createLocalProxy(ServiceDiscovery.class, "myservice");
        serviceBundle.start();

        serviceDiscovery = serviceQueue.createProxyWithAutoFlush(ServiceDiscovery.class, Duration.TEN_MILLIS);
        serviceDiscoveryStrongTyped = serviceQueue2.createProxyWithAutoFlush(ServiceDiscovery.class,
                Duration.TEN_MILLIS);

        serviceDiscoveryWebSocket = client.createProxy(ServiceDiscovery.class, "/myservice");
    }

    @After
    public void after() {
        serviceQueue2.stop();
        serviceQueue.stop();
        serviceBundle.stop();
        server.stop();
        client.stop();
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
        testSuccess(serviceDiscoveryWebSocket);

    }

    private void testSuccess(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupService(empURI).then(this::handleSuccess)
                .catchError(this::handleError).invoke();
        await();
        assertNotNull("We have a return", returnValue.get());
        assertNull("There were no errors", errorRef.get());
        assertEquals("The result is the expected result", successResult, returnValue.get());
    }


    @Test
    public void testServiceWithReturnPromiseFail() {
        testFail(serviceDiscovery);
        testFail(serviceDiscoveryStrongTyped);
        testFail(serviceDiscoveryServiceBundle);
        testFail(serviceDiscoveryWebSocket);
    }

    private void testFail(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupService(null).then(this::handleSuccess)
                .catchError(this::handleError).invoke();

        await();
        assertNull("We do not have a return", returnValue.get());
        assertNotNull("There were  errors", errorRef.get());
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
    }

    public class ServiceDiscoveryImpl {
        public void lookupService(final io.advantageous.qbit.reactive.Callback<URI> callback, final URI uri) {
            if (uri == null) {
                callback.reject("uri can't be null");
            } else {
                callback.resolve(successResult);
            }
        }
    }
}
