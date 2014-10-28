package io.advantageous.qbit.transforms;

import io.advantageous.qbit.message.Request;


/**
 * Created by Richard on 8/26/14.
 * @author rhightower
 */
public class NoOpRequestTransform implements Transformer<Request, Object> {

    @Override
    public Object transform(Request request) {
        return request.body();

    }
}
