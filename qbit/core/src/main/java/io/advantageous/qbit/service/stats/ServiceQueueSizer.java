package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.service.ServiceQueue;

public class ServiceQueueSizer {

    private ServiceQueue serviceQueue;

    public synchronized void setServiceQueue(ServiceQueue serviceQueue) {
        this.serviceQueue = serviceQueue;
    }

    public int requestSize() {
        return serviceQueue.requestQueue().size();
    }

    public int responseSize() {
        return serviceQueue.responseQueue().size();
    }

}
