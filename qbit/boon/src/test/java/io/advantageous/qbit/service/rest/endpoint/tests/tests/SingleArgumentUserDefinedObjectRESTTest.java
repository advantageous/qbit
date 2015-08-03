package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.EmployeeServiceSingleObjectTestService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SingleArgumentUserDefinedObjectRESTTest {


    ServiceEndpointServer serviceEndpointServer;

    HttpServerSimulator httpServerSimulator;

    @Before
    public void before() {
        httpServerSimulator = new HttpServerSimulator();
        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setHttpServer(httpServerSimulator).build()
                .initServices(new EmployeeServiceSingleObjectTestService()).startServer();
    }


    @Test
    public void testPing() {
        final HttpResponse httpResponse = httpServerSimulator.get("/es/ping");
        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());
    }

    @Test
    public void addEmployeeAsyncNoReturn() {
        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/employee-add-async-no-return",
                new Employee(1, "Rick"));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());
    }


    @Test
    public void addEmployeeWithReturn() {
        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/employee-ack",
                new Employee(1, "Rick"));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());
    }


    @Test
    public void addEmployeeUsingCallback() {
        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/employee-async-ack",
                new Employee(1, "Rick"));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());
    }

    @Test
    public void addEmployeeThrowException() {
        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/throw",
                new Employee(1, "Rick"));

        assertEquals(500, httpResponse.code());
        assertTrue(httpResponse.body().startsWith("{\"detailMessage"));

    }

    @Test
    public void testReturnEmployee() {
        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnemployee");

        assertEquals(200, httpResponse.code());

        Employee employee = JsonFactory.fromJson(httpResponse.body(), Employee.class);

        assertEquals(1, employee.getId());

        assertEquals("Rick", employee.getName());

    }

    @Test
    public void testReturnEmployeeCaseDoesNotMatter() {
        final HttpResponse httpResponse = httpServerSimulator.get("/es/Returnemployee");

        assertEquals(200, httpResponse.code());

        Employee employee = JsonFactory.fromJson(httpResponse.body(), Employee.class);

        assertEquals(1, employee.getId());

        assertEquals("Rick", employee.getName());

    }

    @Test
    public void returnEmployeeCallback() {
        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnEmployeeCallback");

        assertEquals(200, httpResponse.code());

        Employee employee = JsonFactory.fromJson(httpResponse.body(), Employee.class);

        assertEquals(1, employee.getId());

        assertEquals("Rick", employee.getName());

    }

}
