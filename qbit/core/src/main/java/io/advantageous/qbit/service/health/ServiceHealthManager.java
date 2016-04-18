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
    boolean isFailing();

    /**
     * Check to see if the service is ok.
     *
     * @return true if ok
     */
    boolean isOk();

    /**
     * Mark the service as failing.
     */
    void setFailing();

    /**
     * Mark the service as recovered.
     */
    void recover();

}
