package io.advantageous.qbit.example.callback;


import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmployeeServiceImpl implements EmployeeService {

    @Override
    public void getEmployeesAsMap(final Callback<Map<String, Employee>> empMapCallback) {

        empMapCallback.returnThis(Maps.map("rick", new Employee("Rick")));
    }

    @Override
    public void getEmployeesAsList(final Callback<List<Employee>> empListCallback) {

        empListCallback.returnThis(Lists.list(new Employee("Rick")));
    }


    @Override
    public void findEmployeeByName(final Callback<Optional<Employee>> employeeCallback,
                                   final String name) {

        if (name.equals("Rick")) {
            employeeCallback.returnThis(Optional.of(new Employee("Rick")));
        } else {
            employeeCallback.returnThis(Optional.empty());
        }
    }

}
