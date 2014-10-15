package io.advantageous.qbit;

import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.impl.ServiceImpl;
import io.advantageous.qbit.service.impl.ServiceMethodCallHandlerImpl;
import io.advantageous.qbit.transforms.JsonRequestBodyToArgListTransformer;
import io.advantageous.qbit.transforms.JsonResponseTransformer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Richard on 8/26/14.
 */
public class Services {

    public static Service jsonService(final String name, Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        ServiceImpl serviceQueue = new ServiceImpl(null, name, service, waitTime, timeUnit, batchSize, new ServiceMethodCallHandlerImpl(), null);
        serviceQueue.requestObjectTransformer(new JsonRequestBodyToArgListTransformer());
        serviceQueue.responseObjectTransformer(new JsonResponseTransformer());
        return serviceQueue;
    }

    public static Service regularService(final String name, Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        return new ServiceImpl(null, name, service, waitTime, timeUnit, batchSize, new ServiceMethodCallHandlerImpl(), null);
    }
}
