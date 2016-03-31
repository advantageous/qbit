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

package io.advantageous.qbit.jms.example.events;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.EventChannel;
import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.jms.JmsServiceBuilder;
import io.advantageous.qbit.jms.JmsTextQueue;
import io.advantageous.qbit.queue.JsonQueue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.PortUtils;
import org.apache.activemq.broker.BrokerService;

import javax.jms.Session;
import java.util.concurrent.TimeUnit;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * EmployeeEventExampleUsingChannelsToSendEvents
 * created by rhightower on 2/11/15.
 */
@SuppressWarnings("ALL")
public class EmployeeEventExampleUsingChannelsToSendEventsWithJMS {


    public static final String NEW_HIRE_CHANNEL = "com.mycompnay.employee.new";


    public static void main(String... args) throws Exception {


        final BrokerService broker; //JMS Broker to make this a self contained example.
        final int port; //port to bind to JMS Broker to


        /* ******************************************************************************/
        /* START JMS BROKER. ************************************************************/
        /* Start up JMS Broker. */
        port = PortUtils.findOpenPortStartAt(4000);
        broker = new BrokerService();
        broker.addConnector("tcp://localhost:" + port);
        broker.start();

        Sys.sleep(5_000);


        /* ******************************************************************************/
        /* START JMS CLIENTS FOR SERVER A AND B *******************************************/
        /* Create a JMS Builder to create JMS Queues. */
        final JmsServiceBuilder jmsBuilder = JmsServiceBuilder.newJmsServiceBuilder().setPort(port)
                .setDefaultDestination(NEW_HIRE_CHANNEL).setAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);


        /* JMS client for server A. */
        final JsonQueue<Employee> employeeJsonQueueServerA =
                new JsonQueue<>(Employee.class, new JmsTextQueue(jmsBuilder));


        /* JMS client for server B. */
        final JsonQueue<Employee> employeeJsonQueueServerB =
                new JsonQueue<>(Employee.class, new JmsTextQueue(jmsBuilder));

        /* Send Queue to send messages to JMS broker. */
        final SendQueue<Employee> sendQueueA = employeeJsonQueueServerA.sendQueue();
        Sys.sleep(1_000);




        /*  ReceiveQueueB Queue B to receive messages from JMS broker. */
        final ReceiveQueue<Employee> receiveQueueB = employeeJsonQueueServerB.receiveQueue();
        Sys.sleep(1_000);




        /* ******************************************************************************/
        /* START EVENT BUS A  ************************************************************/
        /* Create you own private event bus for Server A. */
        final EventManager privateEventBusServerAInternal = EventManagerBuilder.eventManagerBuilder()
                .setEventConnector(new EventConnector() {
                    @Override
                    public void forwardEvent(EventTransferObject<Object> event) {

                        if (event.channel().equals(NEW_HIRE_CHANNEL)) {
                            System.out.println(event);
                            final Object body = event.body();
                            final Object[] bodyArray = ((Object[]) body);
                            final Employee employee = (Employee) bodyArray[0];
                            System.out.println(employee);
                            sendQueueA.sendAndFlush(employee);
                        }
                    }
                })
                .setName("serverAEventBus").build();

                /* Create a service queue for this event bus. */
        final ServiceQueue privateEventBusServiceQueueA = serviceBuilder()
                .setServiceObject(privateEventBusServerAInternal)
                .setInvokeDynamic(false).build();

        final EventManager privateEventBusServerA = privateEventBusServiceQueueA.createProxyWithAutoFlush(EventManager.class,
                50, TimeUnit.MILLISECONDS);



        /* Create you own private event bus for Server B. */
        final EventManager privateEventBusServerBInternal = EventManagerBuilder.eventManagerBuilder()
                .setEventConnector(new EventConnector() {
                    @Override
                    public void forwardEvent(EventTransferObject<Object> event) {
                        System.out.println(event);
                        final Object body = event.body();
                        final Object[] bodyArray = ((Object[]) body);
                        final Employee employee = (Employee) bodyArray[0];
                        System.out.println(employee);
                        sendQueueA.sendAndFlush(employee);
                    }
                })
                .setName("serverBEventBus").build();

                /* Create a service queue for this event bus. */
        final ServiceQueue privateEventBusServiceQueueB = serviceBuilder()
                .setServiceObject(privateEventBusServerBInternal)
                .setInvokeDynamic(false).build();


        final EventManager privateEventBusServerB = privateEventBusServiceQueueB.createProxyWithAutoFlush(EventManager.class,
                50, TimeUnit.MILLISECONDS);


        final EventBusProxyCreator eventBusProxyCreator =
                QBit.factory().eventBusProxyCreator();

        final EmployeeEventManager employeeEventManagerA =
                eventBusProxyCreator.createProxy(privateEventBusServerA, EmployeeEventManager.class);

        final EmployeeEventManager employeeEventManagerB =
                eventBusProxyCreator.createProxy(privateEventBusServerB, EmployeeEventManager.class);

