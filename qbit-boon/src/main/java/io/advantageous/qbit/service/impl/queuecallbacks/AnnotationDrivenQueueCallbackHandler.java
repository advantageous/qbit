package io.advantageous.qbit.service.impl.queuecallbacks;

import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;

/**
 * Created by rhightower on 2/10/15.
 */
public class AnnotationDrivenQueueCallbackHandler implements QueueCallBackHandler {

    public static final String QUEUE_CALLBACK_ANNOTATION_NAME = "QueueCallback";
    private final Object service;
    private MethodAccess queueStartBatch;
    private MethodAccess queueInit;
    private MethodAccess queueEmpty;
    private MethodAccess queueLimit;
    private MethodAccess queueShutdown;
    private MethodAccess queueIdle;
    private ClassMeta<Class<?>> classMeta;

    public AnnotationDrivenQueueCallbackHandler(Object service) {


        classMeta = (ClassMeta<Class<?>>)ClassMeta.classMeta(service.getClass());

        this.service = service;

        final Iterable<MethodAccess> methods = classMeta.methods();

        for (MethodAccess methodAccess : methods) {
            if (methodAccess.hasAnnotation(QUEUE_CALLBACK_ANNOTATION_NAME)) {
                processAnnotationForMethod(methodAccess);
            }

        }

    }

    private void processAnnotationForMethod(final MethodAccess methodAccess) {
        final AnnotationData annotation = methodAccess.annotation(QUEUE_CALLBACK_ANNOTATION_NAME);
        final String value = annotation.getValues().get("value").toString();
        final QueueCallbackType queueCallbackType = QueueCallbackType.valueOf(value);

        switch (queueCallbackType) {
            case IDLE:
                queueIdle = methodAccess;
                break;
            case SHUTDOWN:
                queueShutdown = methodAccess;
                break;
            case LIMIT:
                queueLimit = methodAccess;
                break;
            case INIT:
                queueLimit = methodAccess;
                break;
            case START_BATCH:
                queueStartBatch = methodAccess;
                break;
            case EMPTY:
                queueEmpty = methodAccess;
                break;
            case DYNAMIC:
                switch (methodAccess.name()) {
                    case "queueIdle":
                        queueIdle = methodAccess;
                        break;
                    case "queueShutdown":
                        queueShutdown = methodAccess;
                        break;
                    case "queueLimit":
                        queueLimit = methodAccess;
                        break;
                    case "queueInit":
                        queueInit = methodAccess;
                        break;
                    case "queueStartBatch":
                        queueIdle = methodAccess;
                        break;
                    case "queueEmpty":
                        queueEmpty = methodAccess;
                        break;
                }
                break;

        }

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
