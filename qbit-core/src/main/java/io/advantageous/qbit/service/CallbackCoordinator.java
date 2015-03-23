package io.advantageous.qbit.service;

/**
 * CallbackCoordinator
 * Created by rhightower on 3/21/15.
 */
public interface CallbackCoordinator {

    boolean checkComplete();


    default boolean timedOut(long now) {

        if (startTime() == -1 || timeOutDuration() == -1) {
            return false;
        }
        return ( now - startTime() ) > timeOutDuration();
    }

    default long timeOutDuration() {
        return -1;
    }


    default long startTime() {
        return -1;
    }

    default void finished() {

    }
}
