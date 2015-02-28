package io.advantageous.qbit.events.impl;


import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.EventConnector;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.util.MultiMap;


/**
 * @author Rick Hightower
 */
public class EventRemoteReplicatorService implements EventConnector {


    private final EventConnector eventConnector;



    public EventRemoteReplicatorService(final EventManager eventManager) {

        this.eventConnector = new SimpleEventConnector(eventManager);
    }




    public EventRemoteReplicatorService() {

        this.eventConnector = new SimpleEventConnector(QBit.factory().systemEventManager());
    }



    /** This message receives an event from a remote call. */
    @Override
    public void forwardEvent(Event<Object> event) {


        eventConnector.forwardEvent(new Event<Object>() {
            @Override
            public String channel() {
                return event.channel();
            }

            @Override
            public long id() {
                return id();
            }

            @Override
            public Object body() {
                return body();
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
        });
    }
}
