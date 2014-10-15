package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.BeforeMethodCall;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpBeforeMethodCall implements BeforeMethodCall {
    @Override
    public boolean before(MethodCall call) {
        return true;
    }
}
