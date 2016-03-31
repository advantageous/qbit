package io.advantageous.qbit.service.health;


import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Health service.
 */
public interface HealthService {


    void register(String name, long time, TimeUnit timeUnit);


    void registerNoTtl(String name);


    void checkInOk(String name);

    void checkIn(String name, HealthStatus status);


    boolean ok();

    List<String> findHealthyNodes();

    List<String> findAllNodes();

    List<String> findAllNodesWithStatus(HealthStatus queryStatus);


    List<String> findNotHealthyNodes();


    List<NodeHealthStat> loadNodes();


    void unregister(String serviceName);


    void failWithReason(final String name, final HealthFailReason reason);


    void failWithError(final String name, final Throwable error);


    void warnWithReason(final String name, final HealthFailReason reason);


    void warnWithError(final String name, final Throwable error);

}
