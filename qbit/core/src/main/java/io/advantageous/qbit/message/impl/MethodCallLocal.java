package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.util.MultiMap;

public class MethodCallLocal implements MethodCall<Object> {

    private final String name;
    private final long timestamp;
    private final Object[] arguments;

    private final String uuid;
    private final long messageId;

    private final Callback<Object> callback;

    private final Request<Object> originatingRequest;


    public MethodCallLocal(final String name,
                           final String uuid,
                           final long timestamp,
                           final long messageId,
                           final Object[] args,
                           Callback<Object> callback, final Request<Object> originatingRequest) {
        this.name = name;
        this.timestamp = timestamp;
        this.arguments = args;
        this.uuid = uuid;
        this.messageId = messageId;
        this.callback = detectCallback(callback);
        this.originatingRequest = originatingRequest;
    }

    @Override
    public boolean hasCallback() {
        return callback != null;
    }

    @Override
    public Callback<Object> callback() {
        return callback;
    }

    private Callback detectCallback(Callback<Object> callback) {
        if (callback != null) {
            return callback;
        }
        final Object[] args = arguments;
        if (args == null) {
            return null;
        }
        for (int index = 0; index < args.length; index++) {
            if (args[index] instanceof Callback) {
                return (Callback) args[index];
            }
        }
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String address() {
        return name;
    }

    @Override
    public String returnAddress() {
        return uuid;
    }

    @Override
    public MultiMap<String, String> params() {
        return null;
    }

    @Override
    public MultiMap<String, String> headers() {
        return null;
    }

    @Override
    public boolean hasParams() {
        return false;
    }

    @Override
    public boolean hasHeaders() {
        return false;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public boolean isHandled() {
        return false;
    }

    @Override
    public void handled() {
    }

    @Override
    public String objectName() {
        return "";
    }

    @Override
    public Request<Object> originatingRequest() {
        return originatingRequest;
    }

    @Override
    public long id() {
        return messageId;
    }

    @Override
    public Object body() {
        return arguments;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
