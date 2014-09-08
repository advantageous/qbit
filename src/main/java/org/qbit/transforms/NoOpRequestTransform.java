package org.qbit.transforms;

import org.qbit.message.Request;


/**
 * Created by Richard on 8/26/14.
 */
public class NoOpRequestTransform implements Transformer<Request, Object> {

    @Override
    public Object transform(Request request) {
        return request.body();

    }
}
