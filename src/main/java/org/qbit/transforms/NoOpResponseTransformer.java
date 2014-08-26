package org.qbit.transforms;

import org.qbit.service.Response;

/**
 * Created by Richard on 8/26/14.
 */
public class NoOpResponseTransformer implements Transformer<Response, Response>{
    @Override
    public Response transform(Response response) {
        return response;
    }
}
