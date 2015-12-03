package io.advantageous.qbit.example.jms;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.PUT;
import io.advantageous.qbit.events.EventBusQueueAdapter;
import io.advantageous.qbit.events.EventBusQueueAdapterBuilder;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

@RequestMapping("/jms")
public class JmsConsumerUsingEventBus extends BaseService {


    final BlockingQueue<Todo> blockingQueue = new ArrayBlockingQueue<>(100);

    public JmsConsumerUsingEventBus(final Reactor reactor, final Timer timer, final StatsCollector statsCollector,
                                    final Runnable queueProcess) {
        super(reactor, timer, statsCollector);
        reactor.addRepeatingTask(Duration.ONE_SECOND, queueProcess::run);
    }


    /**
     * curl -X PUT http://localhost:9090/jms/todo/
     *
     * @return true or throws an exception
     */
    @PUT(value = "/todo/", code = 202)
    public Todo get() {
        return blockingQueue.poll();
    }


    @OnEvent(JmsUtil.TODO_QUEUE)
    public void newTodo(final Todo todo) {
        blockingQueue.add(todo);
    }


    @GET("/ping")
    public boolean ping() {
        return true;
    }


    public static void main(final String... args) {


        final Supplier<Queue> todoQueueSupplier = JmsUtil::createQueue;

        final EventBusQueueAdapterBuilder eventBusQueueAdapterBuilder = new EventBusQueueAdapterBuilder();


        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");


        final EventBusQueueAdapter<Object> eventChannelAdapter = eventBusQueueAdapterBuilder
                .setChannel(JmsUtil.TODO_QUEUE).setEventManager(managedServiceBuilder.getEventManager())
                .setQueueSupplier(todoQueueSupplier).build();

        final Runnable queueProcess = eventChannelAdapter::process;


        managedServiceBuilder.addEndpointService(new JmsConsumerUsingEventBus(
                ReactorBuilder.reactorBuilder().build(),
                Timer.timer(),
                managedServiceBuilder.getStatServiceBuilder().buildStatsCollector(),
                queueProcess));

        managedServiceBuilder.setPort(9090).getEndpointServerBuilder().build().startServer();
    }

}
