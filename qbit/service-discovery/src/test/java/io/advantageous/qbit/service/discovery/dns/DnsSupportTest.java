package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.dns.DnsClient;
import io.vertx.core.dns.MxRecord;
import io.vertx.core.dns.SrvRecord;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DnsSupportTest {

    public static TestSrvRecord srv(String service, String name, String target) {
        return new TestSrvRecord(service, name, "", 1, 1, target, 1);
    }

    @Test
    public void testLoadServicesByServiceName() throws Exception {

        /** DNS data. */
        final Map<String, List<SrvRecord>> dnsData = Maps.map(
                "db.skydns.local.", Lists.list(
                        srv("db", "skydns.local", "server1.db.skydns.local"),
                        srv("db", "skydns.local", "server2.db.skydns.local"),
                        srv("db", "skydns.local", "server3.db.skydns.local")
                )
        );

        final DnsClient client = new DnsClient() {
            @Override
            public DnsClient lookup(String name, Handler<AsyncResult<String>> handler) {
                return null;
            }

            @Override
            public DnsClient lookup4(String name, Handler<AsyncResult<String>> handler) {
                return null;
            }

            @Override
            public DnsClient lookup6(String name, Handler<AsyncResult<String>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveA(String name, Handler<AsyncResult<List<String>>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveAAAA(String name, Handler<AsyncResult<List<String>>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveCNAME(String name, Handler<AsyncResult<List<String>>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveMX(String name, Handler<AsyncResult<List<MxRecord>>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveTXT(String name, Handler<AsyncResult<List<String>>> handler) {
                return null;
            }

            @Override
            public DnsClient resolvePTR(String name, Handler<AsyncResult<String>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveNS(String name, Handler<AsyncResult<List<String>>> handler) {
                return null;
            }

            @Override
            public DnsClient resolveSRV(final String name, final Handler<AsyncResult<List<SrvRecord>>> handler) {
                AsyncResult<List<SrvRecord>> result = new AsyncResult<List<SrvRecord>>() {
                    @Override
                    public List<SrvRecord> result() {
                        return dnsData.get(name);
                    }

                    @Override
                    public Throwable cause() {
                        return null;
                    }

                    @Override
                    public boolean succeeded() {
                        return true;
                    }

                    @Override
                    public boolean failed() {
                        return false;
                    }
                };

                handler.handle(result);
                return this;
            }

            @Override
            public DnsClient reverseLookup(String ipaddress, Handler<AsyncResult<String>> handler) {
                return null;
            }
        };


        final DnsSupport dnsSupport = DnsSupportBuilder
                .dnsSupportBuilder(null)
                .setDnsClientSupplier(() -> client)
                .addDnsServerToServiceNameMapping("db", "dbService")
                .setPostfixURL(".skydns.local.")
                .build();


        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final AtomicReference<List<EndpointDefinition>> endPointsRef = new AtomicReference<>();

        dnsSupport.loadServiceEndpointsByServiceName(
                CallbackBuilder.newCallbackBuilder().withListCallback(EndpointDefinition.class,
                        endpointDefinitions ->
                        {

                            endPointsRef.set(endpointDefinitions);
                            countDownLatch.countDown();

                            endpointDefinitions.forEach(endpointDefinition ->
                                    System.out.printf("%s %s %s \n", endpointDefinition.getPort(),
                                            endpointDefinition.getHost(),
                                            endpointDefinition.getId()));
                        }).withErrorHandler(Throwable::printStackTrace)
                        .build(), "dbService");

        final List<EndpointDefinition> endpointDefinitionList = endPointsRef.get();

        final Map<String, EndpointDefinition> map = Maps.toMap(String.class, "host", endpointDefinitionList);

        assertNotNull(map.get("server1.db.skydns.local"));
        assertNotNull(map.get("server2.db.skydns.local"));
        assertNotNull(map.get("server3.db.skydns.local"));
        assertEquals("dbService-1-server1-db-skydns-local", map.get("server1.db.skydns.local").getId());


        final DnsServiceDiscoveryProvider serviceDiscoveryProvider = DnsServiceDiscoveryProviderBuilder.dnsServiceDiscoveryProviderBuilder().setDnsSupport(dnsSupport).build();

        final List<EndpointDefinition> endpointDefinitionList2 = serviceDiscoveryProvider.loadServices("dbService");

        final Map<String, EndpointDefinition> map2 = Maps.toMap(String.class, "host", endpointDefinitionList2);

        assertNotNull(map2.get("server1.db.skydns.local"));
        assertNotNull(map2.get("server2.db.skydns.local"));
        assertNotNull(map2.get("server3.db.skydns.local"));
        assertEquals("dbService-1-server1-db-skydns-local", map2.get("server1.db.skydns.local").getId());


    }

    public static class TestSrvRecord implements SrvRecord {


        private final String service;
        private final String name;
        private final String protocol;

        private final int priority;
        private final int weight;

        private final String target;
        private final int port;

        public TestSrvRecord(final String service, final String name, final String protocol,
                             final int priority, final int weight, final String target, final int port) {
            this.service = service;
            this.name = name;
            this.protocol = protocol;
            this.priority = priority;
            this.weight = weight;
            this.target = target;
            this.port = port;
        }


        @Override
        public int priority() {
            return priority;
        }

        @Override
        public int weight() {
            return weight;
        }

        @Override
        public int port() {
            return port;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String protocol() {
            return protocol;
        }

        @Override
        public String service() {
            return service;
        }

        @Override
        public String target() {
            return target;
        }
    }
}