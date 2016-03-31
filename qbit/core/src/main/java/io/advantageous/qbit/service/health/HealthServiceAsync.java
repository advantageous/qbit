package io.advantageous.qbit.service.health;


import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.reactive.Callback;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface HealthServiceAsync extends ClientProxy {


    default void register(String name, long time, TimeUnit timeUnit) {
    }


    default void checkInOk(String name) {
    }

    default void checkIn(String name, HealthStatus status) {
    }


    default void ok(Callback<Boolean> ok) {
    }

    default void findHealthyNodes(Callback<List<String>> callback) {
    }

    default void findAllNodes(Callback<List<String>> callback) {
    }

    default void findAllNodesWithStatus(Callback<List<String>> callback, HealthStatus queryStatus) {
    }


    default void findNotHealthyNodes(Callback<List<String>> callback) {
    }


    default void loadNodes(Callback<List<NodeHealthStat>> callback) {
        callback.accept(Collections.emptyList());
    }

    default void unregister(String serviceName) {
    }

    default void failWithReason(final String name, final HealthFailReason reason) {
    }


    default void failWithError(final String name, final Throwable error) {
    }


    default void warnWithReason(final String name, final HealthFailReason reason) {
    }


    default void warnWithError(final String name, final Throwable error) {
    }


    default void registerNoTtl(String name) {
    }


}
