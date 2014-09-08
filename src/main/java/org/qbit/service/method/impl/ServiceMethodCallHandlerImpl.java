package org.qbit.service.method.impl;

import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.Invoker;
import org.boon.core.reflection.MethodAccess;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.service.ServiceConstants;
import org.qbit.service.ServiceMethodHandler;

/**
* Created by Richard on 9/8/14.
*/
public class ServiceMethodCallHandlerImpl implements ServiceMethodHandler {
    private  ClassMeta classMeta;
    private  Object service;
    private  MethodAccess queueEmpty;
    private  MethodAccess queueLimit;
    private  MethodAccess queueShutdown;
    private  MethodAccess queueIdle;


    public void init(Object service) {

        this.service = service;

        classMeta = ClassMeta.classMeta(service.getClass());
        queueLimit = classMeta.method("queueLimit");
        queueEmpty = classMeta.method("queueEmpty");
        queueShutdown = classMeta.method("queueShutdown");
        queueIdle = classMeta.method("queueIdle");

    }

    public Response<Object> receive(MethodCall<Object> methodCall, Object arg) {



        final MethodAccess m = classMeta.method(methodCall.name());
        if (m.returnType() == Void.class) {

            Invoker.invokeFromObject(service, methodCall.name(), arg);
            return ServiceConstants.VOID;
        } else {
            Object returnValue = Invoker.invokeFromObject(service, methodCall.name(), arg);

            Response<Object> response = ResponseImpl.response(methodCall.id(), methodCall.timestamp(), methodCall.name(), returnValue);

            return response;
        }
    }


    @Override
    public void receive(MethodCall<Object> item) {

    }


    @Override
    public void empty() {


        if (queueEmpty != null) {
            queueEmpty.invoke(service);
        }
    }

    @Override
    public void limit() {


        if (queueLimit != null) {
            queueLimit.invoke(service);
        }
    }

    @Override
    public void shutdown() {



        if (queueShutdown != null) {
            queueShutdown.invoke(service);
        }
    }

    @Override
    public void idle() {


        if (queueIdle != null) {
            queueIdle.invoke(service);
        }
    }

}
