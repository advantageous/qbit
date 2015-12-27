package io.advantageous.qbit.spring;

import java.util.Map;
import java.util.Set;

/**
 * This is a holder for bean metadata that is scanned when automatically building service queues.  It is later used on
 * context refresh to start those queues.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
public class ServiceQueueRegistry {

    private final Map<String, Map<String, Object>> beanMetadataMap;

    public ServiceQueueRegistry(Map<String, Map<String, Object>> beanMetadataMap) {
        this.beanMetadataMap = beanMetadataMap;
    }

    Set<Map.Entry<String, Map<String, Object>>> getItems() {
        return beanMetadataMap.entrySet();
    }

}
