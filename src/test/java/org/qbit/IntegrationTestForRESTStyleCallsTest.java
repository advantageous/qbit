package org.qbit;

import org.boon.*;
import org.boon.collections.MultiMap;
import org.boon.collections.MultiMapImpl;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;
import org.qbit.annotation.RequestMapping;
import org.qbit.annotation.RequestParam;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueue;
import org.qbit.service.Protocol;
import org.qbit.service.ServiceBundle;
import org.qbit.service.impl.ServiceBundleImpl;
import org.qbit.service.method.impl.MethodCallImpl;
import org.qbit.spi.ProtocolEncoder;
import org.qbit.spi.ProtocolEncoderVersion1;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.boon.Boon.puts;

/**
 * Created by Richard on 9/27/14.
 */
public class IntegrationTestForRESTStyleCallsTest {


    EmployeeService employeeService;
    ServiceBundle serviceBundle;
    ServiceBundleImpl serviceBundleImpl;

    Factory factory;
    MultiMap<String, String> params = null;
    MethodCall<Object> call = null;


    ReceiveQueue<Response<Object>> responseReceiveQueue = null;

    Response<Object> response;

    Object responseBody = null;
    private Employee rick;
    private Employee diana;
    private Employee whitney;

    private String returnAddress ="clientIdAkaReturnAddress";


    ProtocolEncoder encoder = new ProtocolEncoderVersion1();
    private Employee employee;


    @Before
    public void setup() {
        employeeService = new EmployeeService();

        factory = QBit.factory();
        final ServiceBundle bundle = factory.createBundle("/root");
        serviceBundle = bundle;
        serviceBundleImpl = (ServiceBundleImpl) bundle;

        responseReceiveQueue = bundle.responses();


        Employee employee = new Employee();
        employee.id = 10;
        employee.firstName = "Rick";
        employee.lastName = "Hightower";
        employee.salary = new BigDecimal("100");
        employee.active = true;

        rick = employee;

        employee = new Employee();
        employee.id = 1;
        employee.firstName = "Diana";
        employee.lastName = "Hightower";
        employee.active = true;
        employee.salary = new BigDecimal("100");

        diana = employee;


        employee = new Employee();
        employee.id = 2;
        employee.firstName = "Whitney";
        employee.lastName = "Hightower";
        employee.active = true;
        employee.salary = new BigDecimal("100");

        whitney = employee;

        returnAddress = "clientIdAkaReturnAddress";

    }


    @Test
    public void testBasic() {


        String addressToMethodCall = "/root/employeeRest/employee/add";

        /* Create employee service */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, rick, params );

        serviceBundle.call(call);
        serviceBundle.flushSends();

        Sys.sleep(1000);

        response = responseReceiveQueue.pollWait();

