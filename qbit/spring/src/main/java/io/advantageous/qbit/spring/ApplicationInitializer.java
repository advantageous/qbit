package io.advantageous.qbit.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * Base application initializer.  Extend this class to add other initialization steps.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
public abstract class ApplicationInitializer implements ApplicationListener<QBitStartedEvent> {

    @Autowired
    private ApplicationContext applicationContext;

    protected abstract void initialize();

    @Override
    public void onApplicationEvent(QBitStartedEvent event) {
        initialize();
        applicationContext.publishEvent(new ApplicationReadyEvent(applicationContext));
    }
}
