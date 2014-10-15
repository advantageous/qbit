package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.message.MethodCall;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpInputMethodCallQueueListener implements ReceiveQueueListener<MethodCall> {

    @Override
    public void receive(MethodCall item) {

    }

    @Override
    public void empty() {

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
}
