package io.advantageous.qbit.spring.rest;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotUsingSpring {


    @Test
    //Ignore
    public void test() {

        final int port = PortUtils.findOpenPort();
        EndpointServerBuilder.endpointServerBuilder()
                .addService(new HelloWorldImpl()).setPort(port).build().startServer();

        Sys.sleep(1_000);

        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setPort(port).buildAndStart();


        final HttpTextResponse httpTextResponse = httpClient.get("/services/hw/hello/");

        assertEquals("\"hello\"", httpTextResponse.body());
        assertEquals(200, httpTextResponse.code());
        assertEquals("application/json", httpTextResponse.contentType());

    }


    @Test
    //@Ignore
    public void usingServiceInjectedIntoEndPointLikeSpring() {

        final int port = PortUtils.findOpenPort();
        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder
                .endpointServerBuilder()
                .setPort(port).build();

        final Queue<Response<Object>> responses = serviceEndpointServer.serviceBundle().responses();

        final ServiceBuilder serviceBuilder = ServiceBuilder
                .serviceBuilder().setResponseQueue(responses)
                .setServiceObject(new HelloWorldImpl());

        final ServiceQueue serviceQueue = serviceBuilder.build();
        serviceEndpointServer.addServiceQueue("helloworldimpl", serviceQueue);

        serviceQueue.startServiceQueue();


        serviceEndpointServer.startServer();
        Sys.sleep(1_000);

        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setPort(port).buildAndStart();


        final HttpTextResponse httpTextResponse = httpClient.get("/services/hw/hello/");

        assertEquals("\"hello\"", httpTextResponse.body());
        assertEquals(200, httpTextResponse.code());
        assertEquals("application/json", httpTextResponse.contentType());


        final HelloWorld helloworld = serviceEndpointServer.serviceBundle().createLocalProxy(HelloWorld.class, "helloworld");

        for (int index = 0; index < 5; index++) {
            helloworld.hello(s -> System.out.println(s));
        }


    }
}
