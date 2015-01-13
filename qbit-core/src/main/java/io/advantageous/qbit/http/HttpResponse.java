package io.advantageous.qbit.http;


/**
 * Represents a callback which receives an HTTP response.
 *
 * Created by rhightower on 10/21/14.
 * @author rhightower
 */
public interface HttpResponse {
    void response(int code, String mimeType, String body);
}
