package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.EmployeeServiceSingleObjectTestService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SingleArgumentUserDefinedObjectRESTTest {


    ServiceEndpointServer serviceEndpointServer;

    HttpServerSimulator httpServerSimulator;
    HttpRequestBuilder httpRequestBuilder;


    @Before
    public void before() {
        httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();

        httpServerSimulator = new HttpServerSimulator();


        FactorySPI.setHttpServerFactory((options, endPointName, systemManager, serviceDiscovery,
                                         healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit, a, b)
                -> httpServerSimulator);

        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .build()
                .initServices(new EmployeeServiceSingleObjectTestService()).startServer();
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
