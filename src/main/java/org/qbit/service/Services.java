package org.qbit.service;

import org.qbit.service.method.impl.ServiceMethodCallHandlerImpl;
import org.qbit.transforms.JsonRequestBodyToArgListTransformer;
import org.qbit.transforms.JsonResponseTransformer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Richard on 8/26/14.
 */
public class Services {

    public static Service jsonService( Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        ServiceImpl serviceQueue = new ServiceImpl(service, waitTime, timeUnit, batchSize, new ServiceMethodCallHandlerImpl());
        serviceQueue.requestObjectTransformer(new JsonRequestBodyToArgListTransformer());
        serviceQueue.responseObjectTransformer(new JsonResponseTransformer());
        return serviceQueue;
    }

    public static Service regularService( Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        ServiceImpl serviceQueue = new ServiceImpl(service, waitTime, timeUnit, batchSize, new ServiceMethodCallHandlerImpl());
        return serviceQueue;
    }
}
