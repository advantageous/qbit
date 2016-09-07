package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.boon.spi.BoonJsonMapper;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.EmployeeServiceCollectionTestService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import io.advantageous.qbit.spi.FactorySPI;
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

        FactorySPI.setHttpServerFactory((options, endPointName, systemManager, serviceDiscovery,
                                         healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit, a, b, z)
                -> httpServerSimulator);


        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true)
                .build()
                .initServices(new EmployeeServiceCollectionTestService()).startServer();
    }


    @Test
    public void testList() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployees",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());

    }


    @Test
    public void testListWithReturn() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithReturn",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testReturnList() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnList");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }

    @Test
    public void testReturnListByCallback() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnListByCallback");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }


    @Test
    public void testReturnSet() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnSet");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }


    @Test
    public void testReturnArrayByCallback() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnArrayByCallback");

        System.out.println(httpResponse.body());

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }

    @Test
    public void testReturnArray() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnArray");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }


    @Test
    public void testReturnSetByCallback() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnSetByCallback");

        assertEquals(200, httpResponse.code());

        List<Employee> employees = JsonFactory.fromJsonArray(httpResponse.body(), Employee.class);

        assertEquals(2, employees.size());

        System.out.println(employees);

    }


    @Test
    public void testReturnMap() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnMap");

        assertEquals(200, httpResponse.code());

        Map<String, Employee> employeeMap = new BoonJsonMapper().fromJsonMap(httpResponse.body(), String.class, Employee.class);

        assertEquals(2, employeeMap.size());

        System.out.println(employeeMap);

    }


    @Test
    public void testReturnMapByCallback() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnMapByCallback");

        assertEquals(200, httpResponse.code());

        Map<String, Employee> employeeMap = new BoonJsonMapper().fromJsonMap(httpResponse.body(), String.class, Employee.class);

        assertEquals(2, employeeMap.size());

        System.out.println(employeeMap);

    }


    @Test
    public void testReturnMapByPromise() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnMapByPromise");

        assertEquals(200, httpResponse.code());

        Map<String, Employee> employeeMap = new BoonJsonMapper().fromJsonMap(httpResponse.body(), String.class, Employee.class);

        assertEquals(2, employeeMap.size());

        System.out.println(employeeMap);

    }

    @Test
    public void testReturnMapByPromiseHandle() {

        final HttpTextResponse httpResponse = httpServerSimulator.get("/es/returnMapByPromiseHandle");

        assertEquals(200, httpResponse.code());

        Map<String, Employee> employeeMap = new BoonJsonMapper().fromJsonMap(httpResponse.body(), String.class, Employee.class);

        assertEquals(2, employeeMap.size());

        System.out.println(employeeMap);

    }


    @Test
    public void testSendEmployeesWithCallback() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallback",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSet() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesSet",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());

    }


    @Test
    public void testListWithReturnSet() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithReturnSet",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSendEmployeesWithCallbackSet() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallbackSet",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testArray() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesArray",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(202, httpResponse.code());
        assertEquals("\"success\"", httpResponse.body());

    }


    @Test
    public void testListWithReturnArray() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithReturnArray",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSendEmployeesWithCallbackArray() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallbackArray",
                Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }


    @Test
    public void testSendEmployeesWithCallbackMap() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/es/sendEmployeesWithCallbackMap",
                Maps.map("1", new Employee(1, "Rick"), "2",
                        new Employee(2, "Diana")));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

    }
}
