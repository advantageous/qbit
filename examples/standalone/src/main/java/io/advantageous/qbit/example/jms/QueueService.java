package io.advantageous.qbit.example.jms;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

@RequestMapping("/queue")
public class QueueService extends BaseService{


    public QueueService(final Reactor reactor, final Timer timer, final StatsCollector statsCollector) {
        super(reactor, timer, statsCollector);
        reactor.addRepeatingTask(Duration.ONE_SECOND, () -> {

            System.out.println("Hello");
        });
    }


    @GET("/ping")
    public boolean ping() {
        return true;
    }

    public static void main(final String... args) {

        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");

        managedServiceBuilder.addEndpointService(new QueueService(
                ReactorBuilder.reactorBuilder().build(),
                Timer.timer(), managedServiceBuilder.getStatServiceBuilder().buildStatsCollector()));

        managedServiceBuilder.getEndpointServerBuilder().build().startServer();
    }

}
