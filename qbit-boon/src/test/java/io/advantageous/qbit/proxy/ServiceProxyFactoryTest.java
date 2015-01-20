package io.advantageous.qbit.proxy;

import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.core.Handler;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/30/14.
 */
public class ServiceProxyFactoryTest {

    static {
        RegisterBoonWithQBit.registerBoonWithQBit();

    }
    volatile boolean ok;

    List<MethodCall<Object>> calls = new ArrayList<>();

    ServiceBundle serviceBundle = new ServiceBundle() {
        Factory factory = QBit.factory();
        @Override
        public String address() {
            return "";
        }

        @Override
        public void addService(String address, Object object) {

        }

        @Override
        public void addService(Object object) {

        }

        @Override
        public Queue<Response<Object>> responses() {
            return null;
        }


        @Override
        public void call(MethodCall<Object> methodCall) {
            calls.add(methodCall);
        }

        @Override
        public void call(List<MethodCall<Object>> methodCalls) {
            calls.addAll(methodCalls);
        }

        @Override
        public void flush() {

        }

        @Override
        public void flushSends() {

        }

        @Override
        public void stop() {

        }

        @Override
        public List<String> endPoints() {
            return null;
        }

        @Override
        public void startReturnHandlerProcessor(ReceiveQueueListener<Response<Object>> listener) {

        }

        @Override
        public void startReturnHandlerProcessor() {

        }


        @Override
        public <T> T createLocalProxy(Class<T> serviceInterface, String serviceName) {
            return factory.createLocalProxy(serviceInterface, serviceName, this);
        }
    };

    public static interface SomeInterface {
        void method1();

        void method2(String hi, int amount);


        String method3(String hi, int amount);
    }


    @Test
    public void testProxySimpleNoArg() {
        final SomeInterface myService = serviceBundle.createLocalProxy(SomeInterface.class, "myService");
        myService.method1();


    }

    @Test
    public void testProxySimpleTwoArg() {
        final SomeInterface myService = serviceBundle.createLocalProxy(SomeInterface.class, "myService");

        calls.clear();

        myService.method2("Hello", 5);


    }

    @Test
    public void testProxySimpleServiceBundle() {
        final SomeInterface myService = serviceBundle.createLocalProxy(SomeInterface.class, "myService");

        calls.clear();

        myService.method2("Hello", 5);

        boolean found = false;

        for (MethodCall<Object> call : calls) {
            if (call.name().equals("method2")) {
                final Object body = call.body();
                puts(body);
                ok = body != null || die();
                ok = body.getClass().isArray() || die();
                ok = body.getClass() == Object[].class;
                Object[] args = (Object[]) body;
                String arg1 = (String) args[0];
                ok = arg1.equals("Hello") || die();
                int i = (int) args[1];
                ok = i == 5 || die();
                found=true;
            }
        }

        ok = found || die();



    }

    boolean calledMethod1;
    boolean calledMethod2;

    @Test
    public void callingActualService() {




        SomeInterface myService = new SomeInterface() {
            @Override
            public void method1() {

            }

            @Override
            public void method2(String hi, int amount) {

            }

            @Override
            public String method3(String hi, int amount) {
                return null;
            }
        };


        final Factory factory  = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/root");


        bundle.addService(myService);


        final SomeInterface myServiceProxy = bundle.createLocalProxy(SomeInterface.class, "myService");

        myServiceProxy.method2("hi", 5);
        Sys.sleep(1000);

    }


    @Test
    public void callingActualServiceWithReturn() {




        @RequestMapping ("myService")
        class MyServiceClass implements SomeInterface {
            @Override
            public void method1() {

            }

            @Override
            public void method2(String hi, int amount) {

            }

            @Override
            public String method3(String hi, int amount) {
                return "Hi" + hi + " " + amount;
            }
        }


        SomeInterface myService = new MyServiceClass();


        final Factory factory  = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/root");


        bundle.addService(myService);

        final ReceiveQueue<Response<Object>> responseReceiveQueue = bundle.responses().receiveQueue();


        final SomeInterface myServiceProxy = bundle.createLocalProxy(
                SomeInterface.class,
                "myService");

        myServiceProxy.method3("hi", 5);
        bundle.flush();
        Sys.sleep(1000);

        final Response<Object> objectResponse = responseReceiveQueue.pollWait();
        objectResponse.address();
        puts (objectResponse.body());
        ok = "Hihi 5".equals(objectResponse.body()) || die();

    }



    public static interface MyServiceInterfaceForClient {

        void method1();

        void method2(String hi, int amount);

        void method3(Callback<String> handler, String hi, int amount);
    }

    @Test
    public void callingActualServiceWithReturnDifferentInterfaces() {




        @RequestMapping("myService")
        class MyServiceClass implements SomeInterface {
            @Override
            public void method1() {

            }

            @Override
            public void method2(String hi, int amount) {

            }

            @Override
            public String method3(String hi, int amount) {
                return "Hi" + hi + " " + amount;
            }
        }




        SomeInterface myService = new MyServiceClass();


        final Factory factory  = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/root");


        bundle.addService(myService);
        bundle.startReturnHandlerProcessor();



        final MyServiceInterfaceForClient myServiceProxy = bundle.createLocalProxy(
                MyServiceInterfaceForClient.class,
                "myService");

        ok = false;
        Callback<String> returnHandler = new Callback<String>() {
            @Override
            public void accept(String returnValue) {

                puts("We got", returnValue);

                ok = "Hihi 5".equals(returnValue);

            }
        };
        myServiceProxy.method3(returnHandler, "hi", 5);
        bundle.flush();
        Sys.sleep(1000);

        ok = ok || die();


    }


    public static interface ClientInterfaceThrowsException {

        public void methodThrowsExceptionIf5(Callback<String> arg, String hi, int amount);
    }

    @Test
    public void callingServicesThatThrowExceptions() {




        @RequestMapping ("myService")
        class MyServiceClass  {


            public String methodThrowsExceptionIf5(String hi, int amount) {

                if (amount == 5) {
                    return die(String.class, "Hi " + hi + " " + amount);
                } else {
                    return "Hi " + hi + " " + amount;
                }
            }
        }





        MyServiceClass myService = new MyServiceClass();


        final Factory factory  = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/root");


        bundle.addService(myService);
        bundle.startReturnHandlerProcessor();



        final ClientInterfaceThrowsException myServiceProxy = bundle.createLocalProxy(
                ClientInterfaceThrowsException.class,
                "myService");

        ok = false;

        AtomicBoolean wasError = new AtomicBoolean();
        final Callback<String> handler = new Callback<String>() {

            @Override
            public void accept(String s) {

                ok=true;
            }

            @Override
            public void onError(Throwable error) {

                puts("We got", error.getMessage());

                ok = "Hi hi 5".equals(error.getMessage());
                wasError.set(true);
            }
        };

        myServiceProxy.methodThrowsExceptionIf5(handler, "hi", 6);
        bundle.flush();
        Sys.sleep(1000);

        ok = ok || die();

        ok = !wasError.get() || die();

        ok = false;
        wasError.set(false);


        myServiceProxy.methodThrowsExceptionIf5(handler, "hi", 5);
        bundle.flush();
        Sys.sleep(1000);
        ok = ok || die();

        ok = wasError.get() || die();



    }
}
