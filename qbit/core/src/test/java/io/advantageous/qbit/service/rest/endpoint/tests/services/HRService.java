package io.advantageous.qbit.service.rest.endpoint.tests.services;


import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Department;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.qbit.service.rest.endpoint.tests.model.PhoneNumber;

import java.util.*;
import java.util.function.Predicate;


@RequestMapping("/hr")
@Service
public class HRService {

    final Map<Integer, Department> departmentMap = new HashMap<>();


    @RequestMapping("/department/")
    public List<Department> getDepartments() {
        return new ArrayList<>(departmentMap.values());
    }

    @RequestMapping(value = "/department/{departmentId}/", method = RequestMethod.POST)
    public boolean addDepartments(@PathVariable("departmentId") Integer departmentId,
                                  final Department department) {

        departmentMap.put(departmentId, department);
        return true;
    }

    @RequestMapping(value = "/department/{departmentId}/employee/", method = RequestMethod.POST)
    public boolean addEmployee(@PathVariable("departmentId") Integer departmentId,
                               final Employee employee) {

        final Department department = departmentMap.get(departmentId);

        if (department == null) {
            throw new IllegalArgumentException("Department " + departmentId + " does not exist");
        }

        department.addEmployee(employee);
        return true;
    }

    @RequestMapping(value = "/department/{departmentId}/employee/{employeeId}", method = RequestMethod.GET)
    public Employee getEmployee(@PathVariable("departmentId") Integer departmentId,
                                @PathVariable("employeeId") Long employeeId) {

        final Department department = departmentMap.get(departmentId);

        if (department == null) {
            throw new IllegalArgumentException("Department " + departmentId + " does not exist");
        }

        Optional<Employee> employee = department.getEmployeeList().stream().filter(new Predicate<Employee>() {
            @Override
            public boolean test(Employee employee) {
                return employee.getId() == employeeId;
            }
        }).findFirst();

        if (employee.isPresent()) {
            return employee.get();
        } else {
            throw new IllegalArgumentException("Employee with id " + employeeId + " Not found ");
        }
    }


    public Map<Integer, Department> getDepartmentMap() {
        return departmentMap;
    }


    @RequestMapping(value = "/department/{departmentId}/employee/{employeeId}/phoneNumber/",
            method = RequestMethod.POST)
    public boolean addPhoneNumber(@PathVariable("departmentId") Integer departmentId,
                                  @PathVariable("employeeId") Long employeeId,
                                  PhoneNumber phoneNumber) {

        Employee employee = getEmployee(departmentId, employeeId);
        employee.addPhoneNumber(phoneNumber);
        return true;
    }


    @RequestMapping(value = "/department/{departmentId}/employee/{employeeId}/phoneNumber/")
    public List<PhoneNumber> getPhoneNumbers(@PathVariable("departmentId") Integer departmentId,
                                             @PathVariable("employeeId") Long employeeId) {

        Employee employee = getEmployee(departmentId, employeeId);
        return employee.getPhoneNumbers();
    }

}
