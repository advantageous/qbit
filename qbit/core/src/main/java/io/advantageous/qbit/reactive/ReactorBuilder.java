package io.advantageous.qbit.reactive;

import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.util.Timer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ReactorBuilder {

    public static final String CONTEXT = "qbit.reactor.";
    private Timer timer = Timer.timer();
    private long defaultTimeOut = 60_000;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public ReactorBuilder(final PropertyResolver propertyResolver) {

        defaultTimeOut = propertyResolver.getLongProperty("defaultTimeOut", defaultTimeOut);

    }

    public ReactorBuilder(final Properties properties) {

        this(PropertyResolver.createPropertiesPropertyResolver(CONTEXT, properties));
    }


    public ReactorBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(CONTEXT));
    }

    public static ReactorBuilder reactorBuilder() {
        return new ReactorBuilder();

    }

    public Reactor build() {
        return new Reactor(getTimer(), getDefaultTimeOut(), getTimeUnit());
    }

    public Timer getTimer() {
        return timer;
    }

    public ReactorBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public long getDefaultTimeOut() {
        return defaultTimeOut;
    }

    public ReactorBuilder setDefaultTimeOut(long defaultTimeOut) {
        this.defaultTimeOut = defaultTimeOut;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ReactorBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }
}
