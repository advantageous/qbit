package io.advantageous.qbit.service;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.impl.ServiceImpl;

/**
 * Created by rhightower on 2/4/15.
 */
public class ServiceContext {


    static final ServiceContext serviceContext = new ServiceContext();
    public static ServiceContext serviceContext() {
        return serviceContext;
    }


    /**
     * The only time this is valid is during queueInit.
     * This allows a service to get at its Service interface.
     * @return
     */
    public  Service currentService() {
        return ServiceImpl.currentService();
    }

    public  EventManager eventManager() {
        return QBit.factory().systemEventManager();
    }

    public  void joinEventManager() {

        final EventManager eventManager = eventManager();
        eventManager.joinService(currentService());
    }

    public  <T> void send(String channel, T message) {
        eventManager().send(channel, message);
    }
}
