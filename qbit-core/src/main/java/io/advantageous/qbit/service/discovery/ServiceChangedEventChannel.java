package io.advantageous.qbit.service.discovery;

import io.advantageous.qbit.annotation.EventChannel;

/**
 * ServiceChangedChannel
 * Created by rhightower on 3/23/15.
 */
@EventChannel
public interface ServiceChangedEventChannel {
    void  servicePoolChanged(String serviceName);
}
