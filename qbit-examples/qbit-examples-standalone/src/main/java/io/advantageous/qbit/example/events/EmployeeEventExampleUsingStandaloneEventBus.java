package io.advantageous.qbit.example.events;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.Service;
import org.boon.core.Sys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * Created by rhightower on 2/5/15.
 */
public class EmployeeEventExampleUsingStandaloneEventBus {

    @Target({ ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OnEvent {


    /* The channel you want to listen to. */;
        String value() default "";

        /* The consume is the last object listening to this event.
           An event channel can have many subscribers but only one consume.
         */
        boolean consume() default false;


    }


    public static final String NEW_HIRE_CHANNEL = "com.mycompnay.employee.new";

    public static final String PAYROLL_ADJUSTMENT_CHANNEL = "com.mycompnay.employee.payroll";

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

    interface EmployeeHiringServiceClient {
        void hireEmployee(final Employee employee);

    }

    interface EventManagerClient {

        <T> void send(String channel, T event);

        <T> void sendArray(String channel, T... event);

        void clientProxyFlush();
    }


    public static class EmployeeHiringService {

        final EventManagerClient eventManager;

        public EmployeeHiringService (final EventManagerClient eventManager) {
            this.eventManager = eventManager;
        }

        void queueEmpty() {
            eventManager.clientProxyFlush();
        }

        void queueLimit() {
            eventManager.clientProxyFlush();
        }



        public void hireEmployee(final Employee employee) {

            int salary = 100;
            System.out.printf("Hired employee %s\n", employee);

            //Does stuff to hire employee

            //Sends events
            final EventManager eventManager = serviceContext().eventManager();
            eventManager.send(NEW_HIRE_CHANNEL, employee);
            eventManager.sendArray(PAYROLL_ADJUSTMENT_CHANNEL, employee, salary);


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

    public static void main(String... args) {


        /* Create you own private event bus. */
        EventManager privateEventBus = QBit.factory().createEventManager();

        /* Create a service queue for this event bus. */
        Service privateEventBusService = serviceBuilder()
                .setServiceObject(privateEventBus)
                .setInvokeDynamic(false).build().start();

        /*
         Create a proxy client for the queued event bus service.
         */
        EventManagerClient eventBus = privateEventBusService.createProxy(EventManagerClient.class);


        /*
        Create your EmployeeHiringService but this time pass the private event bus.
        Note you could easily use Spring or Guice for this wiring.
         */
        EmployeeHiringService employeeHiring = new EmployeeHiringService(eventBus);



        /* Now create your other service POJOs which have no compile time dependencies on QBit. */
        PayrollService payroll = new PayrollService();
        BenefitsService benefits = new BenefitsService();
        VolunteerService volunteering = new VolunteerService();



        /** Employee hiring service. */
        Service employeeHiringService = serviceBuilder()
                .setServiceObject(employeeHiring)
                .setInvokeDynamic(false).build().start();
        /** Payroll service */
        Service payrollService = serviceBuilder()
                .setServiceObject(payroll)
                .setInvokeDynamic(false).build().start();
        /** Employee Benefits service. */
        Service employeeBenefitsService = serviceBuilder()
                .setServiceObject(benefits)
                .setInvokeDynamic(false).build().start();
        /* Community outreach program. */
        Service volunteeringService = serviceBuilder()
                .setServiceObject(volunteering)
                .setInvokeDynamic(false).build().start();


        /* Now wire in the event bus so it can fire events into the service queues. */
        privateEventBus.joinService(payrollService);
        privateEventBus.joinService(employeeBenefitsService);
        privateEventBus.joinService(volunteeringService);

        /** Now create the service proxy like before. */
        EmployeeHiringServiceClient employeeHiringServiceClientProxy =
                employeeHiringService.createProxy(EmployeeHiringServiceClient.class);

        /** Call the hireEmployee method which triggers the other events. */
        employeeHiringServiceClientProxy.hireEmployee(new Employee("Rick", 1));

        flushServiceProxy(employeeHiringServiceClientProxy);

        Sys.sleep(5_000);

    }
}
