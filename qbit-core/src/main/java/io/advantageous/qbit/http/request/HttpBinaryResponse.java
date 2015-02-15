package io.advantageous.qbit.http.request;

/**
 * Created by rhightower on 1/15/15.
 */
public interface HttpBinaryResponse extends HttpResponseReceiver<byte[]> {

    default boolean isText(){ return false; }


}
