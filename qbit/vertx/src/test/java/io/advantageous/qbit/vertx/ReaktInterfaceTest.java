package io.advantageous.qbit.vertx;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
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

    private final URI successResult = URI.create("http://localhost:8080/employeeService/");
    private ServiceDiscovery serviceDiscoveryWebSocket;
    private ServiceDiscoveryImpl impl;
    private URI empURI;
    private CountDownLatch latch;
    private AtomicReference<URI> returnValue;
    private AtomicReference<Throwable> errorRef;
    private int port;
    private Client client;
    private ServiceEndpointServer server;

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

        Sys.sleep(2000);

        client = ClientBuilder.clientBuilder().setPort(port).build();




        serviceDiscoveryWebSocket = client.createProxy(ServiceDiscovery.class, "/myservice");

        client.start();
    }

    @After
    public void after() {
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
        testFail(serviceDiscoveryWebSocket);
    }

    private void testFail(ServiceDiscovery serviceDiscovery) {
        serviceDiscovery.lookupService(null).then(this::handleSuccess)
                .catchError(this::handleError).invoke();

        await();
        assertNull("We do not have a return", returnValue.get());
        assertNotNull("There were  errors", errorRef.get());
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
        @SuppressWarnings("unused")
        public void lookupService(final io.advantageous.qbit.reactive.Callback<URI> callback, final URI uri) {
            if (uri == null) {
                callback.reject("uri can't be null");
            } else {
                callback.resolve(successResult);
            }
        }
    }
}
