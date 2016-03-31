package io.advantageous.qbit.reactive.async;

import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Used to build AsyncFuture with or without latches.
 */
public class AsyncFutureBuilder {

    private Callback callback;
    private long startTime;
    private long timeout = 30;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    private Runnable onFinished;
    private Runnable onTimeout;
    private Consumer<Throwable> onError;
    private boolean supportLatch = true;

    public static AsyncFutureBuilder asyncFutureBuilder() {

        return new AsyncFutureBuilder();
    }

    public boolean isSupportLatch() {
        return supportLatch;
    }

    public AsyncFutureBuilder setSupportLatch(boolean supportLatch) {
        this.supportLatch = supportLatch;
        return this;
    }

    public Callback getCallback() {
        if (callback == null) {
            callback = o -> {
            };
        }
        return callback;
    }

    public AsyncFutureBuilder setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public AsyncFutureBuilder setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Runnable getOnFinished() {
        if (onFinished == null) {
            onFinished = () -> {

            };
        }
        return onFinished;
    }

    public AsyncFutureBuilder setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
        return this;
    }

    public Runnable getOnTimeout() {
        if (onTimeout == null) {
            onTimeout = () -> {
                LoggerFactory.getLogger(AsyncFutureBuilder.class).error("OPERATION TIMED OUT");
            };
        }
        return onTimeout;
    }

    public AsyncFutureBuilder setOnTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    public Consumer<Throwable> getOnError() {
        if (onError == null) {
            onError = error -> {
                LoggerFactory.getLogger(AsyncFutureBuilder.class).error("OPERATION ERROR ", error);
            };
        }
        return onError;
    }

    public AsyncFutureBuilder setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public long getTimeout() {
        if (timeout <= 0) {
            timeout = 30;
        }
        return timeout;
    }

    public AsyncFutureBuilder setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public TimeUnit getTimeUnit() {
        if (timeUnit == null) {
            timeUnit = TimeUnit.SECONDS;
        }
        return timeUnit;
    }

    public AsyncFutureBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public <T> AsyncFutureCallback<T> build() {

        if (isSupportLatch()) {
            return AsyncFutureBlockOnGetCallbackImpl.callback(
                    getCallback(),
                    getStartTime(),
                    getTimeUnit().toMillis(getTimeout()),
                    getOnFinished(),
                    getOnTimeout(),
                    getOnError());
        } else {

            return AsyncFutureCallbackImpl.callback(
                    getCallback(),
                    getStartTime(),
                    getTimeUnit().toMillis(getTimeout()),
                    getOnFinished(),
                    getOnTimeout(),
                    getOnError());
        }
    }

    public <T> AsyncFutureCallback<T> build(Class<T> returnType) {

        return build();
    }

    public <T> AsyncFutureCallback<List<T>> buildList(Class<T> returnType) {

        return build();
    }

    public <K, V> AsyncFutureCallback<Map<K, V>> buildMap(Class<K> keyType, Class<V> valueType) {

        return build();
    }
}
