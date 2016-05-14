package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;

/**
 * Created by rick on 4/20/16.
 */
class MapAndInvokeDynamic implements MapAndInvoke {
    private BoonServiceMethodCallHandler boonServiceMethodCallHandler;

    public MapAndInvokeDynamic(BoonServiceMethodCallHandler boonServiceMethodCallHandler) {
        this.boonServiceMethodCallHandler = boonServiceMethodCallHandler;
    }

    public Response<Object> mapArgsAsyncHandlersAndInvoke(MethodCall<Object> methodCall, MethodAccess serviceMethod) {

        if (serviceMethod.parameterTypes().length == 0) {

            Object returnValue = serviceMethod.invokeDynamicObject(boonServiceMethodCallHandler.service, null);
            return boonServiceMethodCallHandler.response(serviceMethod, methodCall, returnValue);

        }


        boolean hasHandlers = boonServiceMethodCallHandler.hasHandlers(methodCall, serviceMethod);

        Object returnValue;

        if (hasHandlers) {
            Object body = methodCall.body();
            List<Object> argsList = boonServiceMethodCallHandler.prepareArgumentList(methodCall, serviceMethod.parameterTypes());
            if (body instanceof List || body instanceof Object[]) {
                boonServiceMethodCallHandler.extractHandlersFromArgumentList(methodCall.callback(), serviceMethod, body, argsList);
            } else {
                if (argsList.size() == 1 && !(argsList.get(0) instanceof Callback)) {
                    argsList.set(0, body);
                }
            }
            returnValue = serviceMethod.invokeDynamicObject(boonServiceMethodCallHandler.service, argsList);

        } else {
            if (methodCall.body() instanceof List) {
                final List argsList = (List) methodCall.body();
                returnValue = serviceMethod.invokeDynamic(boonServiceMethodCallHandler.service, argsList.toArray(new Object[argsList.size()]));
            } else if (methodCall.body() instanceof Object[]) {
                final Object[] argsList = (Object[]) methodCall.body();
                returnValue = serviceMethod.invokeDynamic(boonServiceMethodCallHandler.service, argsList);
            } else {
                returnValue = serviceMethod.invokeDynamic(boonServiceMethodCallHandler.service, methodCall.body());
            }
        }


        return boonServiceMethodCallHandler.response(serviceMethod, methodCall, returnValue);

    }
}
