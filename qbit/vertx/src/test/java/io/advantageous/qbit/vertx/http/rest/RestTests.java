package io.advantageous.qbit.vertx.http.rest;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestTests {



    public static AtomicReference<List<DomainClass>> ref = new AtomicReference<>();

    public static class DomainClass {
        int i;
        String s;

        public DomainClass(int i, String s) {
            this.i = i;
            this.s = s;
        }

        @Override
        public String toString() {
            return "DomainClass{" +
                    "i=" + i +
                    ", s='" + s + '\'' +
                    '}';
        }
    }


    @RequestMapping
    public static class TestService {


        @RequestMapping(method = RequestMethod.POST)
        public void addAll(List<DomainClass> domains) {
            ref.set(domains);
        }
    }


    @Test
    public void test() {

        int openPort = PortUtils.findOpenPort();
        ServiceEndpointServer serviceEndpointServer = EndpointServerBuilder.endpointServerBuilder().setPort(openPort).build();
        serviceEndpointServer.initServices(new TestService());
        serviceEndpointServer.start();


        HTTP.Response response = HTTP.jsonRestCallViaPOST("http://localhost:" + openPort +
                "/services/testservice/addall", "[{\"i\": 1, \"s\": \"string\"}, " +
                "{\"i\": 2, \"s\": \"string2\"}]");

        assertEquals(202, response.status());

        while (ref.get() == null) {
            Sys.sleep(10);
        }

        assertNotNull(ref.get());



        serviceEndpointServer.stop();


    }
}
