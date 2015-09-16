package io.advantageous.qbit.meta.swagger;

import java.util.Map;
import java.util.Set;

public class Department {
    Set<Employee> employeeList;
    Map<String, String> tags;
    Employee[] employeeArray;

    Department parent;
}
