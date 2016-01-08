package io.advantageous.qbit.example.jms;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.PUT;
import io.advantageous.qbit.jms.JmsException;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.BaseService;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

import java.util.Optional;

@RequestMapping("/jms")
public class JmsSendService extends BaseService{

    private Optional<Queue<Todo>> queue = Optional.empty();

    private Optional<SendQueue<Todo>> sendQueue = Optional.empty();




    public JmsSendService(final Reactor reactor, final Timer timer, final StatsCollector statsCollector) {
        super(reactor, timer, statsCollector);
        reactor.addRepeatingTask(Duration.ONE_SECOND, () -> {
        });
    }


    /**
     * curl -X PUT -H "Content-Type: application/json" -d '{"name":"deploy app", "done":true}' http://localhost:8080/jms/todo/
     * @param todo todo object
     * @return true or throws an exception
     */
    @PUT(value = "/todo/", code = 202)
    public boolean send(final Todo todo) {
        if (!sendQueue.isPresent()) {
            initSendQueue();
        }
        try {
            sendQueue.ifPresent(todoSendQueue -> todoSendQueue.send(todo));
        } catch (JmsException ex) {
            queue = Optional.empty();
            sendQueue = Optional.empty();
            initSendQueue();
            sendQueue.ifPresent(todoSendQueue -> todoSendQueue.send(todo));
        }
        return true;
    }

    private void initSendQueue() {

        if (!queue.isPresent()) {
             queue = Optional.of(JmsUtil.createQueue());
        }

        queue.ifPresent(todoQueue -> {

            sendQueue = Optional.of(todoQueue.sendQueue());
        });

    }

    @GET("/ping")
    public boolean ping() {
        return true;
    }

    public static void main(final String... args) {



        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder().setRootURI("/");

        managedServiceBuilder.addEndpointService(new JmsSendService(
                ReactorBuilder.reactorBuilder().build(),
                Timer.timer(), managedServiceBuilder.getStatServiceBuilder().buildStatsCollector()));

        managedServiceBuilder.getEndpointServerBuilder().build().startServer();
    }

}
