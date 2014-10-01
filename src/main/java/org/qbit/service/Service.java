package org.qbit.service;

import org.qbit.message.Event;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.SendQueue;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Richard on 7/21/14.
 */
public interface Service {

    SendQueue<MethodCall<Object>> requests();
    ReceiveQueue<Response<Object>> responses();

    ReceiveQueue<Event> events();

    String name();


    void stop();

    Collection<String> addresses(String address);
}
