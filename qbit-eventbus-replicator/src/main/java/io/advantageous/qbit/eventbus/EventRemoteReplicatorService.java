package io.advantageous.qbit.eventbus;


import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.impl.SimpleEventConnector;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.util.MultiMap;


/**
 * @author Rick Hightower
 */
public class EventRemoteReplicatorService implements EventConnector {


    private final EventConnector eventConnector;



    public EventRemoteReplicatorService(final EventManager eventManager) {
        this.eventConnector = new SimpleEventConnector(eventManager);
    }


    public EventRemoteReplicatorService(final EventConnector eventConnector) {
        this.eventConnector = eventConnector;
    }





    public EventRemoteReplicatorService() {
        this.eventConnector = new SimpleEventConnector(QBit.factory().systemEventManager());
    }



    /** This message receives an event from a remote call. */
    @Override
    public void forwardEvent(final EventTransferObject<Object> event) {


        eventConnector.forwardEvent(new EventTransferObject<Object>() {
            @Override
            public String channel() {
                return event.channel();
            }

            @Override
            public long id() {
                return event.id();
            }

            @Override
            public Object body() {
                return event.body();
            }

            @Override
            public boolean isSingleton() {
                return true;
            }

            @Override
            public MultiMap<String, String> params() {
                return event.params();
            }

            @Override
            public MultiMap<String, String> headers() {
                return event.headers();
            }

            /* Save a map lookup or building a header map. */
            @Override
            public boolean wasReplicated() {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return event.equals(obj);
            }

            @Override
            public int hashCode() {
                return event.hashCode();
            }
        });
    }

    @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT})
    void flushConnector() {
        eventConnector.flush();
    }
}
