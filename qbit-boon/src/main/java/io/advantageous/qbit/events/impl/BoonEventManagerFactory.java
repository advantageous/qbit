package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerFactory;
import io.advantageous.qbit.service.Service;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;


/**
 * Created by rhightower on 2/3/15.
 */
public class BoonEventManagerFactory implements EventManagerFactory{

    @Override
    public EventManager createEventManager() {
        return new BoonEventManager();
    }

}
