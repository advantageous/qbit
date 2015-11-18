package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.boon.core.Maps;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.vertx.core.Vertx;

public class IntegrationTestWithSkyDNS {


    public static void main(String... args) {
        final Vertx vertx = Vertx.vertx();
        //final DnsSupport dnsSupport = new DnsSupport(new DnsSingleClientSupplier(vertx, "localhost", 5354),
        //        Maps.map("db", "dbService"), ".skydns.local");

        final DnsSupport dnsSupport = new DnsSupport(new DnsClientFromResolveConfSupplier(vertx),
                Maps.map("db", "dbService"),
                ".skydns.local");

        dnsSupport.loadServiceEndpointsByServiceName(
                CallbackBuilder.newCallbackBuilder().withListCallback(EndpointDefinition.class,
                        endpointDefinitions ->
                        {
                            endpointDefinitions.forEach(endpointDefinition ->
                                    System.out.printf("%s %s %s \n", endpointDefinition.getPort(),
                                            endpointDefinition.getHost(),
                                            endpointDefinition.getId()));
                        }).withErrorHandler(Throwable::printStackTrace)
                        .build(), "dbService");

    }
}
