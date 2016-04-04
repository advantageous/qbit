package io.advantageous.qbit.example.callback;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RequestMapping("/")
public class CompanyRestServiceUsingReactor extends BaseService {


    private final Logger logger = LoggerFactory.getLogger(CompanyRestService.class);
    private final EmployeeService employeeService;

    public CompanyRestServiceUsingReactor(Reactor reactor,
                                          Timer timer,
                                          StatsCollector statsCollector,
                                          EmployeeService employeeService) {
        super(reactor, timer, statsCollector);
        this.employeeService = employeeService;
        reactor.addServiceToFlush(employeeService);
    }



    @RequestMapping("/emap")
    public void employeeMap(final Callback<Map<String, Employee>> empMapCallback) {

        final CallbackBuilder callbackBuilder =  super.reactor.callbackBuilder();
        callbackBuilder.wrap(empMapCallback); //Forward to error handling, timeout, and callback defined in empMapCallback
        employeeService.getEmployeesAsMap(callbackBuilder.build());

    }


    @RequestMapping("/emap2")
    public void employeeMap2(final Callback<Map<String, Employee>> empMapCallback) {

        final CallbackBuilder callbackBuilder = super.reactor.callbackBuilder();
        callbackBuilder.delegate(empMapCallback); //Forward to error handling and timeout defined in empMapCallback

        callbackBuilder.withMapCallback(String.class, Employee.class, employeeMap -> {
            logger.info("GET MAP {}", employeeMap);
            empMapCallback.returnThis(employeeMap);
        });
        employeeService.getEmployeesAsMap(callbackBuilder.build());

    }


    @RequestMapping("/emap3")
    public void employeeMap3(final Callback<Map<String, Employee>> empMapCallback) {

        final CallbackBuilder callbackBuilder = super.reactor.callbackBuilder();
        // Forward to error handling and timeout defined in empMapCallback, but install some additional logging for
        // timeout and error handling that associates the error and timeout handling with this call.
        callbackBuilder.delegateWithLogging(empMapCallback, logger, "employeeMap3");
        callbackBuilder.withMapCallback(String.class, Employee.class, employeeMap -> {
            logger.info("GET MAP {}", employeeMap);
            empMapCallback.returnThis(employeeMap);
        });
        employeeService.getEmployeesAsMap(callbackBuilder.build());
    }


    @RequestMapping("/elist")
    public void employeeList(final Callback<List<Employee>> empListCallback) {

        final CallbackBuilder callbackBuilder = super.reactor.callbackBuilder();
        // Forward to error handling and timeout defined in empMapCallback, but install some additional logging for
        // timeout and error handling that associates the error and timeout handling with this call.
        callbackBuilder.delegateWithLogging(empListCallback, logger, "employeeList");
        callbackBuilder.withListCallback(Employee.class, employeeList -> {
            logger.info("GET List {}", employeeList);
            empListCallback.returnThis(employeeList);
        });
        employeeService.getEmployeesAsList(callbackBuilder.build());
    }


    @RequestMapping("/find")
    public void findEmployee(final Callback<Employee> employeeCallback,
                             @RequestParam("name") final String name) {

        final long startTime = super.time;

        final CallbackBuilder callbackBuilder = super.reactor.callbackBuilder();
        callbackBuilder.delegateWithLogging(employeeCallback, logger, "employeeMap3");
        callbackBuilder.withOptionalCallback(Employee.class, employeeOptional -> {


            /** Record timing. */
            super.recordTiming("findEmployee", time - startTime);

            if (employeeOptional.isPresent()) {
                /* Increment count of employees found. */
                super.incrementCount("employeeFound");
                employeeCallback.returnThis(employeeOptional.get());
            } else {
                /* Increment count of employees not found. */
                super.incrementCount("employeeNotFound");
                employeeCallback.onError(new Exception("Employee not found"));
            }
        });
        employeeService.findEmployeeByName(callbackBuilder.build(), name);
    }


    public static void main(final String... args) throws Exception {

        /** Create a ManagedServiceBuilder which simplifies QBit wiring. */
        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");
        managedServiceBuilder.enableLoggingMappedDiagnosticContext();

        /** Create a service queue for the employee service. */
        final ServiceQueue employeeServiceQueue = managedServiceBuilder.createServiceBuilderForServiceObject(
                new EmployeeServiceImpl()).buildAndStartAll();

        /** Add a CompanyRestService passing it a client proxy to the employee service. */
        managedServiceBuilder.addEndpointService(
                new CompanyRestServiceUsingReactor(
                        ReactorBuilder.reactorBuilder().setDefaultTimeOut(10).setTimeUnit(TimeUnit.SECONDS).build(),
                        Timer.timer(),
                        managedServiceBuilder.createStatsCollector(),
                        employeeServiceQueue.createProxy(EmployeeService.class)));

        /** Start the server. */
        managedServiceBuilder.startApplication();

    }
}
