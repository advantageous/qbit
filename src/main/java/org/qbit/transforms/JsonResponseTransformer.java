package org.qbit.transforms;

import org.boon.json.serializers.impl.JsonSimpleSerializerImpl;
import org.qbit.message.Response;

/**
 * Created by Richard on 8/26/14.
 */
public class JsonResponseTransformer implements Transformer<Response, Response>  {

    /**
     * JSON Serializer.
     */
    private final JsonSimpleSerializerImpl jsonSerializer = new JsonSimpleSerializerImpl();


    protected String toJson(Object object) {
        return jsonSerializer.serialize(object).toString();
    }



    @Override
    public Response transform(Response response) {
        Object body =  toJson(response.body());
        response.body(body);
        return response;

    }
}
