package io.advantageous.qbit.service.rest.endpoint.tests.services;


import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.boon.core.Sets;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;
import io.advantageous.reakt.promise.Promises;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/es")
@Service
public class EmployeeServiceCollectionTestService {

    @RequestMapping(value = "/sendEmployees", method = RequestMethod.POST)
    public void sendEmployess(final List<Employee> employeeList) {

        System.out.println(employeeList);

    }

    @RequestMapping(value = "/sendEmployeesWithReturn", method = RequestMethod.POST)
    public boolean sendEmployeesWithReturn(final List<Employee> employeeList) {

        System.out.println(employeeList);

        return true;
    }


    @RequestMapping(value = "/sendEmployeesWithCallback", method = RequestMethod.POST)
    public void sendEmployeesWithCallback(final Callback<Boolean> callback,
                                          final List<Employee> employeeList) {

        System.out.println(employeeList);

        callback.resolve(true);
    }


    @RequestMapping(value = "/sendEmployeesSet", method = RequestMethod.POST)
    public void sendEmployessSet(final Set<Employee> employeeList) {

        System.out.println("Set " + employeeList);

    }

    @RequestMapping(value = "/sendEmployeesWithReturnSet", method = RequestMethod.POST)
    public boolean sendEmployeesWithReturnSet(final Set<Employee> employeeList) {

        System.out.println("Set " + employeeList);

        return true;
    }


    @RequestMapping(value = "/sendEmployeesWithCallbackSet", method = RequestMethod.POST)
    public void sendEmployeesWithCallbackSet(final Callback<Boolean> callback,
                                             final Set<Employee> employeeList) {

        System.out.println("Set " + employeeList);

        callback.resolve(true);
    }


    @RequestMapping(value = "/sendEmployeesArray", method = RequestMethod.POST)
    public void sendEmployessArray(final Employee... employeeList) {

        System.out.println("Array " + Lists.list(employeeList));

    }

    @RequestMapping(value = "/sendEmployeesWithReturnArray", method = RequestMethod.POST)
    public boolean sendEmployeesWithReturnArray(final Employee[] employeeList) {

        System.out.println("Array " + Lists.list(employeeList));

        return true;
    }


    @RequestMapping(value = "/sendEmployeesWithCallbackArray", method = RequestMethod.POST)
    public void sendEmployeesWithCallbackArray(final Callback<Boolean> callback,
                                               final Employee[] employeeList) {

        System.out.println("Array " + Lists.list(employeeList));

        callback.resolve(true);
    }


    @RequestMapping(value = "/sendEmployeesWithCallbackMap", method = RequestMethod.POST)
    public void sendEmployeesWithCallbackMap(final Callback<Boolean> callback,
                                             final Map<String, Employee> employeeMap) {

        System.out.println("Map " + employeeMap);

        callback.resolve(true);
    }


    @RequestMapping("/returnList")
    public List<Employee> returnList() {
        return Lists.list(new Employee(1, "Rick"), new Employee(2, "Diana"));
    }


    @RequestMapping("/returnListByCallback")
    public void returnListByCallback(Callback<List<Employee>> callback) {
        callback.resolve(Lists.list(new Employee(1, "Rick"), new Employee(2, "Diana")));
    }


    @RequestMapping("/returnSet")
    public Set<Employee> returnSet() {
        return Sets.set(new Employee(1, "Rick"), new Employee(2, "Diana"));
    }


    @RequestMapping("/returnArray")
    public Employee[] returnArray() {
        return new Employee[]{new Employee(1, "Rick"), new Employee(2, "Diana")};
    }


    @RequestMapping("/returnArrayByCallback")
    public void returnArrayByCallback(Callback<Employee[]> callback) {
        callback.resolve(new Employee[]{new Employee(1, "Rick"), new Employee(2, "Diana")});
    }


    @RequestMapping("/returnSetByCallback")
    public void returnSetByCallback(Callback<Set<Employee>> callback) {
        callback.resolve(Sets.set(new Employee(1, "Rick"), new Employee(2, "Diana")));
    }


    @RequestMapping("/returnMap")
    public Map<String, Employee> returnMap() {
        return Maps.map("1", new Employee(1, "Rick"), "2", new Employee(2, "Diana"));

    }

    @RequestMapping("/returnMapByCallback")
    public void returnMapByCallback(Callback<Map<String, Employee>> callback) {
        callback.resolve(Maps.map("1", new Employee(1, "Rick"), "2", new Employee(2, "Diana")));
    }

    @RequestMapping("/returnMapByPromise")
    public Promise<Map<String, Employee>> returnMapByPromise() {
        return Promises.invokablePromise(promise -> promise.resolve(Maps.map("1", new Employee(1, "Rick"), "2", new Employee(2, "Diana"))));
    }

    @RequestMapping("/returnMapByPromiseHandle")
    public PromiseHandle<Map<String, Employee>> returnMapByPromiseHandle() {
        return Promises.deferCall(promise -> promise.resolve(Maps.map("1", new Employee(1, "Rick"), "2", new Employee(2, "Diana"))));
    }

}
