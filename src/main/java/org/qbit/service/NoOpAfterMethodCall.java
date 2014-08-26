package org.qbit.service;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpAfterMethodCall implements AfterMethodCall {
    @Override
    public boolean after(MethodCall call, Response response) {
        return true;
    }
}
