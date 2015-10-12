package io.advantageous.qbit.service.discovery.dns;

import org.junit.Test;

import java.net.URI;
import java.util.List;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.*;

public class DnsUtilTest {

    @Test
    public void test() {
        final List<URI> uris = DnsUtil.readDnsConf();

        assertTrue(uris.size() > 0);
        for (URI uri: uris) {
            puts (uri.getPort(), uri.getHost());
            assertNotNull(uri);
        }
    }
}