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

    SendQueue<MethodCall<Object>> requests();
    ReceiveQueue<Response<Object>> responses();

    ReceiveQueue<Event> events();

    String name();


    void stop();

    Collection<String> addresses(String address);
}
