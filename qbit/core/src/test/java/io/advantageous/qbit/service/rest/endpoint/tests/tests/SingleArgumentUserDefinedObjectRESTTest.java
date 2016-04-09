package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.HttpHeaders;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.EmployeeServiceSingleObjectTestService;
import io.advantageous.qbit.service.rest.endpoint.tests.services.MyService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.advantageous.boon.core.IO.puts;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SingleArgumentUserDefinedObjectRESTTest {


    ServiceEndpointServer serviceEndpointServer;

    HttpServerSimulator httpServerSimulator;
    HttpRequestBuilder httpRequestBuilder;

    EmployeeServiceSingleObjectTestService service;


    @Before
    public void before() {
        httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();

        httpServerSimulator = new HttpServerSimulator();

        service = new EmployeeServiceSingleObjectTestService();

        FactorySPI.setHttpServerFactory((options, endPointName, systemManager, serviceDiscovery,
                                         healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit, a, b, c)
                -> httpServerSimulator);

        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .build()
                .initServices(
                        service,
                        new MyService()
                ).startServer();
    }

    @Test
    public void testRootMap() {


        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder().setUri("/")
                .build()
                .initServices(
                        new MyService()
                ).startServer();

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequestRaw(
                httpRequestBuilder.setUri("/ping")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }

    @Test
    public void testDefaultRequestParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-request-param")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("\"foo\"", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-request-param")
                        .addParam("p", "something")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("\"something\"", httpResponse2.body());

    }


    @Test
    public void testDefaultRequestParamNoDefault() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-request-param-no-default")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-request-param-no-default")
                        .addParam("p", "something")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("\"something\"", httpResponse2.body());

    }


    @Test
    public void testDefaultBooleanRequestParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/boolean-request-param")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/boolean-request-param")
                        .addParam("p", "false")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("false", httpResponse2.body());

    }


    @Test
    public void testDefaultBooleanRequestParamNoDefault() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/boolean-request-param-no-default")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("false", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/boolean-request-param-no-default")
                        .addParam("p", "true")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("true", httpResponse2.body());

    }


    @Test
    public void testDefaultIntRequestParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/int-request-param")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("99", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/int-request-param")
                        .addParam("p", "100")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("100", httpResponse2.body());

    }


    @Test
    public void testDefaultIntRequestParamNoDefault() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/int-request-param-no-default")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("0", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/int-request-param-no-default")
                        .addParam("p", "66")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("66", httpResponse2.body());

    }


    @Test
    public void testDefaultIntegerRequestParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/integer-request-param")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("99", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/integer-request-param")
                        .addParam("p", "100")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("100", httpResponse2.body());

    }


    @Test
    public void testDefaultIntegerRequestParamNoDefault() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/integer-request-param-no-default")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/integer-request-param-no-default")
                        .addParam("p", "66")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("66", httpResponse2.body());

    }

    @Test
    public void testDefaultHeaderParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-header-param-default")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("\"zoo\"", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-header-param-default")
                        .addHeader("p", "something")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("\"something\"", httpResponse2.body());

    }


    @Test
    public void testDefaultHeaderParamNoDefault() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-header-param-no-default")
                        .setMethodGet()
                        .build()
        );

        assertEquals(200, httpResponse.code());
        assertEquals("", httpResponse.body());


        final HttpTextResponse httpResponse2 = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/string-header-param-no-default")
                        .addHeader("p", "something")
                        .setMethodGet()
                        .build()
        );


        assertEquals(200, httpResponse2.code());
        assertEquals("\"something\"", httpResponse2.body());

    }


    @Test
    public void testNoCacheHeaders() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/cache")
                        .setMethodGet().setContentType("foo")
                        .setBody("foo")
                        .build()
        );

        assertEquals(200, httpResponse.code());


        final List<String> controls = (List<String>) httpResponse.headers().getAll(HttpHeaders.CACHE_CONTROL);

        Assert.assertEquals("max-age=0", controls.get(0));

        Assert.assertEquals("no-cache, no-store", controls.get(1));

        assertEquals("true", httpResponse.body());


    }

    @Test
    public void testNoJSONParseWithBytes() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/body/bytes")
                        .setMethodPost().setContentType("foo")
                        .setBody("foo")
                        .build()
        );

        puts(httpResponse);
        assertEquals(200, httpResponse.code());

        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testNoJSONParseWithString() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/body/string")
                        .setMethodPost().setContentType("foo")
                        .setBody("foo")
                        .build()
        );

        puts(httpResponse);
        assertEquals(200, httpResponse.code());

        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testPing() {
        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/ping");
        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());
    }


    @Test
    public void testRequiredParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/echo1").addParam("foo", "bar").build()
        );

        puts(httpResponse);
        assertEquals(200, httpResponse.code());
        assertEquals("\"bar\"", httpResponse.body());
    }


    @Test
    public void testRequiredParamMissing() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/echo1").build()
        );

        puts(httpResponse);
        assertEquals(400, httpResponse.code());
        assertEquals("[\"Unable to find required request param foo\\n\"]", httpResponse.body());
    }


    @Test
    public void testDefaultParam() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/echo2").build()
        );

        puts(httpResponse);
        assertEquals(200, httpResponse.code());
        assertEquals("\"mom\"", httpResponse.body());
    }


    @Test
    public void testCustomHttpExceptionCode() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/echo3").build()
        );

        puts(httpResponse);
        assertEquals(700, httpResponse.code());
        assertEquals("\"Ouch!\"", httpResponse.body());
    }

    @Test
    public void testCustomHttpExceptionCode2() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/echo4").build()
        );

        puts(httpResponse);
        assertEquals(900, httpResponse.code());
        assertEquals("\"Ouch!!\"", httpResponse.body());
    }


    @Test
    public void testCustomHttpExceptionCode3() {

        final HttpTextResponse httpResponse = httpServerSimulator.sendRequest(
                httpRequestBuilder.setUri("/es/echo5").build()
        );

        puts(httpResponse);
        assertEquals(666, httpResponse.code());
        assertEquals("\"Shoot!!\"", httpResponse.body());
    }


    @Test
    public void addEmployeeAsyncNoReturn() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/employee-add-async-no-return",
                new Employee(1, "Rick"));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());
    }


    @Test
    public void echoEmployeeStringToInt() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBodyPlain("/es/echoEmployee",
                "{\"id\":\"1\",\"name\":\"Rick\"}");

        assertEquals(200, httpResponse.code());
        assertEquals("{\"id\":1,\"name\":\"Rick\"}", httpResponse.body());
    }


    @Test
    public void echoBadJson() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBodyPlain("/es/echoEmployee",
                "{\"id\":\"a\",\"name\":\"Rick\"}");

        assertEquals(400, httpResponse.code());
        assertTrue(httpResponse.body().contains("Unable to JSON parse body"));
    }

    @Test
    public void addEmployeeBadJSON() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBodyPlain("/es/employee-ack",
                "{rick:name}");

        assertEquals(400, httpResponse.code());
        assertTrue(httpResponse.body().startsWith("[\"Unable to JSON parse"));
    }


    @Test
    public void addEmployeeWithReturn() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/employee-ack",
                new Employee(1, "Rick"));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());
    }


    @Test
    public void addEmployeeUsingCallback() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/employee-async-ack",
                new Employee(1, "Rick"));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());
    }

    @Test
    public void addEmployeeThrowException() {
        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/throw",
                new Employee(1, "Rick"));

        assertEquals(500, httpResponse.code());
        assertTrue(httpResponse.body().contains("\"message\":"));

        puts(httpResponse.body());

    }

    @Test
    public void testReturnEmployee() {
        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnemployee");

        assertEquals(200, httpResponse.code());

        Employee employee = JsonFactory.fromJson(httpResponse.body(), Employee.class);

        assertEquals(1, employee.getId());

        assertEquals("Rick", employee.getName());

    }

    @Test
    public void testReturnEmployeeCaseDoesNotMatter() {
        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/Returnemployee");

        assertEquals(200, httpResponse.code());

        Employee employee = JsonFactory.fromJson(httpResponse.body(), Employee.class);

        assertEquals(1, employee.getId());

        assertEquals("Rick", employee.getName());

    }

    @Test
    public void returnEmployeeCallback() {
        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnEmployeeCallback");

        assertEquals(200, httpResponse.code());

        Employee employee = JsonFactory.fromJson(httpResponse.body(), Employee.class);

        assertEquals(1, employee.getId());

        assertEquals("Rick", employee.getName());

    }


    @Test
    public void returnEmployeeCallback2() {
        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnEmployeeCallback2");

        assertEquals(777, httpResponse.code());


        assertEquals("crap/crap", httpResponse.contentType());

        assertEquals("Employee{id=1, name='Rick'}", httpResponse.body());

        puts(httpResponse);

    }

}
