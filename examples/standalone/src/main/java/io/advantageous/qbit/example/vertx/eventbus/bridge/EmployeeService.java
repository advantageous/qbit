package io.advantageous.qbit.example.vertx.eventbus.bridge;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.annotation.http.Bridge;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;

@RequestMapping("/es/1.0")
public class EmployeeService {

    @Bridge("/employee/")
    public boolean addEmployee(final Employee employee) {
        System.out.println(employee);
        return true;
    }

    @Bridge("/employee/err/")
    public boolean addEmployeeError(final Employee employee) {
        throw new IllegalStateException("Employee can't be added");
    }

    @Bridge
    public void getEmployee(final Callback<Employee> callback, final String id) {
        callback.returnThis(new Employee(id, "Bob", "Jingles", 1962, 999999999));
    }


    @GET("/employee/")
    public void getEmployeeWithParam(final Callback<Employee> callback,
                                     @RequestParam(value = "id", required = true) final String id) {
        callback.returnThis(new Employee(id, "Bob", "Jingles", 1962, 999999999));
    }

    @Bridge
    public Employee singleton(final Employee employee) {
        return employee;
    }

    @Bridge
    public boolean twoArg(final Employee employee, boolean flag) {

        return employee.getId().equals("rick") && flag;
    }

    @Bridge
    public List<Employee> list(final List<Employee> employees) {
        return employees;
    }

}
