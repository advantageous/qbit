package io.advantageous.qbit;

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Extends the JDK Consumer to provide a default error handler for RPC callbacks.
 * <p>
 * Created by gcc on 10/14/14.
 */
public interface Handler<T> extends Consumer<T> {

    default void onError(Throwable error) {
        LoggerFactory.getLogger(Handler.class).error(error.getMessage(), error);
    }
}
