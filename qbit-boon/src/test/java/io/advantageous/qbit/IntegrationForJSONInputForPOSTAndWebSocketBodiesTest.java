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

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.Protocol;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.impl.ServiceBundleImpl;
import io.advantageous.qbit.spi.BoonProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.util.MultiMap;
import org.boon.Boon;
import org.boon.Exceptions;
import org.boon.Lists;
import org.boon.Str;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.boon.Boon.puts;

/**
 * @author  Richard on 9/27/14.
 */
public class IntegrationForJSONInputForPOSTAndWebSocketBodiesTest {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();

    }

    EmployeeService employeeService;
    ServiceBundle serviceBundle;
    ServiceBundleImpl serviceBundleImpl;

    Factory factory = QBit.factory();
    MultiMap<String, String> params = null;
    MethodCall<Object> call = null;

    ProtocolEncoder encoder = new BoonProtocolEncoder();

    ReceiveQueue<Response<Object>> responseReceiveQueue = null;

    Response<Object> response;

    Object responseBody = null;
    Employee employee;
    private Employee rick;
    private Employee diana;
    private Employee whitney;
    private String returnAddress = "clientIdAkaReturnAddress";


    @Before
    public void setup() {
        employeeService = new EmployeeService();

        final ServiceBundle bundle = new ServiceBundleBuilder().setAddress("/root").buildAndStart();

        serviceBundle = bundle;
        serviceBundleImpl = ( ServiceBundleImpl ) bundle;

        responseReceiveQueue = bundle.responses().receiveQueue();


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

        String addressToMethodCall = "/root/empservice/addEmployee";

        /* Create employee client */
        serviceBundle.addService("/empservice/", employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, rick, params);


        doCall();

        response = responseReceiveQueue.pollWait();

        Str.equalsOrDie(returnAddress, response.returnAddress());


    }


    @Test
    public void testBasicCrud() {

        String addressToMethodCall = "/root/empservice/addEmployee";

        /* Create employee client */
        serviceBundle.addService("/empservice/", employeeService);


        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, rick, params);

        doCall();

        response = responseReceiveQueue.pollWait();

        Exceptions.requireNonNull(response);

        /** Read employee back from client */

        addressToMethodCall = "/root/empservice/readEmployee";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick.id), params);
        doCall();
        response = responseReceiveQueue.pollWait();

        puts(response.body());

        validateRick();


        /** Read employee from Service */
        addressToMethodCall = "/root/empservice/promoteEmployee";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick, 100), params);
        doCall();
        response = responseReceiveQueue.pollWait();


        puts(response.body());


        /** Read employee back from client */

        addressToMethodCall = "/root/empservice/readEmployee";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick.id), params);
        doCall();
        response = responseReceiveQueue.pollWait();

        puts(response.body());

        employee = ( Employee ) response.body();

        validateRick();

        Boon.equalsOrDie(100, employee.level);


        /** Remove employee from Service */
        addressToMethodCall = "/root/empservice/removeEmployee";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick.id), params);
        doCall();
        response = responseReceiveQueue.pollWait();

        Boon.equalsOrDie(true, response.body());


        /** Read employee from Service */
        addressToMethodCall = "/root/empservice/readEmployee";

        call = factory.createMethodCallByAddress(addressToMethodCall, returnAddress, Lists.list(rick.id), params);
        doCall();
        response = responseReceiveQueue.pollWait();


        puts(response.body());

        Boon.equalsOrDie(null, response.body());


    }

    private void validateRick() {
        employee = ( Employee ) response.body();
        Boon.equalsOrDie(rick.id, employee.id);
        Boon.equalsOrDie(rick.active, employee.active);
        Boon.equalsOrDie(rick.firstName, employee.firstName);
        Boon.equalsOrDie(rick.lastName, employee.lastName);
        Boon.equalsOrDie(rick.salary.intValue(), employee.salary.intValue());

    }

    private void doCall() {

        String qbitStringBody = encoder.encodeAsString(call);
        puts("\nPROTOCOL\n", qbitStringBody.replace(( char ) Protocol.PROTOCOL_SEPARATOR, '\n').replace(( char ) Protocol.PROTOCOL_ARG_SEPARATOR, '\n'), "\nPROTOCOL END\n");
        call = factory.createMethodCallToBeParsedFromBody(null, null, null, null, qbitStringBody, null);
        serviceBundle.call(call);
        serviceBundle.flush();
        Sys.sleep(100);
    }

    public static class Employee {
        String firstName;
        String lastName;
        BigDecimal salary;
        boolean active;
        int id;
        int level;


    }

    public static class EmployeeService {
        Map<Integer, Employee> map = new ConcurrentHashMap<>();

        public boolean addEmployee(Employee employee) {
            map.put(employee.id, employee);
            return true;
        }


        public boolean promoteEmployee(Employee employee, int level) {

            employee.level = level;

            final Employee employee1 = map.get(employee.id);

            employee1.level = level;


            map.put(employee.id, employee1);
            return true;
        }

        public Employee readEmployee(int id) {
            return map.get(id);
        }


        public boolean removeEmployee(int id) {
            map.remove(id);
            return true;
        }
    }

}
