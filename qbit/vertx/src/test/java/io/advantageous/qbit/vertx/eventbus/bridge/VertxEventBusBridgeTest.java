package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.qbit.annotation.http.Bridge;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class VertxEventBusBridgeTest {


    /* address */
    final String address = "testservice";
    /* test service */
    TestService testService = new TestService();
    /* service builder */
    ServiceBuilder serviceBuilder;
    ServiceQueue serviceQueue;
    /* vertx event bus bridge to qbit. */
    VertxEventBusBridgeBuilder vertxEventBusBridgeBuilder = VertxEventBusBridgeBuilder.vertxEventBusBridgeBuilder();


    /* latch so we can test results coming back from bridge. */
    CountDownLatch countDownLatch;
    /* grab vertx from the bridge. */
    Vertx vertx;
    AtomicReference<AsyncResult<Message<Object>>> ref;

    private static String cleanJSON(final String json) {
        return json.replace("'", "\"");
    }

    @Before
    public void setup() {

    /* test service */
        testService = new TestService();

    /* service builder */
        serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setServiceObject(testService);
        ServiceQueue serviceQueue = serviceBuilder.build();

    /* vertx event bus bridge to qbit. */
        vertxEventBusBridgeBuilder = VertxEventBusBridgeBuilder.vertxEventBusBridgeBuilder();
        vertxEventBusBridgeBuilder.addBridgeAddress(address, TestService.class);
        vertxEventBusBridgeBuilder.setServiceQueue(serviceQueue);
        serviceQueue.startAll(); //startall not supported yet for bridge.
        vertxEventBusBridgeBuilder.build();


        /* latch so we can test results coming back from bridge. */
        countDownLatch = new CountDownLatch(1);

        /* grab vertx from the bridge. */
        vertx = vertxEventBusBridgeBuilder.getVertx();

        ref = new AtomicReference<>();
    }

    @Test
    public void test() throws Exception {

        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "test");
        /* calling using the event bus, and use latch to wait for the return. */
        vertx.eventBus().send(address, cleanJSON("['hello']"), deliveryOptions,
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();

        assertEquals("hello", testService.value.get());

        assertEquals("true", ref.get().result().body());

        vertx.close();
    }


    @Test
    public void testTwoArg() throws Exception {


        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "twoArg");

        vertx.eventBus().send(address, cleanJSON("[{'id':'rick'}, true]"), deliveryOptions,
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();


        assertEquals("true", ref.get().result().body());

        vertx.close();
    }

    @Test
    public void testSingleton() throws Exception {

        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "singleton");
        final String json = "[{'id':'rick'}]";

        vertx.eventBus().send(address, cleanJSON(json), deliveryOptions,
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();


        assertEquals("{'id':'rick'}".replaceAll("'", "\""), ref.get().result().body());

        vertx.close();
    }


    @Test
    public void oneWay() throws Exception {

        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "oneway");
        final String json = "[{'id':'rick'}]";

        vertx.eventBus().send(address, cleanJSON(json), deliveryOptions,
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();


        assertNotNull(testService.employee.get());

        assertEquals("rick", testService.employee.get().id);

        vertx.close();
    }


    @Test
    public void testList() throws Exception {


        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "list");
        final String json = "[[{'id':'rick'}, {'id':'geoff'}]]";


        vertx.eventBus().send(address, cleanJSON(json), deliveryOptions,
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();


        assertEquals("[{'id':'rick'},{'id':'geoff'}]".replaceAll("'", "\""), ref.get().result().body());

        vertx.close();
    }


    @Test
    public void testErrorFromService() throws Exception {


        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "error");
        final String json = "[[{'id':'rick'}, {'id':'geoff'}]]";


        vertx.eventBus().send(address, cleanJSON(json), deliveryOptions,
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });


        countDownLatch.await();


        assertTrue(ref.get().failed());
        vertx.close();
    }


    @Test
    public void testBadJSON() throws Exception {


        vertx.eventBus().send(address, ("{'method':'badJson', 'args':["),
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();


        assertTrue(ref.get().failed());

        vertx.close();
    }

    public static class Employee {
        final String id;

        public Employee(String id) {
            this.id = id;
        }
    }


    public static class TestService {
        final AtomicReference<String> value = new AtomicReference<>();

        final AtomicReference<Employee> employee = new AtomicReference<>();


        @Bridge
        public void oneway(final Employee employee) {
            this.employee.set(employee);
        }

        @Bridge
        public Employee singleton(final Employee employee) {
            return employee;
        }

        @Bridge
        public boolean twoArg(final Employee employee, boolean flag) {

            return employee.id.equals("rick") && flag;
        }

        @Bridge
        public List<Employee> list(final List<Employee> employees) {
            return employees;
        }

        @Bridge
        public List<Employee> error(final List<Employee> employees) {
            throw new IllegalStateException("PROBLEM");
        }


        @Bridge
        public boolean test(final String newValue) {

            System.out.println("HERE::" + newValue);
            value.set(newValue);

            return true;
        }
    }

}