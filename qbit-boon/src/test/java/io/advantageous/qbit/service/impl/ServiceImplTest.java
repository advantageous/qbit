package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBuilder;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class ServiceImplTest {

    Service service;
    volatile  int callCount = 0;
    MockServiceInterface proxy;
    boolean ok;



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

    @Before
    public void setup() {
        service = new ServiceBuilder().setServiceObject(new MockService()).setInvokeDynamic(false).build();

        proxy = service.createProxy(MockServiceInterface.class);
        ok = true;


    }

    @Test
    public void test() {

        proxy.method1();
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount==1 || die();
    }

    @Test
    public void testCallback() throws Exception {



        service.startCallBackHandler();
        Sys.sleep(100);
        AtomicInteger returnValue = new AtomicInteger();
        proxy.method2(integer -> {
            returnValue.set(integer);
        } );
        proxy.clientProxyFlush();


        Sys.sleep(1000);

        ok = callCount==1 || die();

        ok = returnValue.get()==1 || die(returnValue.get());
    }


    @After
    public void tearDown() {
        callCount = 0;
        if (!ok) die();
    }

}