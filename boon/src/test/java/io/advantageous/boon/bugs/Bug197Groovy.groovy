package io.advantageous.boon.bugs

import io.advantageous.boon.json.JsonSerializer
import io.advantageous.boon.json.JsonSerializerFactory
import io.advantageous.boon.json.serializers.JsonSerializerInternal
import io.advantageous.boon.json.serializers.impl.AbstractCustomObjectSerializer
import io.advantageous.boon.primitive.CharBuf
import org.codehaus.groovy.runtime.GStringImpl
import org.junit.Test

import static io.advantageous.boon.Boon.puts

/**
 * Created by Richard on 9/14/14.
 */
class Bug197Groovy {


    public static final class JsonEncoder {

        private JsonEncoder() {}

        public static String toJson(Object obj) {
            JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory()
                    .addTypeSerializer(GStringImpl.class, new AbstractCustomObjectSerializer(GStringImpl.class) {

                @Override
                public void serializeObject(JsonSerializerInternal serializer, Object instance, CharBuf builder) {
                    builder.addString(((GString) instance).toString());
                }
            });
            JsonSerializer serializer = jsonSerializerFactory.create();
            return serializer.serialize(obj).toString();
        }
    }

    @Test
    public void test() {


        String value = "hello";
        String actualJson = JsonEncoder.toJson("Not AGString $value");

        puts(actualJson);



    }
}
