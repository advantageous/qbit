package io.advantageous.qbit.example.vertx.eventbus.bridge;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.POST;
import io.advantageous.qbit.reactive.Callback;

@RequestMapping("/es/1.0")
public class EmployeeService {

    @POST("/employee/")
    public boolean addEmployee(final Employee employee) {
        System.out.println(employee);
        return true;
    }

    @POST("/employee/err/")
    public boolean addEmployeeError(final Employee employee) {
        throw new IllegalStateException("Employee can't be added");
    }

    @GET("/employee/")
    public void getEmployee(final Callback<Employee> callback,
            @RequestParam("id") final String id) {
        callback.returnThis(new Employee(id, "Bob", "Jingles", 1962, 999999999));
    }

}
