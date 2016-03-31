package io.advantageous.qbit.http.request;

public interface HttpBinaryResponse extends HttpResponse<byte[]> {


    default boolean isText() {
        return false;
    }

}
