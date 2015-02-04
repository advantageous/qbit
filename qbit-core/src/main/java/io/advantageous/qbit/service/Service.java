package io.advantageous.qbit.service;

import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

import java.util.Collection;

/**
 * Created by Richard on 7/21/14.
 * @author rhightower
 */
public interface Service {

    /**
     * Queue so we can enqueue method calls onto a client.
     * A client send queue is not thread safe. Every thread that uses this client, needs its own SendQueue
     * this allows us to batch calls.
     *
     * @return send queue
     */
    SendQueue<MethodCall<Object>> requests();


    /*
    Queue to send events to the service.
     */
    SendQueue<Event<Object>> events();

    /**
     * Queue so we can receive method calls returns from a client
     * @return receive queue
     */
    ReceiveQueue<Response<Object>> responses();

    /**
     * Name of the client
     * @return name
     */
    String name();

    /**
     * Name of the client
     * @return name
     */
    String address();



    /**
     * Stop the client.
     */
    void stop();


    Service startCallBackHandler();

    /**
     * Return a list of addresses.
     * @param address address
     * @return addresses
     */
    Collection<String> addresses(String address);


    <T> T createProxy(Class<T> serviceInterface);


    void flush();
}
