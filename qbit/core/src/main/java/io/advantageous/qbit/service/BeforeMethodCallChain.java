package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BeforeMethodCallChain implements BeforeMethodCall {


    private final List<BeforeMethodCall> beforeMethodCallList;

    public BeforeMethodCallChain(List<BeforeMethodCall> beforeMethodCallList) {
        this.beforeMethodCallList = Collections.unmodifiableList(beforeMethodCallList);
    }

    public static BeforeMethodCallChain beforeMethodCallChain(BeforeMethodCall... beforeMethodCalls) {
        return new BeforeMethodCallChain(Arrays.asList(beforeMethodCalls));
    }

    @Override
    public boolean before(final MethodCall call) {

        for (final BeforeMethodCall beforeMethodCall : beforeMethodCallList) {
            if (!beforeMethodCall.before(call)) {
                return false;
            }
        }
        return true;
    }
}