        // This did not work. Not sure why?
//        /* ******************************************************************************/
//        /* LISTEN TO JMS CLIENT B and FORWARD to Event bus. **********************/
//        /* Listen to JMS client and push to B event bus ****************************/
//        employeeJsonQueueServerB.startListener(new ReceiveQueueListener<Employee>(){
//            @Override
//            public void receive(final Employee employee) {
//                System.out.println("HERE " + employee);
//                employeeEventManagerB.sendNewEmployee(employee);
//                System.out.println("LEFT " + employee);
//
//            }
//        });

        final PeriodicScheduler periodicScheduler = QBit.factory().createPeriodicScheduler(1);
        periodicScheduler.repeat(new Runnable() {
            @Override
            public void run() {
                final Employee employee = receiveQueueB.pollWait();
                if (employee != null) {
                    employeeEventManagerB.sendNewEmployee(employee);
                }
            }
        }, 50, TimeUnit.MILLISECONDS);

        final SalaryChangedChannel salaryChangedChannel = eventBusProxyCreator.createProxy(privateEventBusServerA, SalaryChangedChannel.class);

        /*
        Create your EmployeeHiringService but this time pass the private event bus.
        Note you could easily use Spring or Guice for this wiring.
         */
        final EmployeeHiringService employeeHiring = new EmployeeHiringService(employeeEventManagerA,
                salaryChangedChannel); //Runs on Server A



        /* Now create your other service POJOs which have no compile time dependencies on QBit. */
        final PayrollService payroll = new PayrollService(); //Runs on Server A
        final BenefitsService benefits = new BenefitsService();//Runs on Server A

        final VolunteerService volunteering = new VolunteerService();//Runs on Server B


        /** Employee hiring service. A. */
        ServiceQueue employeeHiringServiceQueue = serviceBuilder()
                .setServiceObject(employeeHiring)
                .setInvokeDynamic(false).build();

        /** Payroll service A. */
        ServiceQueue payrollServiceQueue = serviceBuilder()
                .setServiceObject(payroll)
                .setInvokeDynamic(false).build();

        /** Employee Benefits service. A. */
        ServiceQueue employeeBenefitsServiceQueue = serviceBuilder()
                .setServiceObject(benefits)
                .setInvokeDynamic(false).build();

        /** Community outreach program. B. */
        ServiceQueue volunteeringServiceQueue = serviceBuilder()
                .setServiceObject(volunteering)
                .setInvokeDynamic(false).build();


        /* Now wire in the event bus so it can fire events into the service queues.
        * For ServerA. */
        privateEventBusServerA.joinService(payrollServiceQueue);
        privateEventBusServerA.joinService(employeeBenefitsServiceQueue);


        /* Now wire in event B bus. */
        privateEventBusServerB.joinService(volunteeringServiceQueue);


        /* Start Server A bus. */
        privateEventBusServiceQueueA.start();


        /* Start Server B bus. */
        privateEventBusServiceQueueB.start();


        employeeHiringServiceQueue.start();
        volunteeringServiceQueue.start();
        payrollServiceQueue.start();
        employeeBenefitsServiceQueue.start();


        /** Now create the service proxy like before. */
        EmployeeHiringServiceClient employeeHiringServiceClientProxy =
                employeeHiringServiceQueue.createProxy(EmployeeHiringServiceClient.class);

        /** Call the hireEmployee method which triggers the other events. */
        employeeHiringServiceClientProxy.hireEmployee(new Employee("Lucas", 1));

        flushServiceProxy(employeeHiringServiceClientProxy);

        Sys.sleep(5_000);

    }

    interface EmployeeHiringServiceClient {
        void hireEmployee(final Employee employee);

    }


    @EventChannel
    interface SalaryChangedChannel {


        void salaryChanged(Employee employee, int newSalary);

    }


    interface EmployeeEventManager {

        @EventChannel(NEW_HIRE_CHANNEL)
        void sendNewEmployee(Employee employee);


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
        final SalaryChangedChannel salaryChangedChannel;

        public EmployeeHiringService(final EmployeeEventManager employeeEventManager,
                                     final SalaryChangedChannel salaryChangedChannel) {
            this.eventManager = employeeEventManager;
            this.salaryChangedChannel = salaryChangedChannel;
        }


        @QueueCallback(QueueCallbackType.EMPTY)
        private void noMoreRequests() {


            flushServiceProxy(salaryChangedChannel);
            flushServiceProxy(eventManager);
        }


        @QueueCallback(QueueCallbackType.LIMIT)
        private void hitLimitOfRequests() {

            flushServiceProxy(salaryChangedChannel);
            flushServiceProxy(eventManager);
        }


        public void hireEmployee(final Employee employee) {

            int salary = 100;
            System.out.printf("Hired employee %s\n", employee);

            //Does stuff to hire employee


            eventManager.sendNewEmployee(employee);
            salaryChangedChannel.salaryChanged(employee, salary);


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

    public static class PayrollService implements SalaryChangedChannel {

        @Override
        public void salaryChanged(Employee employee, int newSalary) {
            System.out.printf("DIRECT FROM CHANNEL SalaryChangedChannel Employee added to payroll  %s %d %d\n",
                    employee.getFirstName(), employee.getEmployeeId(), newSalary);

        }
    }
}
