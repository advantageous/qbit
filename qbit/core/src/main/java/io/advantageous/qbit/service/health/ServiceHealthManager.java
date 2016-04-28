package io.advantageous.qbit.service.health;

/**
 * Allows service to notify health system of its status by accessing the low level ServiceQueue health.
 */
public interface ServiceHealthManager {

    /**
     * Checks to see if service is failing.
     *
     * @return true if failing
     */
    default boolean isFailing() {
        return false;
    }

    /**
     * Check to see if the service is ok.
     *
     * @return true if ok
     */
    default boolean isOk() {
        return true;
    }

    /**
     * Mark the service as failing.
     */
    default void setFailing() {

    }


    /**
     * Set failing with a reason for the fail.
     *
     * @param reason reason
     */
    default void setFailingWithReason(HealthFailReason reason) {

    }

    /**
     * Set failing with cause
     *
     * @param cause cause
     */
    default void setFailingWithError(Throwable cause) {

    }

    /**
     * Mark the service as recovered.
     */
    default void recover() {

    }

}
