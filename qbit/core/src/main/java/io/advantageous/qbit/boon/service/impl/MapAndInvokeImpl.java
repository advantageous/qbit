package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.primitive.Arry;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.reactive.Callback;

/**
 * Created by rick on 4/20/16.
 */
class MapAndInvokeImpl implements MapAndInvoke {
    private BoonServiceMethodCallHandler boonServiceMethodCallHandler;

    public MapAndInvokeImpl(BoonServiceMethodCallHandler boonServiceMethodCallHandler) {
        this.boonServiceMethodCallHandler = boonServiceMethodCallHandler;
    }

    public Response<Object> mapArgsAsyncHandlersAndInvoke(MethodCall<Object> methodCall, MethodAccess serviceMethod) {
        boolean hasHandlers = boonServiceMethodCallHandler.hasHandlers(methodCall, serviceMethod);
        Object returnValue;
        if (hasHandlers) {
            Object[] args = (Object[]) methodCall.body();
            Object[] argsList = prepareArgumentList(methodCall, serviceMethod.parameterTypes());
            extractHandlersFromArgumentList(serviceMethod, args, argsList);
            returnValue = serviceMethod.invoke(boonServiceMethodCallHandler.service, argsList);
        } else {
            final Object[] argsList = (Object[]) methodCall.body();
            returnValue = serviceMethod.invoke(boonServiceMethodCallHandler.service, argsList);
        }
        return boonServiceMethodCallHandler.response(serviceMethod, methodCall, returnValue);

    }


    private Object[] prepareArgumentList(final MethodCall<Object> methodCall, Class<?>[] parameterTypes) {
        final Object[] argsList = new Object[parameterTypes.length];

        for (int index = 0; index < parameterTypes.length; index++) {
            final Class<?> parameterType = parameterTypes[index];
            if (parameterType == Callback.class) {
                argsList[index] = boonServiceMethodCallHandler.createCallBackHandler(methodCall);
            }

        }
        return argsList;
    }


    private void extractHandlersFromArgumentList(MethodAccess method, Object[] args, Object[] argsList) {

        extractHandlersFromArgumentListArrayCase(method, args, argsList);

    }

    private void extractHandlersFromArgumentListArrayCase(MethodAccess method, Object[] array, Object[] argsList) {
        if (array.length - 1 == method.parameterTypes().length) {
            if (array[0] instanceof Callback) {
                array = Arry.slc(array, 1);
            }
        }
        for (int index = 0, arrayIndex = 0; index < argsList.length; index++, arrayIndex++) {
            final Object o = argsList[index];
            if (o instanceof Callback) {
                continue;
            }
            if (arrayIndex >= array.length) {
                break;
            }
            argsList[index] = array[arrayIndex];
        }
    }
}
