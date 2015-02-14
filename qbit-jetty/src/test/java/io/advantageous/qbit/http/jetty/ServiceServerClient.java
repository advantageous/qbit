package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceProxyUtils;
import org.boon.core.Sys;

import static org.boon.Boon.puts;


/**
 * Created by rhightower on 2/14/15.
 */
public class ServiceServerClient {

    public interface PingService {
        void ping(Callback<String> callback);
    }

    public static void main(String... args) throws Exception {

        Client client = new ClientBuilder().setPort(9998).setPollTime(10)
                .setAutoFlush(true).setFlushInterval(50).setRequestBatchSize(50)
                .setProtocolBatchSize(50).build();

        PingService pingService = client.createProxy(PingService.class, "ping");

        client.start();

        pingService.ping(s -> puts ("FROM SERVER", s));


        ServiceProxyUtils.flushServiceProxy(pingService);

        Sys.sleep(1000000);
    }
}
