package io.advantageous.qbit.example.jms;


import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.http.PUT;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;
import org.apache.activemq.broker.BrokerService;

import java.util.Optional;

@RequestMapping("/jms")
public class JmsBroker extends BaseService {

    private Optional<BrokerService> broker = Optional.empty();


    public JmsBroker(Reactor reactor, Timer timer, StatsCollector statsCollector) {
        super(reactor, timer, statsCollector);

        initBroker();

        reactor.addRepeatingTask(Duration.ONE_MINUTE, this::initBroker);


    }


    @PUT(value = "/init")
    public boolean initBroker() {
        if (!broker.isPresent()) {
            broker= Optional.of(new BrokerService());
            try {
                broker= Optional.of(new BrokerService());
                broker.get().addConnector("tcp://localhost:" + 5000);
                broker.get().start();
            } catch (Exception e) {
                e.printStackTrace();
                broker = Optional.empty();
            }

        }
        return true;
    }


    @RequestMapping(value = "/ping", method = {RequestMethod.GET, RequestMethod.PUT})
    public boolean ping() {
        return true;
    }


    public static void main(final String... args) {



        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");

        managedServiceBuilder.addEndpointService(new JmsBroker(
                ReactorBuilder.reactorBuilder().build(),
                Timer.timer(), managedServiceBuilder.getStatServiceBuilder().buildStatsCollector()));

        managedServiceBuilder.setPort(7070).getEndpointServerBuilder().build().startServer();
    }

}
