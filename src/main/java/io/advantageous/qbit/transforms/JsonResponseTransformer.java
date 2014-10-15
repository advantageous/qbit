package io.advantageous.qbit.transforms;

import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Response;

/**
 * Transforms raw json into a service response object.
 * <p>
 * Created by Richard on 8/26/14.
 */
public class JsonResponseTransformer implements Transformer<Response<Object>, Response> {

    private final JsonMapper mapper;

    public JsonResponseTransformer(final JsonMapper mapper) {
        this.mapper = mapper;
    }

    protected String toJson(final Object object) {
        return mapper.toJson(object);
    }

    @Override
    public Response transform(final Response<Object> response) {
        final Object body = toJson(response.body());
        response.body(body);
        return response;
    }
}