        Str.equalsOrDie(returnAddress, response.returnAddress());


    }


    @Test
    public void testBasicCrud() {

        String addressToMethodCall = "/root/employeeRest/employee/add";

        /* Create employee service */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, rick, params );

        doCall();

        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);


        Boon.equalsOrDie(true, response.body());

        /** Read employee back from service */

        addressToMethodCall = "/root/employeeRest/employee/10";

        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, "", params );
        doCall();
        response = responseReceiveQueue.pollWait();

        validateRick();


        /** Search for employees */
        addressToMethodCall = "/root/employeeRest/employee/search/";

        params = new MultiMapImpl<>();

        params.put("level", ""+1000);

        params.put("active", ""+rick.active);

        puts ("LEVEL", params.get("level"));


        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, "", params );
        doCall();
        response = responseReceiveQueue.pollWait();

        puts("BODY", response.body());

        Employee employee1 = (Employee) response.body();
        Boon.equalsOrDie(1000, employee1.level);
        Boon.equalsOrDie(rick.active, employee1.active);







        /** Promote employee from Service */
        addressToMethodCall = "/root/employeeRest/employee/promote/100/10";

        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, Lists.list(rick), params );
        doCall();
        response = responseReceiveQueue.pollWait();








        /** Read employee back from service */


        addressToMethodCall = "/root/employeeRest/employee/10";

        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, "", params );
        doCall();
        response = responseReceiveQueue.pollWait();


        validateRick();

        Boon.equalsOrDie(100, employee.level);



        /** Remove employee from Service */
        addressToMethodCall = "/root/employeeRest/employee/remove/";

        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, Lists.list(rick.id), params );
        doCall();
        response = responseReceiveQueue.pollWait();

        Boon.equalsOrDie(true, response.body());



        /** Read employee from Service */

        addressToMethodCall = "/root/employeeRest/employee/10";

        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, "", params );
        doCall();
        response = responseReceiveQueue.pollWait();


        puts(response.body());

        Boon.equalsOrDie(null, response.body());




    }


    @Test
    public void testRequestParamBinding() {


        params = new MultiMapImpl<>();
        params.put("idOfEmployee", ""+10);

        String addressToMethodCall = "/root/employeeRest/addEmployeeWithParams";

        /* Create employee service */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, rick, params );



        doCall();


        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);


        Boon.equalsOrDie(true, response.body());

        /** Read employee back from service */


        params = new MultiMapImpl<>();
        params.put("idOfEmployee", ""+10);

        addressToMethodCall = "/root/employeeRest/employeeRead";

        call = factory.createMethodCallByAddress(addressToMethodCall,
                returnAddress, "", params );
        doCall();
        response = responseReceiveQueue.pollWait();

        validateRick();


    }
    private void doCall() {

        if (!Str.isEmpty(call.body())) {
            String qbitStringBody = encoder.encodeAsString(call);
            puts("\nPROTOCOL\n",
                    qbitStringBody.replace((char) Protocol.PROTOCOL_SEPARATOR, '\n')
                            .replace((char) Protocol.PROTOCOL_ARG_SEPARATOR, '\n'),
                    "\nPROTOCOL END\n"
            );
            call = factory.createMethodCall(null, null, null, null, qbitStringBody, null);
        }

        if (params!=null) {
            MethodCallImpl impl = (MethodCallImpl) call;
            if (params != null)
            impl.params(params);
        }
        serviceBundle.call(call);
        serviceBundle.flushSends();
        Sys.sleep(100);
    }

    public static class Employee {
        String firstName;
        String lastName;
        BigDecimal salary;
        boolean active;
        int id;
        int level;

        @Override
        public String toString() {
            return "Employee{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", salary=" + salary +
                    ", active=" + active +
                    ", id=" + id +
                    ", level=" + level +
                    '}';
        }
    }

    @RequestMapping("/employeeRest/")
    public static class EmployeeService {
        Map<Integer, Employee> map = new ConcurrentHashMap<>();


        @RequestMapping("/employee/add")
        public boolean addEmployee(Employee employee) {
            map.put(employee.id, employee);
            return true;
        }


        @RequestMapping("/employee/search/")
        public Employee findEmployee(Employee employee) {
            return employee;
        }


        @RequestMapping("/employee/promote/{1}/{0}")
        public boolean promoteEmployee(int id, int level) {

            final Employee employee = map.get(id);

            employee.level = level;


            map.put(employee.id, employee);
            return true;
        }


        @RequestMapping("/employee/{0}")
        public Employee readEmployee(int id) {
            return map.get(id);
        }


        @RequestMapping("/employeeRead")
        public Employee readEmployeeWithParamBindings(
                @RequestParam(value="idOfEmployee") int id) {
            return map.get(id);
        }


        @RequestMapping("/addEmployeeWithParams")
        public boolean addEmployeeWithParams(
                @RequestParam(required =true, value="idOfEmployee") int id, Employee employee) {

            puts("addEmployeeWithParams CALLED", id, employee);
            map.put(id, employee);
            return true;

        }

        @RequestMapping("/employee/remove/")
        public boolean removeEmployee(int id) {
            map.remove(id);
            return true;
        }
    }

    private void validateRick() {
        employee =  (Employee)response.body();
        Boon.equalsOrDie(rick.id, employee.id);
        Boon.equalsOrDie(rick.active, employee.active);
        Boon.equalsOrDie(rick.firstName, employee.firstName);
        Boon.equalsOrDie(rick.lastName, employee.lastName);
        Boon.equalsOrDie(rick.salary.intValue(), employee.salary.intValue());

    }


}
