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

package io.advantageous.qbit.example.events;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.EventChannel;
import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.Service;
import org.boon.core.Sys;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * Created by rhightower on 2/11/15.
 */
public class EmployeeEventExampleUsingEventProxyToSendEvents {


    public static final String NEW_HIRE_CHANNEL = "com.mycompnay.employee.new";

    public static final String PAYROLL_ADJUSTMENT_CHANNEL = "com.mycompnay.employee.payroll";

    public static void main(String... args) {


        /* Create you own private event bus. */
        EventManager privateEventBus = QBit.factory().createEventManager();

        /* Create a service queue for this event bus. */
        Service privateEventBusService = serviceBuilder()
                .setServiceObject(privateEventBus)
                .setInvokeDynamic(false).build();


        final EventBusProxyCreator eventBusProxyCreator =
                QBit.factory().eventBusProxyCreator();

        final EmployeeEventManager employeeEventManager =
                eventBusProxyCreator.createProxy(privateEventBus, EmployeeEventManager.class);

        /*
        Create your EmployeeHiringService but this time pass the private event bus.
        Note you could easily use Spring or Guice for this wiring.
         */
        EmployeeHiringService employeeHiring = new EmployeeHiringService(employeeEventManager);



        /* Now createWithWorkers your other service POJOs which have no compile time dependencies on QBit. */
        PayrollService payroll = new PayrollService();
        BenefitsService benefits = new BenefitsService();
        VolunteerService volunteering = new VolunteerService();


        /** Employee hiring service. */
        Service employeeHiringService = serviceBuilder()
                .setServiceObject(employeeHiring)
                .setInvokeDynamic(false).build();
        /** Payroll service */
        Service payrollService = serviceBuilder()
                .setServiceObject(payroll)
                .setInvokeDynamic(false).build();
        /** Employee Benefits service. */
        Service employeeBenefitsService = serviceBuilder()
                .setServiceObject(benefits)
                .setInvokeDynamic(false).build();
        /* Community outreach program. */
        Service volunteeringService = serviceBuilder()
                .setServiceObject(volunteering)
                .setInvokeDynamic(false).build();


        /* Now wire in the event bus so it can fire events into the service queues. */
        privateEventBus.joinService(payrollService);
        privateEventBus.joinService(employeeBenefitsService);
        privateEventBus.joinService(volunteeringService);


        privateEventBusService.start();
        employeeHiringService.start();
        volunteeringService.start();
        payrollService.start();
        employeeBenefitsService.start();


        /** Now createWithWorkers the service proxy like before. */
        EmployeeHiringServiceClient employeeHiringServiceClientProxy =
                employeeHiringService.createProxy(EmployeeHiringServiceClient.class);

        /** Call the hireEmployee method which triggers the other events. */
        employeeHiringServiceClientProxy.hireEmployee(new Employee("Rick", 1));

        flushServiceProxy(employeeHiringServiceClientProxy);

        Sys.sleep(5_000);

    }

    interface EmployeeHiringServiceClient {
        void hireEmployee(final Employee employee);

    }


    interface EmployeeEventManager {

        @EventChannel(NEW_HIRE_CHANNEL)
        void sendNewEmployee(Employee employee);


        @EventChannel(PAYROLL_ADJUSTMENT_CHANNEL)
        void sendSalaryChangeEvent(Employee employee, int newSalary);

    }

    public static class Employee {
        final String firstName;
        final int employeeId;

        public Employee(String firstName, int employeeId) {
            this.firstName = firstName;
            this.employeeId = employeeId;
        }

        public String getFirstName() {
            return firstName;
        }

        public int getEmployeeId() {
            return employeeId;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "firstName='" + firstName + '\'' +
                    ", employeeId=" + employeeId +
                    '}';
        }
    }

    public static class EmployeeHiringService {

        final EmployeeEventManager eventManager;

        public EmployeeHiringService(final EmployeeEventManager employeeEventManager) {
            this.eventManager = employeeEventManager;
        }


        @QueueCallback(QueueCallbackType.EMPTY)
        private void noMoreRequests() {
            flushServiceProxy(eventManager);
        }


        @QueueCallback(QueueCallbackType.LIMIT)
        private void hitLimitOfRequests() {
            flushServiceProxy(eventManager);
        }


        public void hireEmployee(final Employee employee) {

            int salary = 100;
            System.out.printf("Hired employee %s\n", employee);

            //Does stuff to hire employee


            eventManager.sendNewEmployee(employee);
            eventManager.sendSalaryChangeEvent(employee, salary);


        }

    }

    public static class BenefitsService {

        @OnEvent(NEW_HIRE_CHANNEL)
        public void enroll(final Employee employee) {

            System.out.printf("Employee enrolled into benefits system employee %s %d\n",
                    employee.getFirstName(), employee.getEmployeeId());

        }

    }

    public static class VolunteerService {

        @OnEvent(NEW_HIRE_CHANNEL)
        public void invite(final Employee employee) {

            System.out.printf("Employee will be invited to the community outreach program %s %d\n",
                    employee.getFirstName(), employee.getEmployeeId());

        }

    }

    public static class PayrollService {

        @OnEvent(PAYROLL_ADJUSTMENT_CHANNEL)
        public void addEmployeeToPayroll(final Employee employee, int salary) {

            System.out.printf("Employee added to payroll  %s %d %d\n",
                    employee.getFirstName(), employee.getEmployeeId(), salary);

        }

    }
}
