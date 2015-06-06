package io.advantageous.qbit.service.discovery.spi;

import io.advantageous.boon.core.IO;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * created by rick on 5/20/15.
 */
public class ServiceDiscoveryFileSystemProviderTest {


    @Test
    public void testNone() throws Exception {
        ServiceDiscoveryFileSystemProvider provider =
                new ServiceDiscoveryFileSystemProvider(File.createTempFile("testNone", "testNone").getParentFile(), 3_000);

        List<EndpointDefinition> foo = provider.loadServices("baz");

        assertEquals(0, foo.size());

    }


    @Test
    public void testSome() throws Exception {
        File dir = File.createTempFile("testSome", "testSome").getParentFile();
        ServiceDiscoveryFileSystemProvider provider =
                new ServiceDiscoveryFileSystemProvider(dir, 3_000);

        List<EndpointDefinition> endpointDefinitions = EndpointDefinition.serviceDefinitions(
                EndpointDefinition.serviceDefinition("fooId", "foo", "www.foo.com", 9090));

        JsonSerializer jsonSerializer = new JsonSerializerFactory().create();
        String json = jsonSerializer.serialize(endpointDefinitions).toString();

        File outputFile = new File(dir, "foo.json");

        IO.write(outputFile.toPath(), json);

        List<EndpointDefinition> fooList = provider.loadServices("foo");

        assertEquals(1, fooList.size());

        EndpointDefinition endpointDefinition = fooList.get(0);

        assertEquals(9090, endpointDefinition.getPort());
        assertEquals("fooId", endpointDefinition.getId());
        assertEquals("www.foo.com", endpointDefinition.getHost());
        assertEquals("foo", endpointDefinition.getName());


        outputFile.delete();

    }

}