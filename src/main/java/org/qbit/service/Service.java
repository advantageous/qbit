package org.qbit.service;

import org.qbit.queue.InputQueue;
import org.qbit.queue.OutputQueue;

/**
 * Created by Richard on 7/21/14.
 */
public interface Service {

    OutputQueue<Method> requests();
    InputQueue<Response<Object>> responses();

    InputQueue<Event> events();

}
