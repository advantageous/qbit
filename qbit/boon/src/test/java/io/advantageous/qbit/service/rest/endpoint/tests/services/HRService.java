package io.advantageous.qbit.service.rest.endpoint.tests.services;


import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Department;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/hr")
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

        if (department ==  null) {
            throw new IllegalArgumentException("Department " + departmentId + " does not exist");
        }

        department.addEmployee(employee);
        return true;
    }


    public Map<Integer, Department> getDepartmentMap() {
        return departmentMap;
    }
}
