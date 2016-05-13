package io.advantageous.qbit.boon.service.impl;

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reakt.Reakt;
import io.advantageous.reakt.Invokable;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.impl.BasePromise;

public class InvokePromiseWithSendQueue extends BasePromise<Object> implements Invokable {

    private final MethodCallBuilder methodCallBuilder;
    private final SendQueue<MethodCall<Object>> sendQueue;
    private final BeforeMethodSent beforeMethodSent;
    private boolean invoked;

    public InvokePromiseWithSendQueue(SendQueue<MethodCall<Object>> sendQueue, MethodCallBuilder methodCallBuilder,
                                      BeforeMethodSent beforeMethodSent) {
        this.sendQueue = sendQueue;
        this.methodCallBuilder = methodCallBuilder;
        this.beforeMethodSent = beforeMethodSent;
    }

    @Override
    public Promise<Object> invoke() {
        if (invoked) {
            throw new IllegalStateException("Can't call promise invoke twice.");
        }
        invoked = true;
        methodCallBuilder.setCallback(Reakt.convertPromise(this));
        if (beforeMethodSent != null) beforeMethodSent.beforeMethodSent(methodCallBuilder);
        sendQueue.send(methodCallBuilder.build());
        return this;
    }

    @Override
    public boolean isInvokable() {
        return true;
    }
}
