package io.advantageous.qbit.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * This event is fired after QBit starts all its queues.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
class QBitStartedEvent extends ApplicationContextEvent {

    /**
     * Create a new ContextStartedEvent.
     *
     * @param source the {@code ApplicationContext} that the event is raised for
     *               (must not be {@code null})
     */
    QBitStartedEvent(ApplicationContext source) {
        super(source);
    }

}
