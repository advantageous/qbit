package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.dns.DnsClient;

import java.util.function.Supplier;

public interface DnsClientSupplier extends Supplier<DnsClient> {

    default DnsClient getIfErrors() {
        return get();
    }
}
