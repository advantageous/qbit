package io.advantageous.qbit.service;

import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.service.impl.CallbackManager;
import io.advantageous.qbit.service.impl.CallbackManagerWithTimeout;
import io.advantageous.qbit.util.Timer;

import java.util.Properties;

public class CallbackManagerBuilder {


    public static String CONTEXT = "qbit.callbackmanager.";
    private String name;
    private boolean handleTimeouts = false;
    private long timeOutMS = 30_000;
    private long checkInterval = 5_000;
    private Timer timer;


    public CallbackManagerBuilder(final PropertyResolver propertyResolver) {
        handleTimeouts = propertyResolver.getBooleanProperty("handleTimeouts", handleTimeouts);
        timeOutMS = propertyResolver.getLongProperty("timeOutMS", timeOutMS);
        checkInterval = propertyResolver.getLongProperty("checkInterval", checkInterval);
    }

    public CallbackManagerBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(CONTEXT));
    }

    public CallbackManagerBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(CONTEXT, properties));
    }

    public static CallbackManagerBuilder callbackManagerBuilder() {
        return new CallbackManagerBuilder();
    }

    public String getName() {
        return name;
    }

    public CallbackManagerBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isHandleTimeouts() {
        return handleTimeouts;
    }

    public CallbackManagerBuilder setHandleTimeouts(boolean handleTimeouts) {
        this.handleTimeouts = handleTimeouts;
        return this;
    }

    public long getTimeOutMS() {
        return timeOutMS;
    }

    public CallbackManagerBuilder setTimeOutMS(long timeOutMS) {
        this.timeOutMS = timeOutMS;
        return this;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public CallbackManagerBuilder setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
        return this;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public CallbackManagerBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }


    public CallbackManager build() {
        return new CallbackManagerWithTimeout(getTimer(), getName(), isHandleTimeouts(), getTimeOutMS(), getCheckInterval());
    }
}
