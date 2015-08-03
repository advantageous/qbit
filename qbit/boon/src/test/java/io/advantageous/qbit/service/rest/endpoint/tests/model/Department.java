package io.advantageous.qbit.service.rest.endpoint.tests.model;

import java.util.List;

public class Department {

    private final long id;
    private final List<Employee> employeeList;

    public Department(long id, List<Employee> employeeList) {
        this.id = id;
        this.employeeList = employeeList;
    }
}
