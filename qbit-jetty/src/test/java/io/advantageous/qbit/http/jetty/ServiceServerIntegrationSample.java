package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.server.ServiceServer;

import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;

/**
 * Created by rhightower on 2/13/15.
 */
public class ServiceServerIntegrationSample {

    @RequestMapping("/ping")
    public static class PingService {

        @RequestMapping("/ping")
        public String ping() {
            return "ok";
        }
    }



    public static void main(String... args) throws Exception {
        RegisterJettyWithQBit.registerJettyWithQBit();

        final ServiceServer serviceServer =
                serviceServerBuilder().setPort(9998).build()
                        .initServices(new PingService()).startServer();


    }
}
