package io.advantageous.qbit.reactive;

/**
 * CallbackCoordinator
 * created by rhightower on 3/21/15.
 */
public interface CallbackCoordinator {

    boolean checkComplete();


    default boolean timedOut(long now) {

        return !(startTime() == -1 || timeOutDuration() == -1) && (now - startTime()) > timeOutDuration();
    }

    default long timeOutDuration() {
        return -1;
    }


    default long startTime() {
        return -1;
    }

    default void finished() {

    }


    default void cancel() {

    }
}
