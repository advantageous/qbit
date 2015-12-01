package io.advantageous.qbit.example.callback;

import io.advantageous.qbit.reactive.Callback;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmployeeService {
    void getEmployeesAsMap(Callback<Map<String, Employee>> empMapCallback);

    void getEmployeesAsList(Callback<List<Employee>> empListCallback);

    void findEmployeeByName(Callback<Optional<Employee>> employeeCallback,
                            String name);
}
