package io.advantageous.qbit.queue.impl;


import io.advantageous.boon.primitive.Arry;
import io.advantageous.qbit.queue.QueueCallBackHandler;

import java.util.List;

/**
 * QueueCallBackHandlerHub contains a collections of Callbacks handlers that are treated as one.
 * This is useful for registering auto-health checks and such.
 * Created by rick on 6/6/15.
 */
public class QueueCallBackHandlerHub implements QueueCallBackHandler {

    final QueueCallBackHandler[] callBackHandlers;

    public QueueCallBackHandlerHub(QueueCallBackHandler... callBackHandlers) {
        this.callBackHandlers = callBackHandlers;
    }

    public QueueCallBackHandlerHub(List<QueueCallBackHandler> callBackHandlers) {

        this.callBackHandlers = Arry.array(QueueCallBackHandler.class, callBackHandlers);

    }


    @Override
    public void queueLimit() {
        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].queueLimit();
        }
    }

    @Override
    public void queueEmpty() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].queueEmpty();
        }
    }

    @Override
    public void queueInit() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].queueInit();
        }

    }

    @Override
    public void queueIdle() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].queueIdle();
        }

    }

    @Override
    public void queueShutdown() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].queueShutdown();
        }


    }

    @Override
    public void queueStartBatch() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].queueStartBatch();
        }

    }

    @Override
    public void beforeReceiveCalled() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].beforeReceiveCalled();
        }

    }


    @Override
    public void afterReceiveCalled() {

        for (int index = 0; index < callBackHandlers.length; index++) {
            callBackHandlers[index].afterReceiveCalled();
        }

    }
}
