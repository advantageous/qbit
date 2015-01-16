package io.advantageous.qbit.http;


import io.advantageous.qbit.util.MultiMap;

/**
 * Represents a callback which receives an HTTP response.
 *
 * Created by rhightower on 10/21/14.
 * @author rhightower
 * can be text or binary
 */
public interface HttpResponse<T> {

    default boolean isText(){ return true; }

    void response(int code, String mimeType, T body);


    default void response(int code, String mimeType, T body, MultiMap<String, String> multiMap) {
        response(code, mimeType, body);
    }
}
