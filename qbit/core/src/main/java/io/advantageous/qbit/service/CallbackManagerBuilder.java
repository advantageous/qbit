package io.advantageous.qbit.service;

import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.service.impl.CallbackManager;
import io.advantageous.qbit.service.impl.CallbackManagerWithTimeout;
import io.advantageous.qbit.util.Timer;

import java.util.Properties;

public class CallbackManagerBuilder {


    private  String name;
    private  boolean handleTimeouts = true;
    private  long timeOutMS = 30_000;
    private  long checkInterval = 30_000;
    private  Timer timer;

    public static String CONTEXT = "qbit.callbackmanager.";


    public static CallbackManagerBuilder callbackManagerBuilder() {
        return new CallbackManagerBuilder();
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHandleTimeouts() {
        return handleTimeouts;
    }

    public void setHandleTimeouts(boolean handleTimeouts) {
        this.handleTimeouts = handleTimeouts;
    }

    public long getTimeOutMS() {
        return timeOutMS;
    }

    public void setTimeOutMS(long timeOutMS) {
        this.timeOutMS = timeOutMS;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }


    public CallbackManager build() {
        return new CallbackManagerWithTimeout(getTimer(), getName(), isHandleTimeouts(), getTimeOutMS(), getCheckInterval());
    }
}
