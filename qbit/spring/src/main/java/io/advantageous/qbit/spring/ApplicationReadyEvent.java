package io.advantageous.qbit.spring;

import org.springframework.context.ApplicationEvent;

/**
 * Application event fired when the application is ready for traffic.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
public class ApplicationReadyEvent extends ApplicationEvent {
    public ApplicationReadyEvent(Object source) {
        super(source);
    }
}
