package io.advantageous.qbit.http.request;


import io.advantageous.qbit.util.MultiMap;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Represents a callback which receives an HTTP response.
 *
 * Created by rhightower on 10/21/14.
 * @author rhightower
 * can be text or binary
 */
public interface HttpResponseReceiver<T> {

    default boolean isText(){ return true; }

    void response(int code, String contentType, T body);


    default void response(int code, String contentType, T body, MultiMap<String, String> headers) {
        response(code, contentType, body);
    }


    default Consumer<Exception> errorHandler() {
        return exception -> LoggerFactory.getLogger(HttpResponse.class)
                .error(exception.getMessage(), exception);
    }

}
