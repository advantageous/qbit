/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit;

import io.advantageous.boon.Boon;
import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Lists;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.MethodCallImpl;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.Protocol;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.impl.ServiceBundleImpl;
import io.advantageous.qbit.spi.BoonProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;


/**
 * A test
 * Created by Richard on 9/27/14.
 */
public class IntegrationTestForRESTStyleCallsTest {




    EmployeeService employeeService;
    ServiceBundle serviceBundle;
    ServiceBundleImpl serviceBundleImpl;

    Factory factory;
    MultiMap<String, String> params = null;
    MethodCall<Object> call = null;

    boolean ok = true;

    ReceiveQueue<Response<Object>> responseReceiveQueue = null;

    Response<Object> response;
    ProtocolEncoder encoder = new BoonProtocolEncoder();
    private Employee rick;
    private Employee diana;
    private Employee whitney;
    private String returnAddress = "clientIdAkaReturnAddress";
    private Employee employee;


    @Before
    public void setup() {
        employeeService = new EmployeeService();

        factory = QBit.factory();

        serviceBundle = serviceBundleBuilder()//.setRequestQueueBuilder(QueueBuilder.queueBuilder().setPollWait(1000))
                .setAddress("/root").buildAndStart();
        serviceBundleImpl = ( ServiceBundleImpl ) serviceBundle;

        responseReceiveQueue = serviceBundle.responses().receiveQueue();


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

        /* Create employee client */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, rick, params);

        serviceBundle.call(call);
        serviceBundle.flush();

        Sys.sleep(1000);

        response = responseReceiveQueue.pollWait();

