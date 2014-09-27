package org.qbit.service;

import org.qbit.service.impl.ServiceImpl;
import org.qbit.service.impl.ServiceMethodCallHandlerImpl;
import org.qbit.transforms.JsonRequestBodyToArgListTransformer;
import org.qbit.transforms.JsonResponseTransformer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Richard on 8/26/14.
 */
public class Services {

    public static Service jsonService( final String name, Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        ServiceImpl serviceQueue = new ServiceImpl(name, service, waitTime, timeUnit, batchSize, new ServiceMethodCallHandlerImpl());
        serviceQueue.requestObjectTransformer(new JsonRequestBodyToArgListTransformer());
        serviceQueue.responseObjectTransformer(new JsonResponseTransformer());
        return serviceQueue;
    }

    public static Service regularService( final String name, Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        ServiceImpl serviceQueue = new ServiceImpl(name, service, waitTime, timeUnit, batchSize, new ServiceMethodCallHandlerImpl());
        return serviceQueue;
    }
}
