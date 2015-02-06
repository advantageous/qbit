package io.advantageous.qbit.example.events;

import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.Service;
import org.boon.core.Sys;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * Created by rhightower on 2/4/15.
 */
public class EmployeeEventExampleUsingSystemEventBus {

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

    public static class EmployeeHiringService {



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


        EmployeeHiringService employeeHiring = new EmployeeHiringService();
        PayrollService payroll = new PayrollService();
        BenefitsService benefits = new BenefitsService();
        VolunteerService volunteering = new VolunteerService();


        Service employeeHiringService = serviceBuilder()
                .setServiceObject(employeeHiring)
                .setInvokeDynamic(false).build().start();



        Service payrollService = serviceBuilder()
                .setServiceObject(payroll)
                .setInvokeDynamic(false).build().start();


        Service employeeBenefitsService = serviceBuilder()
                .setServiceObject(benefits)
                .setInvokeDynamic(false).build().start();


        Service volunteeringService = serviceBuilder()
                .setServiceObject(volunteering)
                .setInvokeDynamic(false).build().start();

        EmployeeHiringServiceClient employeeHiringServiceClientProxy = employeeHiringService.createProxy(EmployeeHiringServiceClient.class);

        employeeHiringServiceClientProxy.hireEmployee(new Employee("Rick", 1));

        flushServiceProxy(employeeHiringServiceClientProxy);

        Sys.sleep(5_000);

    }
}
