package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.util.MultiMap;
import org.boon.Boon;
import org.boon.Lists;
import org.boon.Str;
import org.boon.core.Conversions;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class ServiceBundleImplTest {


    ServiceBundle serviceBundle;
    ServiceBundleImpl serviceBundleImpl;
    AdderService adderService;

    Factory factory;
    MultiMap<String, String> params = null;
    MethodCall<Object> call = null;
    boolean ok;


    ReceiveQueue<Response<Object>> responseReceiveQueue = null;

    Response<Object> response;

    Object responseBody = null;

    volatile  int callCount = 0;
    MockServiceInterface proxy;



    class MockService {
        public void method1() {
            callCount++;
        }
        public int method2() {
            ++callCount;
            return callCount;
        }
    }

    interface MockServiceInterface {
        void method1();
        void method2(Callback<Integer> count);
        void clientProxyFlush();

    }



    public static class AdderService {
        int sum;
        public int add(int a, int b) {

            puts("ADDER SERVICE CALLED", a, b);
            sum += (a+b);
            return a+b;
        }
    }


    @Before
    public void before() {

        factory = QBit.factory();

        final ServiceBundle bundle = new ServiceBundleBuilder().setAddress("/foo").build();
        serviceBundle = bundle;
        serviceBundleImpl = (ServiceBundleImpl) bundle;
        adderService = new AdderService();
        callCount = 0;

    }


    @Test
    public void test() {

        serviceBundle.addService(new MockService());
        proxy = serviceBundle.createLocalProxy(MockServiceInterface.class, "mockService");
        serviceBundle.startReturnHandlerProcessor();

        proxy.method1();
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount==1 || die();
    }

    @Test
    public void testCallback() throws Exception {


        serviceBundle.addService(new MockService());
        proxy = serviceBundle.createLocalProxy(MockServiceInterface.class, "mockService");
        serviceBundle.startReturnHandlerProcessor();

        AtomicInteger returnValue = new AtomicInteger();
        proxy.method2(integer -> {
            returnValue.set(integer);
        } );
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount==1 || die();

        ok = returnValue.get()==1 || die(returnValue.get());
    }


    @Test
    public void testAddress() throws Exception {

        Str.equalsOrDie("/foo", serviceBundle.address());

    }



    @Test
    public void testAddService() throws Exception {

        serviceBundle.addService("/adder", adderService);
        final List<String> endPoints = serviceBundle.endPoints();
        puts(endPoints);
        endPoints.contains("/foo/adder");
    }

    @Test
    public void testResponses() throws Exception {

        call = factory.createMethodCallByAddress("/foo/adder/add", "", Lists.list(1, 2), params);
        serviceBundle.addService("/adder", adderService);

        serviceBundle.call(call);

        serviceBundle.flushSends();

        Sys.sleep(1000);

        responseReceiveQueue = serviceBundle.responses().receiveQueue();

        serviceBundle.flush();

        Sys.sleep(200);

        response = responseReceiveQueue.pollWait();

        responseBody = response.body();

        int sum = Conversions.toInt(responseBody);

        Boon.equalsOrDie("Sum should be 3", 3, sum);

        serviceBundle.stop();
    }

    @Test
    public void testCall() throws Exception {

    }
}
