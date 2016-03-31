package io.advantageous.qbit.client;

import io.advantageous.qbit.message.MethodCallBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BeforeMethodSentChain implements BeforeMethodSent {


    private final List<BeforeMethodSent> beforeMethodCallSentList;

    public BeforeMethodSentChain(List<BeforeMethodSent> beforeMethodCallSentList) {
        this.beforeMethodCallSentList = Collections.unmodifiableList(beforeMethodCallSentList);
    }

    public static BeforeMethodSentChain beforeMethodSentChain(BeforeMethodSent... beforeMethodSentCalls) {
        return new BeforeMethodSentChain(Arrays.asList(beforeMethodSentCalls));
    }

    @Override
    public void beforeMethodSent(final MethodCallBuilder methodBuilder) {

        for (final BeforeMethodSent beforeMethodCallSent : beforeMethodCallSentList) {
            beforeMethodCallSent.beforeMethodSent(methodBuilder);
        }
    }

}
