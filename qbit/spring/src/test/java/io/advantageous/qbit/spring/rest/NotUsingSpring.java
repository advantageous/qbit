package io.advantageous.qbit.spring.rest;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotUsingSpring {


    @Test
    public void test() {

        final int port = PortUtils.findOpenPort();
        EndpointServerBuilder.endpointServerBuilder()
                .addService(new HelloWorld()).setPort(port).build().startServer();

        Sys.sleep(1_000);

        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setPort(port).buildAndStart();


        final HttpTextResponse httpTextResponse = httpClient.get("/services/hw/hello/");

        assertEquals("\"hello\"", httpTextResponse.body());
        assertEquals(200, httpTextResponse.code());
        assertEquals("application/json", httpTextResponse.contentType());

    }


    @Test
    @Ignore
    public void usingServiceInjectedIntoEndPointLikeSpring() {

        final int port = PortUtils.findOpenPort();
        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(port).build().startServer();

        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder().setServiceObject(new HelloWorld());

        serviceEndpointServer.addServiceQueue("/services/hw", serviceBuilder.build());
        Sys.sleep(1_000);

        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setPort(port).buildAndStart();


        final HttpTextResponse httpTextResponse = httpClient.get("/services/hw/hello/");

        assertEquals("\"hello\"", httpTextResponse.body());
        assertEquals(200, httpTextResponse.code());
        assertEquals("application/json", httpTextResponse.contentType());

    }
}