        Str.equalsOrDie(returnAddress, response.returnAddress());


    }


    @Test
    public void testBasicCrud() {

        String addressToMethodCall = "/root/employeeRest/employee/add";

        /* Create employee client */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, rick, params);

        doCall();

        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);


        Boon.equalsOrDie(true, response.body());

        /** Read employee back from client */

        addressToMethodCall = "/root/employeeRest/employee/10";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);
        doCall();
        response = responseReceiveQueue.pollWait();

        validateRick();


        /** Search for employees */
        addressToMethodCall = "/root/employeeRest/employee/search/";

        params = new MultiMapImpl<>();

        params.put("level", "" + 1000);

        params.put("active", "" + rick.active);

        puts("LEVEL", params.get("level"));


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);
        doCall();
        response = responseReceiveQueue.pollWait();

        puts("BODY", response.body());

        Employee employee1 = ( Employee ) response.body();
        Boon.equalsOrDie(1000, employee1.level);
        Boon.equalsOrDie(rick.active, employee1.active);


        /** Remove employee from Service */
        addressToMethodCall = "/root/employeeRest/employee/remove/";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick.id), params);
        doCall();
        response = responseReceiveQueue.pollWait();

        Boon.equalsOrDie(true, response.body());


        /** Read employee from Service */

        addressToMethodCall = "/root/employeeRest/employee/10";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);
        doCall();
        response = responseReceiveQueue.pollWait();


        puts(response.body());

        Boon.equalsOrDie(null, response.body());


    }


    @Test
    public void testTwoUriPathVars() {


        employeeService.addEmployee(rick);
        Sys.sleep(10);



        /* Create employee client */
        serviceBundle.addService(employeeService);

        /** Promote employee from Service */
        String addressToMethodCall = "/root/employeeRest/employee/promote/100/10";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick), params);
        doCall();
        response = responseReceiveQueue.pollWait();


        Boon.equalsOrDie(true, response.body());
        Sys.sleep(10);


        final Employee employee1 = employeeService.readEmployee(10);
        Boon.equalsOrDie(rick.id, employee1.id);
        Boon.equalsOrDie(100, employee1.level);


    }

    @Test
    public void testRequestParamBinding() {


        params = new MultiMapImpl<>();
        params.put("idOfEmployee", "" + 10);

        String addressToMethodCall = "/root/employeeRest/addEmployeeWithParams";

        /* Create employee client */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, rick, params);


        doCall();


        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);

        if ( response.body() instanceof Exception ) {
            Exception ex = ( Exception ) response.body();
            ex.printStackTrace();
        }

        Boon.equalsOrDie(true, response.body());

        /** Read employee back from client */


        params = new MultiMapImpl<>();
        params.put("idOfEmployee", "" + 10);

        addressToMethodCall = "/root/employeeRest/employeeRead";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);
        doCall();
        response = responseReceiveQueue.pollWait();

        validateRick();


    }


    @Test
    public void testRequestParamBinding2Params() {


        params = new MultiMapImpl<>();
        params.put("idOfEmployee", "" + 10);
        params.put("deptName", "Engineering");


        String addressToMethodCall = "/root/employeeRest/addEmployeeWithParams2";

        /* Create employee client */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, rick, params);


        doCall();


        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);

        if ( response.body() instanceof Exception ) {
            Exception ex = ( Exception ) response.body();
            ex.printStackTrace();
        }

        Boon.equalsOrDie(true, response.body());

        /** Read employee back from client */


        params = new MultiMapImpl<>();
        params.put("idOfEmployee", "" + 10);

        addressToMethodCall = "/root/employeeRest/employeeRead";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);
        doCall();
        response = responseReceiveQueue.pollWait();

        validateRick();


    }


    @Test
    public void testException() {


        String addressToMethodCall = "/root/employeeRest/employee/error/";

        /* Create employee client */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);


        doCall();


        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);

        ok = response.wasErrors() || die();

        puts(response.body());

    }

    @Test
    public void testAsync() {

        String addressToMethodCall = "/root/employeeRest/async/";

        /* Create employee client */
        serviceBundle.addService(employeeService);

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, "", params);

        doCall();

        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);


        puts(response.body());

        ok = !response.wasErrors() || die();

        Boon.equalsOrDie("hi mom", response.body());

    }


    @Test
    public void testAsync2() {

        String addressToMethodCall = "/root/employeeRest/asyncHelloWorld/";

        /* Create employee client */
        serviceBundle.addService(employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress,

                new Object[]{
                        new Callback<String>() {

                            @Override
                            public void accept(String s) {
                                puts("$$$$$$$$$$$$$$$$$$" + s);
                            }
                        },
                        "World"}, params);

        doCall();

        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);

        puts(response.body());

        ok = !response.wasErrors() || die();


        puts(response.body());
        Boon.equalsOrDie("Hello World", response.body());

    }

    private void doCall() {

        if ( !Str.isEmpty(call.body()) ) {
            String qbitStringBody = encoder.encodeAsString(call);
            puts("\nPROTOCOL\n", qbitStringBody.replace(( char ) Protocol.PROTOCOL_SEPARATOR, '\n').replace(( char ) Protocol.PROTOCOL_ARG_SEPARATOR, '\n'), "\nPROTOCOL END\n");
            call = factory.createMethodCallToBeParsedFromBody(null, null, null, null, qbitStringBody, null);
        }

        if ( params != null ) {
            MethodCallImpl impl = ( MethodCallImpl ) call;
//            if (params != null)
//                impl.params(params);
        }
        serviceBundle.call(call);

        serviceBundle.flushSends();
        Sys.sleep(100);
        serviceBundle.flush();
        Sys.sleep(200);
    }

    private void validateRick() {
        employee = ( Employee ) response.body();
        Boon.equalsOrDie(rick.id, employee.id);
        Boon.equalsOrDie(rick.active, employee.active);
        Boon.equalsOrDie(rick.firstName, employee.firstName);
        Boon.equalsOrDie(rick.lastName, employee.lastName);
        Boon.equalsOrDie(rick.salary.intValue(), employee.salary.intValue());

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

    @RequestMapping( "/employeeRest/" )
    public static class EmployeeService {
        Map<Integer, Employee> map = new ConcurrentHashMap<>();


        @RequestMapping( "/employee/add" )
        public boolean addEmployee(Employee employee) {
            map.put(employee.id, employee);
            return true;
        }


        @RequestMapping( "/employee/search/" )
        public Employee findEmployee(Employee employee) {
            return employee;
        }


        @RequestMapping( "/employee/promote/{1}/{0}" )
        public boolean promoteEmployee(int id, int level) {

            final Employee employee = map.get(id);

            employee.level = level;


            map.put(employee.id, employee);
            return true;
        }


        @RequestMapping( "/employee/{0}" )
        public Employee readEmployee(int id) {
            return map.get(id);
        }


        @RequestMapping( "/employeeRead" )
        public Employee readEmployeeWithParamBindings(@RequestParam( value = "idOfEmployee" ) int id) {
            return map.get(id);
        }


        @RequestMapping( "/addEmployeeWithParams" )
        public boolean addEmployeeWithParams(@RequestParam( required = true, value = "idOfEmployee" ) int id, Employee employee) {

            puts("addEmployeeWithParams CALLED", id, employee);
            map.put(id, employee);
            return true;

        }


        @RequestMapping( "/addEmployeeWithParams2" )
        public boolean addEmployeeWithParams2(
                @RequestParam( required = true, value = "idOfEmployee" ) int id,
                @RequestParam( "deptName") String deptName,
                Employee employee) {

            puts("addEmployeeWithParams2 CALLED", id, deptName, employee);
            map.put(id, employee);
            return true;

        }

        @RequestMapping( "/employee/remove/" )
        public boolean removeEmployee(int id) {
            map.remove(id);
            return true;
        }


        @RequestMapping( "/employee/error/" )
        public boolean throwAnExceptionNoMatterWhat() {
            die("YOU ARE NOT THE BOSS OF ME JAVA!");
            return true;
        }

        @RequestMapping( "/async/" )
        public void async(Callback<String> handler) {
            handler.accept("hi mom");
        }

        @RequestMapping( "/asyncHelloWorld/" )
        public void asyncHelloWorld(Callback<String> handler, String arg) {

            handler.accept("Hello " + arg);
        }

    }


}
