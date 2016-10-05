package io.advantageous.qbit.vertx.http.websocket;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.Promises;
import org.junit.Test;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;


public class WebSocketProxy {

    @Test
    public void testDisconnect() {
        final int port = PortUtils.findOpenPortStartAt(7777);

        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(port).setHost("localhost")
                .addService(new EmployeeServiceImpl()).build().startServerAndWait();

        final Client client = ClientBuilder.clientBuilder().setPort(port).setHost("localhost").build().startClient();

        final EmployeeServiceClient employeeService = client.createProxy(EmployeeServiceClient.class, "employeeserviceimpl");

        Promise<Employee> promise = Promises.blockingPromise();

        employeeService.addEmployee(new Employee("rick")).asHandler().invokeWithPromise(promise.asHandler());

        ServiceProxyUtils.flushServiceProxy(employeeService);
        assertTrue(promise.asHandler().success());

        serviceEndpointServer.stop();

        promise = Promises.blockingPromise();

        employeeService.addEmployee(new Employee("rick")).asHandler().invokeWithPromise(promise.asHandler());

        assertTrue(promise.asHandler().failure());

        assertTrue(promise.asHandler().cause() instanceof ConnectException);

        client.stop();

    }


    @Test
    public void testSendGenericList() {

        final int port = PortUtils.findOpenPortStartAt(7777);

        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(port).setHost("localhost")
                .addService(new EmployeeServiceImpl()).build().startServerAndWait();

        final Client client = ClientBuilder.clientBuilder().setPort(port).setHost("localhost").build().startClient();

        final EmployeeServiceClient employeeService = client.createProxy(EmployeeServiceClient.class, "employeeserviceimpl");

        Promise<Boolean> promise = Promises.blockingPromise();

        employeeService.addEmployees(Arrays.asList(new Employee("rick"))).asHandler().invokeWithPromise(promise.asHandler());

        ServiceProxyUtils.flushServiceProxy(employeeService);

        boolean success = promise.asHandler().success();

        if (!success) {
            promise.asHandler().cause().printStackTrace();
        }
        assertTrue(success);


        serviceEndpointServer.stop();
        client.stop();

    }


    @Test
    public void testSendBoolean() {

        final int port = PortUtils.findOpenPortStartAt(7777);

        final ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder()
                .setPort(port).setHost("localhost")
                .addService(new EmployeeServiceImpl()).build().startServerAndWait();

        final Client client = ClientBuilder.clientBuilder().setPort(port).setHost("localhost").build().startClient();

        final EmployeeServiceClient employeeService = client.createProxy(EmployeeServiceClient.class, "employeeserviceimpl");

        Promise<Boolean> promise = Promises.blockingPromise();

        employeeService.sendBoolean(true).asHandler().invokeWithPromise(promise.asHandler());

        ServiceProxyUtils.flushServiceProxy(employeeService);

        boolean success = promise.asHandler().success();

        if (!success) {
            promise.asHandler().cause().printStackTrace();
        }
        assertTrue(success);
        assertTrue(promise.asHandler().get());



        Promise<Boolean> promise2 = Promises.blockingPromise();

        employeeService.sendBoolean(false).asHandler().invokeWithPromise(promise2.asHandler());

        ServiceProxyUtils.flushServiceProxy(employeeService);

        boolean success2 = promise2.asHandler().success();

        if (!success2) {
            promise2.asHandler().cause().printStackTrace();
        }
        assertTrue(success2);
        assertFalse(promise2.asHandler().get());


        serviceEndpointServer.stop();
        client.stop();

    }

    @Test
    public void test() throws Exception {


        final int port = PortUtils.findOpenPortStartAt(7777);
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

        void addEmployees(Callback<Boolean> callback, List<Employee> list);
    }


    interface EmployeeServiceClient {
        Promise<Employee> addEmployee(Employee e);

        Promise<Boolean> addEmployees(List<Employee> list);



        Promise<Boolean> sendBoolean(boolean sent);

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
            callback.resolve(e);
        }


        public void sendBoolean(Callback<Boolean> callback, boolean sent) {
            callback.resolve(sent);
        }

        @Override
        public void addEmployees(Callback<Boolean> callback, List<Employee> list) {

            try {
                if (list.size() > 0) {
                    final Employee employee = list.get(0);
                }
            } catch (Exception ex) {
                callback.reject(ex);
            }
            callback.resolve(true);
        }
    }

}
