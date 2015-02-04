package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventConsumer;
import io.advantageous.qbit.message.Event;

/**
 * Created by rhightower on 2/3/15.
 */
public class NoOpEventConsumer<T> implements EventConsumer<T>{
    @Override
    public void listen(Event<T> event) {

    }
}
