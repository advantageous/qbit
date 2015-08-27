package io.advantageous.qbit.admin.jobs;

import io.advantageous.qbit.admin.AdminJob;

import java.util.concurrent.TimeUnit;

public class AdminJobBase implements AdminJob {


    private final int every;
    private final TimeUnit timeUnit;
    private final Runnable runnable;

    public AdminJobBase(final int every,
                        final TimeUnit timeUnit,
                        final Runnable runnable) {
        this.every = every;
        this.timeUnit = timeUnit;
        this.runnable = runnable;
    }

    @Override
    public int every() {
        return every;
    }

    @Override
    public TimeUnit timeUnit() {
        return timeUnit;
    }

    @Override
    public Runnable runnable() {
        return runnable;
    }
}
