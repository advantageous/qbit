package org.qbit.service.impl;

import org.qbit.Factory;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.ReceiveQueue;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Richard on 9/26/14.
 */
public class ServiceBundleImpl implements ServiceBundle {

    private Map<String, Service> serviceMapping
            = new ConcurrentHashMap<>();

    final String address;
    private Factory factory;

    public ServiceBundleImpl(String address, Factory factory) {
        this.address = address;
        this.factory = factory;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public void addService(String name, Object object) {

        final Service service = factory.createService(object);
        serviceMapping.put(name, service);

        service.addresses(address);



    }

    @Override
    public ReceiveQueue<Response<Object>> responses() {
        return null;
    }

    @Override
    public void call(MethodCall<Object> methodCall) {

    }
}
