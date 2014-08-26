package org.qbit.service;

import org.qbit.queue.InputQueueListener;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpInputMethodCallQueueListener implements InputQueueListener<MethodCall> {

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
