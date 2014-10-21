package io.advantageous.qbit.boon;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.proxy.ServiceProxyFactory;
import io.advantageous.qbit.service.EndPoint;
import org.boon.Str;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Created by Richard on 10/1/14.
 * @author Rick Hightower
 */
public class BoonJSONServiceFactory implements ServiceProxyFactory {

    private final Factory factory;


    private static volatile long generatedMessageId;


    public BoonJSONServiceFactory(Factory factory) {
        this.factory = factory;
    }

    @Override
    public <T> T createProxyWithReturnAddress(Class<T> serviceInterface, final String serviceName,
                                              String returnAddressArg,
                                              final EndPoint serviceBundle) {

        final String objectAddress = serviceBundle!=null
                ? Str.add(serviceBundle.address(), "/", serviceName) : "";


        if (!Str.isEmpty(returnAddressArg)) {
            returnAddressArg = Str.add(objectAddress, "/"+ UUID.randomUUID());
        }

        final String returnAddress = returnAddressArg;


        InvocationHandler invocationHandler = new InvocationHandler() {

            long timestamp = Timer.timer().now();
            int times = 10;
            long messageId = generatedMessageId+=1_000_000_000;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                times--;
                if (times == 0){
                    timestamp = Timer.timer().now();
                    times = 10;
                } else {
                    timestamp++;
                }


                final String address = Str.add(objectAddress, "/", method.getName());



                final MethodCall<Object> call = factory.createMethodCallToBeEncodedAndSent(messageId++,
                        address, returnAddress,
                        serviceName, method.getName(), timestamp, args, null);

                if (method.getName().equals("toString")) {
                    return "PROXY OBJECT";
                }

                serviceBundle.call(call);

                return null;
            }
        };

        final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceInterface}, invocationHandler
        );


        return (T) o;


    }

    @Override
    public <T> T createProxy(Class<T> serviceInterface, String serviceName, EndPoint serviceBundle) {
        return createProxyWithReturnAddress(serviceInterface, serviceName, "", serviceBundle);
    }
}
