package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

interface MapAndInvoke {
    Response<Object> mapArgsAsyncHandlersAndInvoke(MethodCall<Object> methodCall, MethodAccess serviceMethod);
}
