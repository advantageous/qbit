package io.advantageous.qbit.vertx.http.websocket;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;


public class WebSocketProxy {

    @Test
    public void test() throws Exception {


        final int port = PortUtils.findOpenPortStartAt(8080);
        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(port).setHost("localhost")
                .addService(new EmployeeServiceImpl()).build().startServerAndWait();

        final Client client = ClientBuilder.clientBuilder().setPort(port).setHost("localhost").build().startClient();

        final EmployeeService employeeService = client.createProxy(EmployeeService.class, "employeeserviceimpl");

        ServiceProxyUtils.flushServiceProxy(employeeService);


        AtomicLong counter = new AtomicLong();
        final CountDownLatch latch = new CountDownLatch(20);

        for (int index = 0; index < 20; index++) {
            final AtomicReference<Employee> ref = new AtomicReference<>();
            employeeService.addEmployee(employee -> {
                ref.set(employee);
                System.out.println(ref.get());
                counter.incrementAndGet();
                latch.countDown();
            }, new Employee("rick"));

        }


        latch.await(2, TimeUnit.SECONDS);
        assertEquals(20, counter.get());


        serviceEndpointServer.stop();
    }

    interface EmployeeService {
        void addEmployee(Callback<Employee> callback, Employee e);
    }

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

    public static class EmployeeServiceImpl implements EmployeeService {

        @Override
        public void addEmployee(Callback<Employee> callback, Employee e) {
            callback.returnThis(e);
        }
    }

}
