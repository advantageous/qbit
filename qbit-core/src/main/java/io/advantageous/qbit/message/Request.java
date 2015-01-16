package io.advantageous.qbit.message;

import io.advantageous.qbit.util.MultiMap;

/**
 * This is an abstraction for an incoming client request.
 * <p>
 * Created by Richard on 7/21/14.
 * @author Rick Hightower
 */
public interface Request<T> extends Message<T> {

    String address();

    String returnAddress();

    MultiMap<String, String> params();

    MultiMap<String, String> headers();


    boolean hasParams();

    boolean hasHeaders();

    long timestamp();

    boolean isHandled();

    void handled();
}
