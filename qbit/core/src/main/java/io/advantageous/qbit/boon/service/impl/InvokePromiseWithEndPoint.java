package io.advantageous.qbit.boon.service.impl;

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.reakt.Reakt;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.reakt.Invokable;
import io.advantageous.reakt.promise.impl.BasePromise;

public class InvokePromiseWithEndPoint extends BasePromise<Object> implements Invokable {

    private final MethodCallBuilder methodCallBuilder;
    private final EndPoint endPoint;
    private final BeforeMethodSent beforeMethodSent;
    private boolean invoked;


    InvokePromiseWithEndPoint(EndPoint endPoint, MethodCallBuilder methodCallBuilder, BeforeMethodSent beforeMethodSent) {
        this.endPoint = endPoint;
        this.methodCallBuilder = methodCallBuilder;
        this.beforeMethodSent = beforeMethodSent;
    }

    @Override
    public void invoke() {
        if (invoked) {
            throw new IllegalStateException("Can't call promise invoke twice.");
        }
        invoked = true;
        methodCallBuilder.setCallback(Reakt.convertPromise(this));
        beforeMethodSent.beforeMethodSent(methodCallBuilder);
        endPoint.call(methodCallBuilder.build());
    }

    @Override
    public boolean isInvokable() {
        return true;
    }
}
