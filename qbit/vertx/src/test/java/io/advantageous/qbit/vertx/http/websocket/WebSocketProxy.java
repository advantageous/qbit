package io.advantageous.qbit.vertx.http.websocket;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertEquals;

public class WebSocketProxy {

    public static class Employee {
        final String id;
        public Employee(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    interface EmployeeService {
        void addEmployee(Callback<Employee>callback, Employee e);
    }

    public static class EmployeeServiceImpl implements EmployeeService {

        @Override
        public void addEmployee(Callback<Employee> callback, Employee e) {
            callback.returnThis(e);
        }
    }


    @Test
    public void test() throws Exception {


        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(9999)
                .addService(new EmployeeServiceImpl()).build().startServer();

        Sys.sleep(1000);

        final Client client = ClientBuilder.clientBuilder().setPort(9999).setHost("localhost").build().startClient();

        Sys.sleep(1000);
        final EmployeeService employeeService = client.createProxy(EmployeeService.class, "employeeserviceimpl");

        ServiceProxyUtils.flushServiceProxy(employeeService);

        Sys.sleep(1000);


        for (int index = 0; index < 20; index++) {
            final AtomicReference<Employee> ref = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);
            employeeService.addEmployee(employee -> {
                ref.set(employee);

                System.out.println(ref.get());
                latch.countDown();
            }, new Employee("rick"));

            latch.await(2, TimeUnit.SECONDS);
        }



        serviceEndpointServer.stop();
    }

}
