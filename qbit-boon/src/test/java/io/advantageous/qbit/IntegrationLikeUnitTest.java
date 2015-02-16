/*******************************************************************************

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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.impl.ServiceBundleImpl;
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
 * Created by Richard on 9/27/14.
 */
public class IntegrationLikeUnitTest {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();

    }

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

    private String returnAddress = "clientIdAkaReturnAddress";


    @Before
    public void setup() {
        employeeService = new EmployeeService();

        factory = QBit.factory();

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

        serviceBundle.call(call);
        serviceBundle.flush();

        Sys.sleep(1000);

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

        Boon.equalsOrDie(rick, response.body());


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

        Boon.equalsOrDie(rick, response.body());

        Employee employee = ( Employee ) response.body();

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

    private void doCall() {
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


            map.put(employee.id, employee);
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