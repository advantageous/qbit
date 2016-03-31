package io.advantageous.qbit.client;


import io.advantageous.qbit.message.MethodCallBuilder;

/**
 * `BeforeMethodSent` gets called just before a method is sent to a service queue or a service bundle.
 */
public interface BeforeMethodSent {

    default void beforeMethodSent(final MethodCallBuilder methodBuilder) {
    }
}
