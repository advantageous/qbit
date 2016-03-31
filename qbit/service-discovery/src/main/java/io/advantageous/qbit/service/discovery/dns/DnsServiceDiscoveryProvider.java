package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    private final Logger logger = LoggerFactory.getLogger(DnsServiceDiscoveryProvider.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * New DnsServiceDiscoveryProvider.
     *
     * @param dnsSupport dnsSupport
     * @param timeout    timeout
     * @param timeUnit   timeUnit
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
     *
     * @param serviceName serviceName
     * @return list of EndpointDefinition
     */
    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {

        if (debug) logger.debug("Loading Service {}", serviceName);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final AtomicReference<List<EndpointDefinition>> endPointsRef = new AtomicReference<>();

        final AtomicReference<Throwable> exceptionAtomicReference = new AtomicReference<>();

        dnsSupport.loadServiceEndpointsByServiceName(CallbackBuilder.newCallbackBuilder()
                .withListCallback(EndpointDefinition.class,
                        endpointDefinitions ->
                        {

                            endPointsRef.set(endpointDefinitions);
                            countDownLatch.countDown();

                        }).withErrorHandler(exceptionAtomicReference::set)
                .build(), serviceName);

        try {
            if (debug) logger.debug("Waiting for load services {} {}", timeout, timeUnit);

            countDownLatch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new IllegalStateException("DNS Timeout", e);
        }


        if (exceptionAtomicReference.get() != null) {
            logger.error("DnsServiceDiscoveryProvider.loadServices EXCEPTION", exceptionAtomicReference.get());
            throw new IllegalStateException("Unable to read from DNS", exceptionAtomicReference.get());
        } else {

            if (debug) logger.debug("DnsServiceDiscoveryProvider.loadServices SUCCESS");
            return endPointsRef.get();
        }

    }

}
