package io.advantageous.boon.bugs;

import com.google.common.hash.HashCode;

import com.google.common.hash.Hasher;
import io.advantageous.boon.Maps;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.serializers.CustomObjectSerializer;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.primitive.CharBuf;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.hash.Hashing.md5;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Maps.fromMap;
import static io.advantageous.boon.Maps.idx;
import static io.advantageous.boon.Maps.idxStr;
import static io.advantageous.boon.json.JsonFactory.fromJson;

/**
 * Created by Richard on 9/16/14.
 */
public class Bugs231 {


    boolean ok;

    public static String toJson(Object obj) {

        return createSerializer().serialize(obj).toString();
    }

    private static JsonSerializer createSerializer() {
        JsonSerializerFactory factory = new JsonSerializerFactory()
                .addTypeSerializer(HashCode.class, new HashCodeSerializer()
                );
        return factory.create();
    }


    private static class HashCodeSerializer implements CustomObjectSerializer<HashCode> {
        @Override
        public Class<HashCode> type() {
            return HashCode.class;
        }

        @Override
        public void serializeObject(JsonSerializerInternal serializer, HashCode instance, CharBuf builder) {
            serializer.serializeString(instance.toString(), builder);
        }
    }



    @Test
    public void test() {
        Hasher hasher = md5().newHasher();
        hasher.putString("heisann", Charset.defaultCharset());
        HashCode hash = hasher.hash();
        String actualJson = toJson(hash);

        ok = actualJson.equals("\"86c7c929d73e1d91c268c9f18d121212\"") || die(actualJson);

        puts(actualJson);
    }


    public static class Employee {
        String firstName = "Rick".intern();
        HashCode password = md5().newHasher().putString("BACON!", Charset.defaultCharset()).hash();

    }


    @Test
    public void testWithField() {
        Employee rick = new Employee();
        String actualJson = toJson(rick);
        puts(actualJson);


        /* Convert it back to an employee. */
        Map<String, Object> fromJson = (Map<String, Object>) fromJson(actualJson);

        /* Grab the password an convert it into a HashCode. */
        String password = idxStr(fromJson, "password");
        fromJson = Maps.copy(fromJson);
        /* Convert and shove it back into the map. */
        idx(fromJson, "password", HashCode.fromString(password));

        /* Now convert the map into an Employee. */
        Employee rick2 = fromMap(fromJson, Employee.class);


        ok = rick.firstName == rick2.firstName.intern() || die();

        ok = rick.password.equals(rick2.password) || die(rick2.password);

    }



}
