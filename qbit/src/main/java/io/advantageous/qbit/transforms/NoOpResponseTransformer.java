package io.advantageous.qbit.transforms;

import io.advantageous.qbit.message.Response;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpResponseTransformer implements Transformer<Response<Object>, Response>{
    @Override
    public Response transform(Response response) {
        return response;
    }
}
