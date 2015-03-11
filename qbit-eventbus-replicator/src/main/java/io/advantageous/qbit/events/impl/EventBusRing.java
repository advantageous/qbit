package io.advantageous.qbit.events.impl;

import io.advantageous.consul.Consul;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.consul.domain.option.RequestOptionsBuilder;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EventBusRing implements Startable, Stoppable {

    private final String eventBusName;
    private final EventConnectorHub eventConnectorHub;
    private final PeriodicScheduler periodicScheduler;
    private final int interval;
    private final TimeUnit timeUnit;
    private final String consulHost;
    private final int consulPort;
    private final String datacenter;
    private final String tag;
    private final int longPollTimeSeconds;
    private Consul consul;
    private int lastIndex = 0;
    private RequestOptions requestOptions;

    private ScheduledFuture repeat;


    public EventBusRing(final String eventBusName,
                        final EventConnectorHub eventConnectorHub,
                        final PeriodicScheduler periodicScheduler,
                        final int interval,
                        final TimeUnit timeunit,
                        final String consulHost,
                        final int consulPort,
                        final int longPollTimeSeconds,
                        final String datacenter,
                        final String tag) {
        this.eventBusName = eventBusName;
        this.eventConnectorHub = eventConnectorHub;
        this.periodicScheduler = periodicScheduler==null ?
                QBit.factory().periodicScheduler() : periodicScheduler;
        this.interval = interval;
        this.timeUnit = timeunit;
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.consul = Consul.consul(consulHost, consulPort);
        this.datacenter = datacenter;
        this.tag = tag;
        this.longPollTimeSeconds = longPollTimeSeconds;
        this.requestOptions = new RequestOptionsBuilder()
                .consistency(Consistency.CONSISTENT)
                .blockSeconds(longPollTimeSeconds, lastIndex).build();


    }


    @Override
    public void start() {

     consul.start();
     repeat = periodicScheduler.repeat(() -> {
            monitor();
        }, interval, timeUnit);
    }

    private void monitor() {

        try {
            final ConsulResponse<List<ServiceHealth>> consulResponse = consul.health()
                    .getHealthyServices(
                            eventBusName, datacenter, tag, requestOptions);

            this.lastIndex = consulResponse.getIndex();
            this.requestOptions = new RequestOptionsBuilder()
                    .consistency(Consistency.CONSISTENT)
                    .blockSeconds(longPollTimeSeconds, lastIndex).build();


            rebuildHub();


        } catch (Exception ex) {
            ex.printStackTrace();//TODO add logging
            consul = Consul.consul(consulHost, consulPort);
            consul.start();
        }

    }

    private void rebuildHub() {

        //look at stuff in the hub and see if it matches the healthy nodes
        //if not take them out of the hub
    }


    @Override
    public void stop() {
        consul.stop();
        if (repeat!=null) {
            repeat.cancel(true);
        }
    }
}
