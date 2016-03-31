package io.advantageous.qbit.http.request;

import io.advantageous.qbit.util.MultiMap;


public interface HttpResponse<T> {


    MultiMap<String, String> headers();

    int code();

    String contentType();


    T body();

    boolean isText();
}
