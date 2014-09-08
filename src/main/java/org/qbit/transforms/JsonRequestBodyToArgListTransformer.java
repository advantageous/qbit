package org.qbit.transforms;

import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.qbit.message.Request;

import java.util.List;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/11/14.
 */
public class JsonRequestBodyToArgListTransformer implements Transformer<Request, Object> {


    /**
     * JSON Parser.
     */
    private final JsonParserAndMapper jsonParser = new JsonParserFactory().create();



    protected Object fromJson(String json) {
        try {
            return jsonParser.parse(json);
        } catch (Exception ex) {
            puts("Unable to handle JSON", json);
            die("Unable to handle JSON", json);
            return null;
        }
    }


    @Override
    public Object transform(Request request) {
        if (request.body() instanceof List) {
            List<Object> list = (List<Object>) request.body();
            return fromJson((String) list.get(0));

        } else if (request.body() instanceof String) {
            return fromJson((String) request.body());
        } else {
            die("Unable to handle request", request);
            return null;
        }
    }
}
