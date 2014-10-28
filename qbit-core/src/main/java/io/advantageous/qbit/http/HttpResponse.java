package io.advantageous.qbit.http;


/**
 * Created by rhightower on 10/21/14.
 */
public interface HttpResponse {
    void response(int code, String mimeType, String body);
}
