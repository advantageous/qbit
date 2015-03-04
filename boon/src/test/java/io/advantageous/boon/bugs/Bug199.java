package io.advantageous.boon.bugs;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.ObjectMapper;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by Richard on 8/25/14.
 */
public class Bug199 {


    static enum Gender {
        MALE, FEMALE
    }

    ;

    static class User {


        // @SerializedName(value = "map")
        private static Map<Gender, User> map;

        public String getName() {
            return "name";
        }

        public boolean isVerified() {
            return true;
        }

        public Gender getGender() {
            return Gender.FEMALE;
        }
    }

    @Test
    public void test() {

        JsonSerializerFactory factory = new JsonSerializerFactory();
        factory.usePropertyOnly();
        ObjectMapper mapper = JsonFactory.create(null, factory);

        User user = new User();
        EnumMap<Gender,User> map = new EnumMap<Gender, User>(Gender.class);
        map.put(Gender.FEMALE, user);

        puts(map);
        puts(mapper.writeValueAsString( map));
    }
}
