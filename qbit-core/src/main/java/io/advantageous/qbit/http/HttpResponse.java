package io.advantageous.qbit.http;

import io.advantageous.qbit.util.MultiMap;

/**
 * Created by rhightower on 1/29/15.
 */
public interface HttpResponse {

    MultiMap<String, String> headers();
    int code();
    String contentType();
    String body();

}
