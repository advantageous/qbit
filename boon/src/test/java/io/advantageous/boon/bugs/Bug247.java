package io.advantageous.boon.bugs;

import com.google.common.base.Predicate;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Maps.map;

import com.google.common.collect.Maps;
import io.advantageous.boon.Boon;
import io.advantageous.boon.Str;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.json.serializers.impl.AbstractCustomObjectSerializer;
import io.advantageous.boon.primitive.CharBuf;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by Richard on 9/19/14.
 */
public class Bug247 {

    boolean ok;

    public final class MapSerializer extends AbstractCustomObjectSerializer<Map> {


        public MapSerializer() {
            super(Map.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void serializeObject(JsonSerializerInternal serializer, Map instance, CharBuf builder) {
            serializer.serializeMap(instance, builder);
        }
    }

    @Test
    public void test() {

        JsonSerializer serializer
                = new JsonSerializerFactory()
                .addTypeSerializer(AbstractMap.class, new MapSerializer()).create();
        Map<String, String> map = map("Aaaaa", "aaaa", "Bbbbb", "bbbb", "Ccccc", "cccc");
        Predicate<String> startsWithB = new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.charAt(0) == 'B';
            }
        };
        Map<String, String> filtered = Maps.filterKeys(map, startsWithB);
        String result = serializer.serialize(filtered).toString();

        ok = result.equals("{\"Bbbbb\":\"bbbb\"}") || die(result);
    }


    @Test
    public void testUsingPrettyPrint() {


        Map<String, String> map = map("Aaaaa", "aaaa", "Bbbbb", "bbbb", "Ccccc", "cccc");
        Predicate<String> startsWithB = new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.charAt(0) == 'B';
            }
        };
        Map<String, String> filtered = Maps.filterKeys(map, startsWithB);

        String result = Boon.toPrettyJson(filtered);

        Str.equalsOrDie("        {\n" +
                "            \"Bbbbb\" : \"bbbb\"\n" +
                "        }", result);
    }
}
