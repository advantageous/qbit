package io.advantageous.qbit.example.perf.websocket;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.PUT;

import static io.advantageous.qbit.admin.ManagedServiceBuilder.managedServiceBuilder;

/**
 * curl  -H "Content-Type: application/json"  -X PUT http://localhost:8080/trade -d '{"name":"ibm", "amount":1}'
 * curl  http://localhost:8080/count
 */
@RequestMapping("/")
@Service("t")
public class TradeService {

    private long count;

    @PUT("/trade")
    public boolean t(final Trade trade) {
        trade.getNm().hashCode();
        trade.getAmt();
        count++;
        return true;
    }

    @GET("/count")
    public long count() {
        return count;
    }

    public static void main(final String... args) {

        final ManagedServiceBuilder managedServiceBuilder = managedServiceBuilder();

        managedServiceBuilder
                .addEndpointService(new TradeService())
                .setRootURI("/");

        //managedServiceBuilder.getEndpointServerBuilder().setHost("192.168.0.1");

        managedServiceBuilder.startApplication();
    }
}
