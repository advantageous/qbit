package io.advantageous.qbit.service.dispatchers;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.system.QBitSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ServiceWorkersTest {


    ServiceBundleBuilder serviceBundleBuilder;
    ServiceBundle serviceBundle;
    QBitSystemManager systemManager;
    IMyService myService;
    int numServices = 4;
    List<MyService> myServiceList;

    @Before
    public void setup() {
        systemManager = new QBitSystemManager();
        myServiceList = new ArrayList<>(numServices);

        for (int index = 0; index < numServices; index++) {
            myServiceList.add(new MyService());
        }

        final AtomicInteger serviceCount = new AtomicInteger();

        serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder().setSystemManager(systemManager);
        serviceBundle = serviceBundleBuilder.build();


        serviceBundle.addRoundRobinService("/myService", numServices, () -> myServiceList.get(serviceCount.getAndIncrement()));
        serviceBundle.start();

        myService = serviceBundle.createLocalProxy(IMyService.class, "/myService");

    }


    @Test
    public void test() {

        for (int index = 0; index < 100; index++) {
            myService.method1();
        }
        ServiceProxyUtils.flushServiceProxy(myService);


        myServiceList.forEach(myService1 -> {

            for (int index = 0; index < 10; index++) {
                Sys.sleep(10);

                if (myService1.count.get() == 25) break;
            }

        });


        myServiceList.forEach(myService1 -> {

            for (int index = 0; index < 10; index++) {

                assertEquals(25, myService1.count.get());
            }

        });

    }


    @Test
    public void testWithCallback() {

        AtomicBoolean fail = new AtomicBoolean();

        AtomicInteger count = new AtomicInteger();

        Callback<String> callback = CallbackBuilder.newCallbackBuilder()
                .withCallback(String.class, value -> {
                    if (value.equals("mom")) {
                        count.incrementAndGet();
                    }
                })
                .withErrorHandler(throwable -> {
                    throwable.printStackTrace();
                    fail.set(true);
                })
                .build(String.class);

        for (int index = 0; index < 100; index++) {
            myService.method2(callback);
        }
        ServiceProxyUtils.flushServiceProxy(myService);


        for (int index = 0; index < 10; index++) {
            Sys.sleep(10);
            if (count.get() == 100) break;
        }

        assertFalse(fail.get());

        myServiceList.forEach(myService1 -> {

            for (int index = 0; index < 10; index++) {

                assertEquals(25, myService1.count.get());
            }

        });

    }

    @After
    public void tearDown() {
        systemManager.shutDown();
    }

    public interface IMyService {

        void method1();


        void method2(Callback<String> callback);
    }

    public static class MyService {

        final AtomicInteger count = new AtomicInteger();

        public void method1() {

            count.incrementAndGet();
        }

        public void method2(final Callback<String> callback) {
            count.incrementAndGet();
            callback.accept("mom");
        }
    }


}