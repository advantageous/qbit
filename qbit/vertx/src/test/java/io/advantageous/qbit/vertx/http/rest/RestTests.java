package io.advantageous.qbit.vertx.http.rest;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.http.NoCacheHeaders;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.http.HttpContext;
import io.advantageous.qbit.http.HttpHeaders;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.util.PortUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.advantageous.boon.core.IO.puts;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestTests {


    public static AtomicReference<List<DomainClass>> ref = new AtomicReference<>();

    private int openPort;
    private ServiceEndpointServer serviceEndpointServer;

    @Test
    public void testPing() {
        HTTP.Response response = HTTP.getResponse(buildURL("ping"));
        assertEquals(200, response.status());


        final List<String> controls = response.headers().get(HttpHeaders.CACHE_CONTROL);

        Assert.assertEquals("no-cache, no-store", controls.get(0));

        Assert.assertEquals("max-age=0", controls.get(1));

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

    @Test
    public void testBadJSON() {


        HTTP.Response response = HTTP.jsonRestCallViaPOST(buildURL("addall"),
                "\"i\": 1, \"s\": \"string\"}, " +
                        "{\"i\": 2, \"s\": \"string2\"}]");

        assertEquals(400, response.status());


    }

    @Test
    public void testBadJSONWithReturn() {


        serviceEndpointServer.stop();

        openPort = PortUtils.findOpenPort();
        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(openPort)
                .setErrorHandler(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        final Optional<HttpRequest> httpRequest = new HttpContext().getHttpRequest();
                        if (httpRequest.isPresent()) {
                            httpRequest.get().getReceiver().respondOK("\"Bad JSON" + throwable.getMessage() + "\"");
                            httpRequest.get().handled();
                        }
                    }
                })
                .build();
        serviceEndpointServer.initServices(new TestService());
        serviceEndpointServer.startServerAndWait();


        HTTP.Response response = HTTP.jsonRestCallViaPOST(buildURL("addall2"),
                "\"i\": 1, \"s\": \"string\"}, " +
                        "{\"i\": 2, \"s\": \"string2\"}]");

        puts(response);

        assertEquals(200, response.status());


    }

    @Test
    public void testBadJSONCustomHandler() {

        serviceEndpointServer.stop();

        openPort = PortUtils.findOpenPort();
        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(openPort)
                .setErrorHandler(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        final Optional<HttpRequest> httpRequest = new HttpContext().getHttpRequest();
                        if (httpRequest.isPresent()) {
                            httpRequest.get().getReceiver().respondOK("\"Bad JSON" + throwable.getMessage() + "\"");
                            httpRequest.get().handled();
                        }
                    }
                })
                .build();
        serviceEndpointServer.initServices(new TestService());
        serviceEndpointServer.startServerAndWait();


        HTTP.Response response = HTTP.jsonRestCallViaPOST(buildURL("addall"),
                "\"i\": 1, \"s\": \"string\"}, " +
                        "{\"i\": 2, \"s\": \"string2\"}]");

        assertEquals(200, response.status());


    }

    @Before
    public void before() {
        openPort = PortUtils.findOpenPort();
        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder().setPort(openPort).build();
        serviceEndpointServer.initServices(new TestService());
        serviceEndpointServer.startServerAndWait();
    }

    @After
    public void after() {

        serviceEndpointServer.stop();
    }

    private String buildURL(String ping) {
        return "http://localhost:" + openPort +
                "/services/testservice/" + ping;
    }

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


        @RequestMapping(method = RequestMethod.POST)
        public boolean addAll2(List<DomainClass> domains) {
            ref.set(domains);
            return true;
        }


        @RequestMapping(method = RequestMethod.GET)
        @NoCacheHeaders
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
}
