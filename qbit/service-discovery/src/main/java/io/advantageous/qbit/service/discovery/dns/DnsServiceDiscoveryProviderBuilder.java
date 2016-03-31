package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.boon.core.Sys;

import java.util.concurrent.TimeUnit;

/**
 * Builds a DnsServiceProvider.
 */
public class DnsServiceDiscoveryProviderBuilder {

    private DnsSupport dnsSupport;
    private int timeout = Sys.sysProp(DnsServiceDiscoveryProviderBuilder.class.getName() + ".timeout", 30);
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    public static DnsServiceDiscoveryProviderBuilder dnsServiceDiscoveryProviderBuilder() {
        return new DnsServiceDiscoveryProviderBuilder();
    }

    public DnsSupport getDnsSupport() {
        return dnsSupport;
    }

    public DnsServiceDiscoveryProviderBuilder setDnsSupport(DnsSupport dnsSupport) {
        this.dnsSupport = dnsSupport;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public DnsServiceDiscoveryProviderBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public DnsServiceDiscoveryProviderBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public DnsServiceDiscoveryProvider build() {
        return new DnsServiceDiscoveryProvider(getDnsSupport(), getTimeout(), getTimeUnit());
    }
}
