package org.qbit.service;

import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.Invoker;
import org.boon.core.reflection.MethodAccess;

import org.qbit.queue.*;
import org.qbit.transforms.*;

import java.util.concurrent.TimeUnit;


/**
 * Created by Richard on 8/11/14.
 */
public class ServiceImpl extends BasicQueue<MethodCall> implements Service {

    private final Object service;

    private BeforeMethodCall beforeMethodCall = new NoOpBeforeMethodCall();


    private BeforeMethodCall beforeMethodCallAfterTransform = new NoOpBeforeMethodCall();


    private AfterMethodCall afterMethodCall = new NoOpAfterMethodCall();


    private AfterMethodCall afterMethodCallAfterTransform = new NoOpAfterMethodCall();

    private InputQueueListener<MethodCall> inputQueueListener = new NoOpInputMethodCallQueueListener();

    private final Queue<Response<Object>> responseQueue = new BasicQueue<Response<Object>>(1000,
            TimeUnit.SECONDS, 100);

    private  Transformer<Request, Object> requestObjectTransformer = new NoOpRequestTransform();

    private Transformer<Response, Response> responseObjectTransformer = new NoOpResponseTransformer();


    public ServiceImpl requestObjectTransformer(Transformer<Request, Object> requestObjectTransformer) {
        this.requestObjectTransformer = requestObjectTransformer;
        return this;
    }


    public ServiceImpl responseObjectTransformer(Transformer<Response, Response> responseObjectTransformer) {
        this.responseObjectTransformer = responseObjectTransformer;
        return this;
    }

    public ServiceImpl(final Object service, int waitTime, TimeUnit timeUnit, int batchSize,
                       InputQueueListener customMethodInvoker) {
        super(waitTime, timeUnit, batchSize);
        this.service = service;





        if (customMethodInvoker==null) {


            final ClassMeta classMeta = ClassMeta.classMeta(service.getClass());

            final MethodAccess queueLimit = classMeta.method("queueLimit");

            final MethodAccess queueEmpty = classMeta.method("queueEmpty");


            final MethodAccess queueShutdown = classMeta.method("queueShutdown");


            final MethodAccess queueIdle = classMeta.method("queueIdle");

            this.startListener(new InputQueueListener<MethodCall>() {
                @Override
                public void receive(MethodCall methodCall) {

                    inputQueueListener.receive(methodCall);

                    if (!beforeMethodCall.before(methodCall)) {
                        return;
                    }

                    final Object arg = requestObjectTransformer.transform(methodCall);

                    if (beforeMethodCallAfterTransform!=null) {
                        MethodCall transformedCall = MethodCallImpl.transformed(methodCall, arg);

                        if (!beforeMethodCallAfterTransform.before(transformedCall)) {
                            return;
                        }
                    }



                    final MethodAccess m = classMeta.method(methodCall.name());
                    if (m.returnType() == Void.class) {

                        Invoker.invokeFromObject(service, methodCall.name(), arg);
                    } else {
                        Object returnValue = Invoker.invokeFromObject(service, methodCall.name(), arg);

                        Response response = ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.name(), returnValue);


                        if (!afterMethodCall.after(methodCall, response)) {
                            return;
                        }

                        response = responseObjectTransformer.transform(response);


                        if (!afterMethodCallAfterTransform.after(methodCall, response)) {
                            return;
                        }




                        responseQueue.output().offer(response);
                    }
                }

                @Override
                public void empty() {


                    if (inputQueueListener!=null) {
                        inputQueueListener.empty();
                    }
                    if (queueEmpty!=null) {
                        queueEmpty.invoke(service);
                    }
                }

                @Override
                public void limit() {


                    if (inputQueueListener!=null) {
                        inputQueueListener.limit();
                    }

                    if (queueLimit!=null) {
                        queueLimit.invoke(service);
                    }
                }

                @Override
                public void shutdown() {



                    if (inputQueueListener!=null) {
                        inputQueueListener.shutdown();
                    }


                    if (queueShutdown!=null) {
                        queueShutdown.invoke(service);
                    }
                }

                @Override
                public void idle() {


                    if (inputQueueListener!=null) {
                        inputQueueListener.idle();
                    }

                    if (queueIdle!=null) {
                        queueIdle.invoke(service);
                    }
                }
            });

        } else {
            this.startListener(customMethodInvoker);
        }

    }

    public Queue<Response<Object>> responseQueue() {
        return responseQueue;
    }

    @Override
    public OutputQueue<MethodCall> requests() {
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


    public Object service() {
        return service;
    }


    public ServiceImpl beforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public ServiceImpl afterMethodCall(AfterMethodCall afterMethodCall) {
        this.afterMethodCall = afterMethodCall;
        return this;
    }

    public ServiceImpl beforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;
    }


    public ServiceImpl afterMethodCallAfterTransform(AfterMethodCall afterMethodCallAfterTransform) {
        this.afterMethodCallAfterTransform = afterMethodCallAfterTransform;
        return this;
    }
}
