package org.qbit.service;

import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.Invoker;
import org.boon.core.reflection.MethodAccess;

import org.qbit.queue.*;
import org.qbit.transforms.JsonRequestBodyToArgListTransformer;
import org.qbit.transforms.Transformer;

import java.util.concurrent.TimeUnit;


/**
 * Created by Richard on 8/11/14.
 */
public class ServiceImpl extends BasicQueue<Method> implements Service {

    private final Object service;

    private final Queue<Response<Object>> responseQueue = new BasicQueue<Response<Object>>(1000,
            TimeUnit.SECONDS, 100);

    private final Transformer<Request, Object> requestObjectTransformer = new JsonRequestBodyToArgListTransformer();





    public ServiceImpl(final Object service, int waitTime, TimeUnit timeUnit, int batchSize) {
        super(waitTime, timeUnit, batchSize);
        this.service = service;


        final ClassMeta classMeta = ClassMeta.classMeta(service.getClass());

        this.startListener(new InputQueueListener<Method>() {
            @Override
            public void receive(Method methodCall) {
                final Object fromJson = requestObjectTransformer.transform(methodCall);
                final MethodAccess m = classMeta.method(methodCall.name());
                if (m.returnType() == Void.class) {

                    Invoker.invokeFromObject(service, methodCall.name(), fromJson);
                } else {
                    Object returnValue = Invoker.invokeFromObject(service, methodCall.name(), fromJson);
                    responseQueue.output().offer(ResponseImpl.response(methodCall.id(), methodCall.name(), returnValue));
                }
            }

            @Override
            public void empty() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void idle() {

            }
        });
    }

    public Queue<Response<Object>> responseQueue() {
        return responseQueue;
    }

    @Override
    public OutputQueue<Method> requests() {
        return this.output();
    }

    @Override
    public InputQueue<Response<Object>> responses() {
        return this.responseQueue.input();
    }

    @Override
    public InputQueue<Event> events() {
        return null;
    }
}
