package io.advantageous.qbit.service.rest.endpoint.tests.tests;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Department;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.services.HRService;
import io.advantageous.qbit.service.rest.endpoint.tests.sim.HttpServerSimulator;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class ResourceStyleRestService {


    ServiceEndpointServer serviceEndpointServer;
    HttpServerSimulator httpServerSimulator;

    HRService hrService;

    @Before
    public void before() {

        hrService = new HRService();
        httpServerSimulator = new HttpServerSimulator();

        FactorySPI.setHttpServerFactory((options, endPointName, systemManager, serviceDiscovery,
                                         healthServiceAsync, serviceDiscoveryTtl, serviceDiscoveryTtlTimeUnit)
                -> httpServerSimulator);


        serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setEnableHealthEndpoint(true).setEnableStatEndpoint(true)
                .build()
                .initServices(hrService).startServer();
    }



    @Test
    public void addDepartment() {

        final HttpTextResponse httpResponse = httpServerSimulator.postBody("/hr/department/100/",
                new Department(100, Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana"))));

        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());


        final List<Department> departments = hrService.getDepartments();

        assertEquals(1, departments.size());


        assertEquals(100, departments.get(0).getId());
    }

    @Test
    public void addEmployeeToDepartment() {

        httpServerSimulator.postBody("/hr/department/100/",
                new Department(100, Lists.list(new Employee(1, "Rick"),
                        new Employee(2, "Diana"))));


        HttpTextResponse httpResponse = httpServerSimulator.postBody("/hr/department/100/employee/",
                new Employee(3, "Noah"));


        assertEquals(200, httpResponse.code());
        assertEquals("true", httpResponse.body());

        Department department = hrService.getDepartmentMap().get(100);
        Map<String, Employee> employeeMap = Maps.toMap("id", department.getEmployeeList());
        Employee employee = employeeMap.get("3");

        assertEquals("Noah", employee.getName());


    }




}
