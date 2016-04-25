package io.advantageous.qbit.admin;

import io.advantageous.qbit.queue.QueueCallBackHandler;

class ManagedServiceDefinition {

    private final String alias;
    private final Object serviceObject;
    private final QueueCallBackHandler[] queueCallBackHandlers;


    ManagedServiceDefinition(final String alias,
                             final Object serviceObject,
                             final QueueCallBackHandler... queueCallBackHandlers) {
        this.alias = alias;
        this.serviceObject = serviceObject;
        this.queueCallBackHandlers = queueCallBackHandlers;
    }

    public String getAlias() {
        return alias;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public QueueCallBackHandler[] getQueueCallBackHandlers() {
        return queueCallBackHandlers;
    }
}
