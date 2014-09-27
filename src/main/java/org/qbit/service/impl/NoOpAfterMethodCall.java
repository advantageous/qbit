package org.qbit.service.impl;

import org.qbit.message.MethodCall;
import org.qbit.message.Response;
import org.qbit.service.AfterMethodCall;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpAfterMethodCall implements AfterMethodCall {
    @Override
    public boolean after(MethodCall call, Response response) {
        return true;
    }
}
