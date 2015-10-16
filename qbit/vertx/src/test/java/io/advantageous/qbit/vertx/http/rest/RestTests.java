package io.advantageous.qbit.vertx.http.rest;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.http.request.HttpResponseBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.util.PortUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestTests {



    public static AtomicReference<List<DomainClass>> ref = new AtomicReference<>();

    private int openPort;
    private ServiceEndpointServer serviceEndpointServer;

    public static class DomainClass {
        int i;
        String s;

        public DomainClass(int i, String s) {
            this.i = i;
            this.s = s;
        }

        @Override
        public String toString() {
            return "DomainClass{" +
                    "i=" + i +
                    ", s='" + s + '\'' +
                    '}';
        }
    }


    @RequestMapping
    public static class TestService {


        @RequestMapping(method = RequestMethod.POST)
        public void addAll(List<DomainClass> domains) {
            ref.set(domains);
        }


        @RequestMapping(method = RequestMethod.GET)
        public void ping(Callback<String> callback) {

            callback.returnThis("love rocket");
        }


        @RequestMapping(method = RequestMethod.GET)
        public void ping2(Callback<HttpTextResponse> callback) {

            callback.returnThis(HttpResponseBuilder.httpResponseBuilder()
                    .setBody("hello mom")
                    .setCode(777)
                    .buildTextResponse());
        }


        @RequestMapping(method = RequestMethod.PUT)
        public void ping3(Callback<HttpTextResponse> callback, String foo) {

            callback.returnThis(HttpResponseBuilder.httpResponseBuilder()
                    .setBody("hello mom " + foo)
                    .setCode(777)
                    .buildTextResponse());
        }
    }



    @Test
    public void testPing() {
        HTTP.Response response = HTTP.getResponse(buildURL("ping"));
        assertEquals(200, response.status());
        assertEquals("\"love rocket\"", response.body());
    }


    @Test
    public void testPing2() {
        HTTP.Response response = HTTP.getResponse(buildURL("ping2"));
        assertEquals(777, response.status());
        assertEquals("hello mom", response.body());
    }

    @Test
    public void testPing3() {
        HTTP.Response response = HTTP.jsonRestCallViaPUT(buildURL("ping3"), "\"foo\"");
        assertEquals(777, response.status());
        assertEquals("hello mom foo", response.body());
    }


    @Test
    public void test() {



        HTTP.Response response = HTTP.jsonRestCallViaPOST(buildURL("addall"),
                "[{\"i\": 1, \"s\": \"string\"}, " +
                "{\"i\": 2, \"s\": \"string2\"}]");

        assertEquals(202, response.status());

        while (ref.get() == null) {
            Sys.sleep(10);
        }

        assertNotNull(ref.get());




    }



    @Before
    public void before() {
        openPort = PortUtils.findOpenPort();
        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder().setPort(openPort).build();
        serviceEndpointServer.initServices(new TestService());
        serviceEndpointServer.start();
        Sys.sleep(1000);

    }

    @After
    public void after() {

        serviceEndpointServer.stop();
    }



    private String buildURL(String ping) {
        return "http://localhost:" + openPort +
                "/services/testservice/" + ping;
    }
}
