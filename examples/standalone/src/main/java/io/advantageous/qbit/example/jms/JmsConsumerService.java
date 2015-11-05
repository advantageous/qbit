package io.advantageous.qbit.example.jms;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.PUT;
import io.advantageous.qbit.jms.JmsException;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

import java.util.Optional;

@RequestMapping("/jms")
public class JmsConsumerService extends BaseService {

    private Optional<Queue<Todo>> queue = Optional.empty();

    private Optional<ReceiveQueue<Todo>> consumeQueue = Optional.empty();




    public JmsConsumerService(final Reactor reactor, final Timer timer, final StatsCollector statsCollector) {
        super(reactor, timer, statsCollector);
        reactor.addRepeatingTask(Duration.ONE_SECOND, () -> {
        });
    }


    /**
     * curl -X PUT http://localhost:9090/jms/todo/
     * @return true or throws an exception
     */
    @PUT(value = "/todo/", code = 202)
    public Todo get() {
        if (!consumeQueue.isPresent()) {
            initConsumeQueue();
        }

        Todo todo;

        try {
            todo = consumeQueue.get().poll();
        } catch (JmsException ex) {
            queue = Optional.empty();
            consumeQueue = Optional.empty();
            initConsumeQueue();
            todo = consumeQueue.get().poll();
        }
        return todo;
    }

    private void initConsumeQueue() {

        if (!queue.isPresent()) {
            queue = Optional.of(JmsUtil.createQueue());
        }

        queue.ifPresent(todoQueue -> {

            consumeQueue = Optional.of(todoQueue.receiveQueue());
        });

    }

    @GET("/ping")
    public boolean ping() {
        return true;
    }

    public static void main(final String... args) {



        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");

        managedServiceBuilder.addEndpointService(new JmsConsumerService(
                ReactorBuilder.reactorBuilder().build(),
                Timer.timer(), managedServiceBuilder.getStatServiceBuilder().buildStatsCollector()));

        managedServiceBuilder.setPort(9090).getEndpointServerBuilder().build().startServer();
    }

}
