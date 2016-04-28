package io.advantageous.qbit.service.health;

import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.Promises;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Reakt style client for QBit health system.
 */
public interface HealthServiceClient extends ClientProxy {

    /**
     * Register a service with the health system
     *
     * @param name     name of the service
     * @param ttl      ttl on how long before the service timeout.
     * @param timeUnit time unit for the ttl.
     */
    default void register(String name, long ttl, TimeUnit timeUnit) {
    }


    /**
     * Check in the service so it passes it TTL
     *
     * @param name name of service.
     */
    default void checkInOk(String name) {
    }

    /**
     * Check in with a certain TTL.
     *
     * @param name   name of service (PASS, WARN, FAIL, UNKNOWN)
     * @param status status
     */
    default void checkIn(String name, HealthStatus status) {
    }


    /**
     * Checks to see if all services registered with the health system are ok.
     *
     * @return ok
     */
    default Promise<Boolean> ok() {
        final Promise<Boolean> promise = Promises.promise();
        promise.resolve(true);
        return promise;
    }

    /**
     * Returns list of healthy nodes.
     *
     * @return promise
     */
    default Promise<List<String>> findHealthyNodes() {
        final Promise<List<String>> promise = Promises.promise();
        promise.resolve(Collections.emptyList());
        return promise;
    }

    /**
     * Find all nodes
     *
     * @return promise
     */
    default Promise<List<String>> findAllNodes() {
        final Promise<List<String>> promise = Promises.promise();
        promise.resolve(Collections.emptyList());
        return promise;
    }

    /**
     * Find all nodes with a certain status.
     *
     * @param queryStatus status you are looking for.
     * @return promise
     */
    default Promise<List<String>> findAllNodesWithStatus(HealthStatus queryStatus) {
        final Promise<List<String>> promise = Promises.promise();
        promise.resolve(Collections.emptyList());
        return promise;

    }

    /**
     * Find all healthy nodes
     *
     * @return promise
     */
    default Promise<List<String>> findNotHealthyNodes() {
        final Promise<List<String>> promise = Promises.promise();
        promise.resolve(Collections.emptyList());
        return promise;

    }


    /**
     * Load all nodes no matter the status.
     *
     * @return promise
     */
    default Promise<List<NodeHealthStat>> loadNodes() {
        final Promise<List<NodeHealthStat>> promise = Promises.promise();
        promise.resolve(Collections.emptyList());
        return promise;
    }

    /**
     * Unregister the service
     *
     * @param serviceName name of service
     */
    default void unregister(String serviceName) {
    }

    /**
     * Fail with a particular reason.
     *
     * @param name   name
     * @param reason reason
     */
    default void failWithReason(final String name, final HealthFailReason reason) {
    }


    /**
     * Fail with error
     *
     * @param name  name
     * @param error error
     */
    default void failWithError(final String name, final Throwable error) {
    }

    /**
     * warn with reason
     *
     * @param name   name
     * @param reason reason
     */
    default void warnWithReason(final String name, final HealthFailReason reason) {
    }


    /**
     * warn with error
     *
     * @param name  name
     * @param error error
     */
    default void warnWithError(final String name, final Throwable error) {
    }


    /**
     * Register a service but don't specify a check in TTL.
     *
     * @param name name
     */
    default void registerNoTtl(String name) {
    }

}
