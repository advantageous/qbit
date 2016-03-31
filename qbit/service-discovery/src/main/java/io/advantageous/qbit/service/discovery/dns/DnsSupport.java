package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.vertx.core.dns.DnsClient;
import io.vertx.core.dns.SrvRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * DNS Support for service discovery.
 * This looks up DNS entries for a given domain name.
 * <p>
 * It has two main methods.
 * <p>
 * One method allow you to look up things by URL e.g., db.skydns.local. .
 * <p>
 * The other method allows you to look things up by QBit service name e.g., dbService.
 */
public class DnsSupport {


    private final Logger logger = LoggerFactory.getLogger(DnsSupport.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * Holds mappings from DNS names to service names.
     */
    private final Map<String, String> dnsServiceNameToServiceName;

    /**
     * Holds mappings from service names to dns names.
     */
    private final Map<String, String> serviceNameToDNSName;

    /**
     * Holds a postfixURL to hold what URL comes after the service name.
     * <p>
     * Example: db.skydns.local.
     * In the above db is the service and skydns.local. is the postfix URL.
     * <p>
     * The postfixURL equates to the name in the SRV DNS record.
     */
    private final String postfixURL;


    /**
     * Class that knows how to create an instance of DnsClient.
     */
    private final DnsClientSupplier dnsClientProvider;

    /**
     * @param dnsClientProvider           dnsClientProvider
     * @param dnsServiceNameToServiceName dnsServiceNameToServiceName
     * @param postFixURL                  postFixURL
     */
    public DnsSupport(final DnsClientSupplier dnsClientProvider,
                      final Map<String, String> dnsServiceNameToServiceName,
                      final String postFixURL) {

        this.dnsClientProvider = dnsClientProvider;
        this.postfixURL = postFixURL == null ? "" : postFixURL;
        this.dnsServiceNameToServiceName = dnsServiceNameToServiceName;
        this.serviceNameToDNSName = new HashMap<>(dnsServiceNameToServiceName.size());

        /*
         * Build serviceNameToDNSName by reversing the dnsServiceNameToServiceName mappings.
         */
        dnsServiceNameToServiceName.entrySet().forEach(entry -> serviceNameToDNSName.put(entry.getValue(), entry.getKey()));


        if (debug) {
            logger.debug("DnsSupport dnsClientProvider={} postfixURL={} dnsServiceNameToServiceName={} serviceNameToDNSName={}",
                    this.dnsClientProvider, this.postfixURL, this.dnsServiceNameToServiceName, this.serviceNameToDNSName);
        }
    }


    /**
     * Looks up a service name based on its dns service name. The service part of the SRV DNS Record.
     *
     * @param dnsServiceName dnsServiceName
     * @return serviceName
     */
    public String findServiceName(final String dnsServiceName) {
        String serviceName = dnsServiceNameToServiceName.get(dnsServiceName);
        serviceName = serviceName == null ? dnsServiceName : serviceName;

        if (debug) logger.debug("FindServiceName dnsServiceName={} serviceName={}", dnsServiceName, serviceName);

        return serviceName;
    }

    /**
     * Looks up a dns service name (SRV DNS RECORD).
     *
     * @param serviceName serviceName
     * @return DNS service name (server field + name of SRV DNS Record).
     */
    public String findDnsServiceName(final String serviceName) {
        String dnsServiceName = serviceNameToDNSName.get(serviceName);
        dnsServiceName = (dnsServiceName == null ? serviceName : dnsServiceName) + postfixURL;


        if (debug) logger.debug("Find DNS_ServiceName dnsServiceName={} serviceName={}", dnsServiceName, serviceName);
        return dnsServiceName;
    }


    /**
     * Load the service nodes based on the internal service name.
     * DB, Ingester, RadarAggregator, etc.
     *
     * @param callback    callback
     * @param serviceName serviceName
     */
    public void loadServiceEndpointsByServiceName(final Callback<List<EndpointDefinition>> callback,
                                                  final String serviceName) {

        loadServiceEndpointsByDNSService(callback, findDnsServiceName(serviceName));
    }

    /**
     * Load the services nodes by its "${SRV.service}${SRV.name}".
     *
     * @param callback   callback
     * @param serviceURL serviceURL
     */
    public void loadServiceEndpointsByDNSService(final Callback<List<EndpointDefinition>> callback,
                                                 final String serviceURL) {
        final DnsClient dnsClient = dnsClientProvider.get();
        dnsClient.resolveSRV(serviceURL, event ->
                {
                    if (event.succeeded()) {
                        if (debug) logger.debug("loadServiceEndpointsByDNSService SUCCESS serviceURL={} ", serviceURL);
                        callback.returnThis(convertEndpoints(event.result()));
                    } else {

                        Throwable error = event.cause();

                        logger.info("loadServiceEndpointsByDNSService FAILURE  " + serviceURL, error);


                        attemptRecover(callback, serviceURL, error);

                    }
                }
        );
    }

    private void attemptRecover(final Callback<List<EndpointDefinition>> callback, final String serviceURL, final Throwable error) {
        final DnsClient dnsClient2 = dnsClientProvider.getIfErrors();

        dnsClient2.resolveSRV(serviceURL, event -> {

            if (event.succeeded()) {

                if (debug)
                    logger.debug("loadServiceEndpointsByDNSService FAIL OVER SUCCESS serviceURL={} ", serviceURL);
                callback.returnThis(convertEndpoints(event.result()));
            } else {

                logger.info("loadServiceEndpointsByDNSService FAIL OVER FAILURE  " + serviceURL, event.cause());

                //Send the first failure
                callback.onError(error);
            }
        });
    }

    /**
     * Converts list of SrvRecord(s) to list of EndpointDefinition(s).
     *
     * @param results of SrvRecord to convert to EndpointDefinition(s)
     * @return list of EndpointDefinition
     */
    private List<EndpointDefinition> convertEndpoints(final List<SrvRecord> results) {
        return results.stream().map(this::convertSrvRecordToEndpointDefinition
        ).collect(Collectors.<EndpointDefinition>toList());
    }

    /**
     * Convert a single srvRecord into an EndpointDefinition.
     *
     * @param srvRecord srvRecord
     * @return EndpointDefinition from srvRecord
     */
    private EndpointDefinition convertSrvRecordToEndpointDefinition(final SrvRecord srvRecord) {
        return new EndpointDefinition(findServiceName(srvRecord.service()), srvRecord.target(),
                srvRecord.port());
    }


}
