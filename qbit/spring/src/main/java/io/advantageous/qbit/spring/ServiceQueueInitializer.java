package io.advantageous.qbit.spring;

import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.springframework.boot.ansi.AnsiElement.*;

/**
 * The ServiceQueueInitializer is an event listener that starts all the queues after everything has been setup.
 */
public class ServiceQueueInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(ServiceQueueInitializer.class);

    @Autowired(required = false)
    @Qualifier("serviceEndpointServer")
    private ServiceEndpointServer serviceEndpointServer;

    @Autowired(required = false)
    @Qualifier("clusteredEventManagerImpl")
    private EventManager clusteredEventManager;

    @Autowired
    private ServiceQueueRegistry serviceQueueRegistry;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        serviceQueueRegistry.getItems().forEach(item -> {

            final ServiceQueue serviceQueue = applicationContext.getBean(item.getKey(), ServiceQueue.class);

            if (clusteredEventManager != null && (boolean) item.getValue().get("remoteEventListener")) {
                clusteredEventManager.joinService(serviceQueue);
            }

            if (serviceEndpointServer != null && (boolean) item.getValue().get("exposeRemoteEndpoint")) {
                final String endpointLocation = (String) item.getValue().get("endpointLocation");
                logger.info(AnsiOutput.toString("Registering endpoint: ", BOLD, GREEN, endpointLocation, NORMAL));
                serviceEndpointServer.addServiceQueue(endpointLocation, serviceQueue);

                logger.info("Starting service queue as part of endpoint {}", serviceQueue.name());
                serviceQueue.startServiceQueue();
            } else {
                logger.info("Starting service queue standalone {}", serviceQueue.name());
                serviceQueue.start();
                serviceQueue.startCallBackHandler();
            }

        });
        if (serviceEndpointServer != null) {
            serviceEndpointServer.start();
        }

        applicationContext.publishEvent(new QBitStartedEvent(applicationContext));
    }
}
