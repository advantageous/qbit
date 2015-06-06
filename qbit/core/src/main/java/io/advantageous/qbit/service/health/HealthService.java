package io.advantageous.qbit.service.health;


import java.util.List;
import java.util.concurrent.TimeUnit;

public interface HealthService {


    void register(String name, long time, TimeUnit timeUnit);


    void checkInOk(String name);

    void checkIn(String name, HealthStatus status);


    boolean ok();

    List<String> findHealthyNodes();

    List<String> findAllNodes();

    List<String> findAllNodesWithStatus(HealthStatus queryStatus);


    List<String> findNotHealthyNodes();


}
