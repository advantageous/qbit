package io.advantageous.qbit;

import io.advantageous.qbit.json.JsonMapper;
import org.boon.Boon;
import org.boon.Sets;
import org.boon.core.reflection.fields.FieldAccess;
import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.boon.json.JsonSerializer;
import org.boon.json.JsonSerializerFactory;
import org.boon.json.serializers.FieldFilter;

/**
 * Created by gcc on 10/15/14.
 * @author Rick Hightower
 */
public class BoonJsonMapper implements JsonMapper {

    private ThreadLocal<JsonParserAndMapper> parser = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {
            return new JsonParserFactory().setIgnoreSet(Sets.set("metaClass")).createFastObjectMapperParser();
        }
    };


    private ThreadLocal<JsonSerializer> serializer = new ThreadLocal<JsonSerializer>() {
        @Override
        protected JsonSerializer initialValue() {
            return new JsonSerializerFactory().addFilter(new FieldFilter() {
                @Override
                public boolean include(Object parent, FieldAccess fieldAccess) {
                    return !fieldAccess.name().equals("metaClass");
                }
            }
            ).create();
        }
    };



    @Override
    public Object fromJson(String json) {
        return parser.get().parse(json);
    }

    @Override
    public String toJson(Object object) {
        return serializer.get().serialize(object).toString();
    }



}
