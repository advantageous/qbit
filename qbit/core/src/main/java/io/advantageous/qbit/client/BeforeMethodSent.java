package io.advantageous.qbit.client;


import io.advantageous.qbit.message.MethodCallBuilder;

public interface BeforeMethodSent {

    default void beforeMethodSent(final MethodCallBuilder methodBuilder) {}
}
