package io.advantageous.qbit.boon;


import java.lang.reflect.InvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import io.advantageous.qbit.util.Timer;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.qbit.service.method.impl.MethodCallImpl;
import org.boon.Str;


/**
 * Created by Richard on 9/30/14.
 *  @author Rick Hightower
 */
public class BoonServiceProxyFactory implements ServiceProxyFactory {

    private static volatile long generatedMessageId;


    @Override
    public  <T> T createProxyWithReturnAddress(final Class<T> serviceInterface,
                                               final String serviceName,
                                               String returnAddressArg,
                                               final EndPoint serviceBundle) {


        final String objectAddress = serviceBundle!=null
                ? Str.add(serviceBundle.address(),  "/" , serviceName) : "";


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

                final MethodCallImpl call = MethodCallImpl.method(messageId++,
                        address, returnAddress,
                        objectAddress, method.getName(), timestamp, args, null);



                if (serviceBundle!=null) {
                    serviceBundle.call(call);
                }

                return null;
            }
        };

        final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceInterface}, invocationHandler
        );


        return (T) o;


    }


    @Override
    public  <T> T createProxy(final Class<T> serviceInterface,
                              final String serviceName,
                              final EndPoint serviceBundle) {

        return createProxyWithReturnAddress(serviceInterface, serviceName, "", serviceBundle);

    }

}
