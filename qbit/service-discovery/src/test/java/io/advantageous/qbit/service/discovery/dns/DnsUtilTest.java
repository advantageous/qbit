package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.boon.core.Sys;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.List;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.*;

@Ignore
public class DnsUtilTest {

    @Test
    public void test() {
        final List<URI> uris = DnsUtil.readDnsConf();

        assertTrue(uris.size() > 0);
        for (URI uri : uris) {
            puts(uri.getPort(), uri.getHost());
            assertNotNull(uri);
        }
    }


    @Test
    public void testResolvOverride() {


        Sys.putSysProp(DnsUtil.QBIT_DNS_RESOLV_CONF, "./qbit/service-discovery/src/test/test-files/resolv.conf");
        final List<URI> uris = DnsUtil.readDnsConf();

        assertTrue(uris.size() > 0);
        for (URI uri : uris) {
            puts(uri.getPort(), uri.getHost());
            assertNotNull(uri);
        }

        assertEquals("localhost", uris.get(0).getHost());
        assertEquals(5354, uris.get(0).getPort());
    }
}