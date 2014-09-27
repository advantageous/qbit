package org.qbit.service.impl;

import org.boon.Lists;
import org.boon.Str;
import org.boon.collections.ConcurrentHashSet;
import org.qbit.Factory;
import org.qbit.GlobalConstants;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.queue.Queue;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.ReceiveQueueListener;
import org.qbit.queue.SendQueue;
import org.qbit.queue.impl.BasicQueue;
import org.qbit.service.Service;
import org.qbit.service.ServiceBundle;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class ServiceBundleImpl implements ServiceBundle {

    private Map<String, SendQueue<MethodCall<Object>>> serviceMapping
            = new ConcurrentHashMap<>();


    private Set<Service> services = new ConcurrentHashSet<>(10);

    final BasicQueue<MethodCall<Object>> methodQueue;


    final SendQueue<MethodCall<Object>> methodSendQueue;


    private Set<SendQueue<MethodCall<Object>>> sendQueues = new ConcurrentHashSet<>(10);

    private Queue<Response<Object>> responseQueue;

    private final String address;
    private Factory factory;

    public ServiceBundleImpl(String address, int batchSize, int pollRate, Factory factory) {
        if (address.endsWith("/")) {
            address = Str.slc(address, 0, -1);
        }

        this.address = address;

        this.factory = factory;
        this.responseQueue =  new BasicQueue<>("Response Queue " + address, pollRate,
                TimeUnit.MILLISECONDS, batchSize);

        this.methodQueue = new BasicQueue<>("Send Queue " + address, pollRate, TimeUnit.MILLISECONDS, batchSize);

        methodSendQueue = methodQueue.sendQueue();

        methodQueue.startListener(new ReceiveQueueListener<MethodCall<Object>>() {
            @Override
            public void receive(MethodCall<Object> item) {
                doCall(item);
            }

            @Override
            public void empty() {

                for (SendQueue<MethodCall<Object>> sendQueue : sendQueues) {
                    sendQueue.flushSends();
                }
            }

            @Override
            public void limit() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {

            }
        });

    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public void addService(String serviceAddress, Object object) {


        final Service service = factory.createService(address, serviceAddress,
                 object, responseQueue);

        services.add(service);

        final SendQueue<MethodCall<Object>> requests = service.requests();
        serviceMapping.put(serviceAddress, requests);
        serviceMapping.put(service.name(), requests);

        sendQueues.add(requests);

        final List<String> addresses = service.addresses(this.address);


        for (String addr : addresses) {


            SendQueue<MethodCall<Object>> methodCallSendQueue =
                    serviceMapping.get(service.name());

            serviceMapping.put(addr, methodCallSendQueue);
        }



    }

    @Override
    public ReceiveQueue<Response<Object>> responses() {
        return responseQueue.receiveQueue();
    }

    @Override
    public void call(MethodCall<Object> methodCall) {

        methodSendQueue.send(methodCall);

    }

    private void doCall(MethodCall<Object> methodCall) {


        SendQueue<MethodCall<Object>> sendQueue = null;

        if (  !Str.isEmpty(methodCall.objectName())  ) {
            sendQueue = serviceMapping.get(methodCall.objectName());
        }
        else if (  !Str.isEmpty(methodCall.address())  ) {
            sendQueue = serviceMapping.get(methodCall.address());
        }

        if (GlobalConstants.DEBUG && sendQueue==null) {
            die("SEND QUEUE IS NULL FOR METHOD", methodCall,
                    "\n", serviceMapping.keySet());
            return;
        }

        sendQueue.send(methodCall);

    }

    @Override
    public void flushSends() {
        this.methodSendQueue.flushSends();
    }

    public void stop() {
        methodQueue.stop();

        for (Service service : services) {
            service.stop();
        }

    }

    @Override
    public List<String> endPoints() {
        return Lists.list(serviceMapping.keySet());
    }
}
