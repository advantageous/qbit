package io.advantageous.qbit.service.impl.queuecallbacks;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;

/**
 * Created by rhightower on 2/10/15.
 */
public class DynamicQueueCallbackHandler implements QueueCallBackHandler {


    private final Object service;
    private final MethodAccess queueStartBatch;
    private final MethodAccess queueInit;
    private final MethodAccess queueEmpty;
    private final MethodAccess queueLimit;
    private final MethodAccess queueShutdown;
    private final MethodAccess queueIdle;
    private ClassMeta<Class<?>> classMeta;


    public DynamicQueueCallbackHandler(Object service) {
        this.service = service;
        classMeta = (ClassMeta<Class<?>>)  ClassMeta.classMeta(service.getClass());

        queueLimit = classMeta.method("queueLimit");
        queueEmpty = classMeta.method("queueEmpty");
        queueShutdown = classMeta.method("queueShutdown");
        queueIdle = classMeta.method("queueIdle");
        queueInit = classMeta.method("queueInit");
        queueStartBatch = classMeta.method("queueStartBatch");
    }


    @Override
    public void queueLimit() {
        if (queueLimit != null) {
            queueLimit.invoke(service);
        }


    }

    @Override
    public void queueEmpty() {
        if (queueEmpty != null) {
            queueEmpty.invoke(service);
        }

    }

    @Override
    public void queueInit() {
        if (queueInit!=null) {
            queueInit.invoke(this.service);
        }
    }

    @Override
    public void queueIdle() {
        if (queueIdle != null) {
            queueIdle.invoke(service);
        }
    }

    @Override
    public void queueShutdown() {
        if (queueShutdown != null) {
            queueShutdown.invoke(service);
        }

    }

    @Override
    public void queueStartBatch() {
        if (queueStartBatch!=null) {
            queueStartBatch.invoke(service);
        }

    }


}
