package io.advantageous.qbit;

import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.service.impl.ServiceImpl;
import io.advantageous.qbit.transforms.JsonRequestBodyToArgListTransformer;
import io.advantageous.qbit.transforms.JsonResponseTransformer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Richard on 8/26/14.
 */
public class Services {

    public static Service jsonService(final String name, Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        JsonMapper mapper = new BoonJsonMapper();


        ServiceImpl serviceQueue = new ServiceImpl(null, name, service, new QueueBuilder(), new BoonServiceMethodCallHandler(true), null, true);
        serviceQueue.requestObjectTransformer(new JsonRequestBodyToArgListTransformer(mapper));
        serviceQueue.responseObjectTransformer(new JsonResponseTransformer(mapper));
        return serviceQueue;
    }

    public static Service regularService(final String name, Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        return new ServiceImpl(null, name, service, new QueueBuilder(), new BoonServiceMethodCallHandler(true), null, true);
    }
}
