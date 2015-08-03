package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.BoonJsonMapper;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.EmployeeServiceCollectionTestService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class CollectionArrayUserDefinedObjectRESTTest {



    ServiceEndpointServer serviceEndpointServer;
    HttpServerSimulator httpServerSimulator;

    @Before
    public void before() {
        httpServerSimulator = new HttpServerSimulator();
        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setHttpServer(httpServerSimulator).build()
                .initServices(new EmployeeServiceCollectionTestService()).startServer();
    }



    @Test
    public void testList() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());

    }


    @Test
    public void testListWithReturn() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithReturn",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }




    @Test
    public void testReturnList() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnList");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }

    @Test
    public void testReturnListByCallback() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnListByCallback");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }


    @Test
    public void testReturnSet() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnSet");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }





    @Test
    public void testReturnArrayByCallback() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnArrayByCallback");

        System.out.println(httpResponse.body());

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }

    @Test
    public void testReturnArray() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnArray");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }


    @Test
    public void testReturnSetByCallback() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnSetByCallback");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }




    @Test
    public void testReturnMap() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnMap");

        assertEquals(200, httpResponse.code());

        Map<String, Employee> employeeMap = new BoonJsonMapper().fromJsonMap(httpResponse.body(), String.class, Employee.class);

        assertEquals(2, employeeMap.size());

        System.out.println(employeeMap);

    }


    @Test
    public void testReturnMapByCallback() {

        final HttpResponse httpResponse = httpServerSimulator.get("/es/returnMapByCallback");

        assertEquals(200, httpResponse.code());

        Map<String, Employee> employeeMap = new BoonJsonMapper().fromJsonMap(httpResponse.body(), String.class, Employee.class);

        assertEquals(2, employeeMap.size());

        System.out.println(employeeMap);

    }


    @Test
    public void testSendEmployeesWithCallback() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallback",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSet() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesSet",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());

    }


    @Test
    public void testListWithReturnSet() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithReturnSet",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSendEmployeesWithCallbackSet() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallbackSet",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }









    @Test
    public void testArray() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesArray",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());

    }


    @Test
    public void testListWithReturnArray() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithReturnArray",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSendEmployeesWithCallbackArray() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallbackArray",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSendEmployeesWithCallbackMap() {

        final HttpResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallbackMap",
                Maps.map("1", new Employee(1, "Rick"), "2",
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }
}
