package io.advantageous.qbit.servlet.integrationproto;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.server.ServiceServer;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;

/**
 * Created by rhightower on 2/12/15.
 */
public class MyServiceModule {
    @RequestMapping("/ping")
    public static class PingService {

        @RequestMapping("/ping")
        public String ping() {
            return "ok";
        }
    }

    public static ServiceServer configureApp(final HttpServer server) {
        return serviceServerBuilder().setHttpServer(server)
                .build().initServices(new PingService()).startServer();
    }
}
