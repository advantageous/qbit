package io.advantageous.qbit.http.request;

import io.advantageous.qbit.util.MultiMap;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created by rhightower on 1/29/15.
 */
public interface HttpResponse {

    MultiMap<String, String> headers();
    int code();
    String contentType();
    String body();

}
