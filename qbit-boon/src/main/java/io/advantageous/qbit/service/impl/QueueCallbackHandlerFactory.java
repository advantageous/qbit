package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.service.impl.queuecallbacks.AnnotationDrivenQueueCallbackHandler;
import io.advantageous.qbit.service.impl.queuecallbacks.DynamicQueueCallbackHandler;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;

/**
 * Created by rhightower on 2/10/15.
 */
public class QueueCallbackHandlerFactory {


    public static final String QUEUE_CALLBACK_ANNOTATION_NAME = "QueueCallback";

    static QueueCallBackHandler createQueueCallbackHandler(Object service) {

        if (service instanceof QueueCallBackHandler) {
            return (QueueCallBackHandler) service;
        } else  {
            if (hasQueueCallbackAnnotations(service)) {
                return new AnnotationDrivenQueueCallbackHandler(service);
            } else {
                return new DynamicQueueCallbackHandler(service);
            }
        }

    }

    private static boolean hasQueueCallbackAnnotations(Object service) {


        ClassMeta<Class<?>> classMeta = (ClassMeta<Class<?>>) ClassMeta.classMeta(service.getClass());
        final Iterable<MethodAccess> methods = classMeta.methods();

        for (MethodAccess methodAccess : methods) {
            if (methodAccess.hasAnnotation(QUEUE_CALLBACK_ANNOTATION_NAME)) {
               return true;
            }

        }
        return false;
    }
}
