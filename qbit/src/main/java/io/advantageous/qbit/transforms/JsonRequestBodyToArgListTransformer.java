package io.advantageous.qbit.transforms;

import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;

import java.util.List;

/**
 * Transforms a JSON request body to an argument list.
 * <p>
 * Created by Richard on 8/11/14.
 */
public class JsonRequestBodyToArgListTransformer implements Transformer<Request, Object> {

    private final JsonMapper mapper;

    public JsonRequestBodyToArgListTransformer(final JsonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object transform(final Request request) {
        if (request.body() instanceof List) {
            final List list = (List) request.body();
            return mapper.fromJson(((String) list.get(0)));
        } else if (request.body() instanceof String) {
            return mapper.fromJson(((String) request.body()));
        } else {
            throw new IllegalArgumentException("Unable to handle request");
        }
    }
}
