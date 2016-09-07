package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.message.impl.ResponseImpl;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.PromiseHandle;

import java.util.List;

/**
 * Created by rick on 4/20/16.
 */
class MapAndInvokeDynamic implements MapAndInvoke {
    private BoonServiceMethodCallHandler boonServiceMethodCallHandler;

    public MapAndInvokeDynamic(BoonServiceMethodCallHandler boonServiceMethodCallHandler) {
        this.boonServiceMethodCallHandler = boonServiceMethodCallHandler;
    }

    public Response<Object> mapArgsAsyncHandlersAndInvoke(MethodCall<Object> serviceMethodCall, MethodAccess serviceMethod) {


        if (serviceMethod.parameterTypes().length == 0 && !(PromiseHandle.class.isAssignableFrom(serviceMethod.returnType()))) {

            Object returnValue = serviceMethod.invokeDynamicObject(boonServiceMethodCallHandler.service, null);
            return boonServiceMethodCallHandler.response(serviceMethod, serviceMethodCall, returnValue);

        }


        boolean hasHandlers = boonServiceMethodCallHandler.hasHandlers(serviceMethodCall, serviceMethod);

        Object returnValue;

        if (hasHandlers) {
            Object body = serviceMethodCall.body();
            List<Object> argsList = boonServiceMethodCallHandler.prepareArgumentList(serviceMethodCall, serviceMethod.parameterTypes());
            if (body instanceof List || body instanceof Object[]) {
                boonServiceMethodCallHandler.extractHandlersFromArgumentList(serviceMethodCall.callback(), serviceMethod, body, argsList);
            } else {
                if (argsList.size() == 1 && !(argsList.get(0) instanceof Callback)) {
                    argsList.set(0, body);
                }
            }
            returnValue = serviceMethod.invokeDynamicObject(boonServiceMethodCallHandler.service, argsList);

            if (returnValue instanceof Promise) {
                final Promise<Object> promise = ((Promise<Object>) returnValue);
                promise
                        .then(value -> {

                            boonServiceMethodCallHandler.responseSendQueue.send(ResponseImpl.response(serviceMethodCall, value));
                        })
                        .catchError(error -> {
                            boonServiceMethodCallHandler.responseSendQueue.send(ResponseImpl.error(serviceMethodCall, error));
                        }).invoke();

                return ServiceConstants.VOID;
            }

        } else {
            if (serviceMethodCall.body() instanceof List) {
                final List argsList = (List) serviceMethodCall.body();
                returnValue = serviceMethod.invokeDynamic(boonServiceMethodCallHandler.service, argsList.toArray(new Object[argsList.size()]));
            } else if (serviceMethodCall.body() instanceof Object[]) {
                final Object[] argsList = (Object[]) serviceMethodCall.body();
                returnValue = serviceMethod.invokeDynamic(boonServiceMethodCallHandler.service, argsList);
            } else {
                returnValue = serviceMethod.invokeDynamic(boonServiceMethodCallHandler.service, serviceMethodCall.body());
            }
        }


        return boonServiceMethodCallHandler.response(serviceMethod, serviceMethodCall, returnValue);

    }
}
