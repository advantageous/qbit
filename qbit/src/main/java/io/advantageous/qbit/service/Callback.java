package io.advantageous.qbit.service;

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Extends the JDK Consumer to provide a default error handler for RPC callbacks.
 * Note: This was boon Handler but we switched to JDK 8 Consumer style callback.
 * <p>
 * Created by gcc on 10/14/14.
 */
public interface Callback<T> extends Consumer<T> {

    default void onError(Throwable error) {
        LoggerFactory.getLogger(Callback.class).error(error.getMessage(), error);
    }
}
