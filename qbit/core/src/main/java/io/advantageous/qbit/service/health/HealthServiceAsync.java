package io.advantageous.qbit.service.health;


import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface HealthServiceAsync extends ClientProxy {


    void register(String name, long time, TimeUnit timeUnit);


    void checkInOk(String name);

    void checkIn(String name, HealthStatus status);


    void ok(Callback<Boolean> ok);

    void findHealthyNodes(Callback<List<String>> callback);

    void findAllNodes(Callback<List<String>> callback);

    void findAllNodesWithStatus(Callback<List<String>> callback, HealthStatus queryStatus);


    void findNotHealthyNodes(Callback<List<String>> callback);


    List<NodeHealthStat> loadNodes();

    void unregister(String serviceName);
}
