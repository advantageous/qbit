package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AfterMethodCallChain implements AfterMethodCall {

    private final List<AfterMethodCall> afterMethodCallList;

    public AfterMethodCallChain(List<AfterMethodCall> afterMethodCallList) {
        this.afterMethodCallList = Collections.unmodifiableList(afterMethodCallList);
    }

    public static AfterMethodCallChain afterMethodCallChain(final AfterMethodCall... calls) {
        return new AfterMethodCallChain(Arrays.asList(calls));

    }

    @Override
    public boolean after(final MethodCall call, final Response response) {

        for (final AfterMethodCall afterMethodCall : afterMethodCallList) {
            if (!afterMethodCall.after(call, response)) {
                return false;
            }
        }
        return true;
    }
}
