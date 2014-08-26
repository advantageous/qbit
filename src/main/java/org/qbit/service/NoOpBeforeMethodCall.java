package org.qbit.service;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpBeforeMethodCall implements BeforeMethodCall {
    @Override
    public boolean before(MethodCall call) {
        return true;
    }
}
