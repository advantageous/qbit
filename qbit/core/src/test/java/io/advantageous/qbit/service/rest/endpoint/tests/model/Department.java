package io.advantageous.qbit.service.rest.endpoint.tests.model;

import java.util.ArrayList;
import java.util.List;

public class Department {

    private final long id;
    private final List<Employee> employeeList;

    public Department(long id, List<Employee> employeeList) {
        this.id = id;
        this.employeeList = employeeList;
    }

    public void addEmployee(Employee employee) {
        employeeList.add(employee);
    }

    public List<Employee> getEmployeeList() {
        return new ArrayList<>(employeeList);
    }

    public long getId() {
        return id;
    }
}
