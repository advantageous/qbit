package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.EmployeeServiceCollectionTestService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PredicateChainTest {



    ServiceEndpointServer serviceEndpointServer;
    HttpServerSimulator httpServerSimulator;

    @Before
    public void before() {
        httpServerSimulator = new HttpServerSimulator();

        FactorySPI.setHttpServerFactory((options, endPointName, systemManager, serviceDiscovery,
                                         healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit, a, b)
                -> httpServerSimulator);


        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true)
                .build()
                .initServices(new EmployeeServiceCollectionTestService()).startServer();
    }


    @Test
    public void testChain() {


        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true);

        HttpServerBuilder httpServerBuilder = endpointServerBuilder.getHttpServerBuilder();

        httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> false);

        serviceEndpointServer  = endpointServerBuilder.addService(new EmployeeServiceCollectionTestService()).build();

        serviceEndpointServer.startServer();


        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));


        assertNull(httpResponse);
    }



    @Test
    public void testChainWithResponse() {


        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true);

        HttpServerBuilder httpServerBuilder = endpointServerBuilder.getHttpServerBuilder();

        httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> {
            httpRequest.getReceiver().response(666, "foo-content", "foo");
            return false;
        });

        serviceEndpointServer  = endpointServerBuilder.addService(new EmployeeServiceCollectionTestService()).build();

        serviceEndpointServer.startServer();


        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));


        assertEquals(666, httpResponse.code());
        assertEquals("foo", httpResponse.body());
    }


    @Test
    public void testChainContinueTrue() {


        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true);

        HttpServerBuilder httpServerBuilder = endpointServerBuilder.getHttpServerBuilder();

        httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> true);

        serviceEndpointServer  = endpointServerBuilder.addService(new EmployeeServiceCollectionTestService()).build();

        serviceEndpointServer.startServer();


        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));


        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());
    }


    @Test
    public void testChainLastContinueFalse() {


        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true);

        AtomicInteger count = new AtomicInteger();

        HttpServerBuilder httpServerBuilder = endpointServerBuilder.getHttpServerBuilder();

        for (int i=0; i < 10; i++) {
            httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> {
                count.incrementAndGet();
                return true;
            });
        }

        serviceEndpointServer  = endpointServerBuilder.addService(new EmployeeServiceCollectionTestService()).build();

        serviceEndpointServer.startServer();


        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));


        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());


        assertEquals(10, count.get());
    }

    @Test
    public void testChainContinueTrueWithChain() {


        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true);

        AtomicInteger count = new AtomicInteger();

        HttpServerBuilder httpServerBuilder = endpointServerBuilder.getHttpServerBuilder();

        for (int i=0; i < 10; i++) {
            httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> {
                count.incrementAndGet();
                return true;
            });
        }

        httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> {
            count.incrementAndGet();
            httpRequest.getReceiver().response(666, "foo-content", "foo");
            return false;
        });


        for (int i=0; i < 10; i++) {
            httpServerBuilder.addShouldContinueHttpRequestPredicate(httpRequest -> {
                count.incrementAndGet();
                return true;
            });
        }

        serviceEndpointServer  = endpointServerBuilder.addService(new EmployeeServiceCollectionTestService()).build();

        serviceEndpointServer.startServer();


        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));


        assertEquals(666, httpResponse.code());
        assertEquals("foo", httpResponse.body());

        assertEquals(11, count.get());
    }

}
