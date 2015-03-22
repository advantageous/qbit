package io.advantageous.qbit.service;

/**
 * CallbackCoordinator
 * Created by rhightower on 3/21/15.
 */
public interface CallbackCoordinator {

    boolean checkComplete();


    default boolean timedOut() {
        return false;
    }
}
