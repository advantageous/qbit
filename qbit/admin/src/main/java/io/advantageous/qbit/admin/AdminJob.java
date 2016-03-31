package io.advantageous.qbit.admin;

import java.util.concurrent.TimeUnit;

public interface AdminJob {
    int every();

    TimeUnit timeUnit();

    Runnable runnable();
}
