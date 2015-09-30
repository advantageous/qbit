package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wraps DnsSupport to fit in as a ServiceDiscoveryProvider.
 */
public class DnsServiceDiscoveryProvider implements ServiceDiscoveryProvider {

    /**
     * Dns support talks to DNS server to get EndpointDefinitions.
     */
    private final DnsSupport dnsSupport;

    /**
     * Timeout for DNS call.
     */
    private final int timeout;

    /**
     * Time unit for timeout.
     */
    private final TimeUnit timeUnit;

    /**
     * New DnsServiceDiscoveryProvider.
     * @param dnsSupport dnsSupport
     * @param timeout timeout
     * @param timeUnit timeUnit
     */
    public DnsServiceDiscoveryProvider(final DnsSupport dnsSupport,
                                       final int timeout,
                                       final TimeUnit timeUnit) {
        this.dnsSupport = dnsSupport;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    /**
     * Load the services.
     * @param serviceName serviceName
     * @return list of EndpointDefinition
     */
    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {


        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final AtomicReference<List<EndpointDefinition>> endPointsRef = new AtomicReference<>();

        final AtomicReference<Throwable> exceptionAtomicReference = new AtomicReference<>();

        dnsSupport.loadServiceEndpointsByServiceName(CallbackBuilder.callbackBuilder()
                .setCallbackReturnsList(EndpointDefinition.class,
                        endpointDefinitions ->
                        {

                            endPointsRef.set(endpointDefinitions);
                            countDownLatch.countDown();

                        }).setOnError(exceptionAtomicReference::set)
                .build(), serviceName);

        try {
            countDownLatch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new IllegalStateException("DNS Timeout", e);
        }
        if (exceptionAtomicReference.get()!=null) {
            throw new IllegalStateException("Unable to read from DNS", exceptionAtomicReference.get());
        } else {
            return endPointsRef.get();
        }

    }

}
