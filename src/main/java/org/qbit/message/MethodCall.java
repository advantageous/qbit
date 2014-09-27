package org.qbit.message;

public interface MethodCall<T> extends Request<T> {

    String name();

    long timestamp();

    String objectName();
}
