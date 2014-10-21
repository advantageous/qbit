package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.service.AfterMethodCall;

/**
 * Default after call handler.
 * Created by Richard on 8/26/14.
 */
public class NoOpAfterMethodCall implements AfterMethodCall {
    @Override
    public boolean after(MethodCall call, Response response) {
        return true;
    }
}
