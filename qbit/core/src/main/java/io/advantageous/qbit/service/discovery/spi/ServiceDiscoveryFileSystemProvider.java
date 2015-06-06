package io.advantageous.qbit.service.discovery.spi;

import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.util.Timer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceDiscoveryFileSystemProvider implements ServiceDiscoveryProvider {

    private final File dir;
    private final Map<String, ServiceData> serviceDataMap = new ConcurrentHashMap<>();
    private final long checkIntervalMS;
    private final ThreadLocal<JsonParserAndMapper> jsonMappingParserThreadLocal = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {

            return new JsonParserFactory().lax().create();
        }
    };


    public ServiceDiscoveryFileSystemProvider(final File dir, long checkIntervalMS) {
        this.dir = dir;
        this.checkIntervalMS = checkIntervalMS;
    }

    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {


        ServiceData serviceData = serviceDataMap.get(serviceName);

        if (serviceData == null) {
            File file = new File(dir, serviceName + ".json");
            serviceData = new ServiceData(loadDefinitions(file), Timer.timer().now(), file.lastModified());
            serviceDataMap.put(serviceName, serviceData);
        } else {
            if (Timer.timer().now() > serviceData.longLastCheck + checkIntervalMS) {

                File file = new File(dir, serviceName + ".json");

                if (file.exists() && file.lastModified() > serviceData.lastModified) {
                    serviceData = new ServiceData(loadDefinitions(file), Timer.timer().now(), file.lastModified());
                    serviceDataMap.put(serviceName, serviceData);
                }
            }
        }

        return serviceData.endpointDefinitions;
    }

    private List<EndpointDefinition> loadDefinitions(final File file) {

        if (file.exists()) {
            return jsonMappingParserThreadLocal.get().parseListFromFile(EndpointDefinition.class, file.toString());
        } else {
            return Collections.emptyList();
        }
    }

    private static class ServiceData {
        final List<EndpointDefinition> endpointDefinitions;
        final long longLastCheck;
        final long lastModified;

        public ServiceData(List<EndpointDefinition> endpointDefinitions, long longLastCheck, long lastModified) {
            this.endpointDefinitions = endpointDefinitions;
            this.longLastCheck = longLastCheck;
            this.lastModified = lastModified;

        }
    }
}
