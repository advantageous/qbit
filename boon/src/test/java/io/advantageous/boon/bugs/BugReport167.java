/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.bugs;

import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.json.*;
import io.advantageous.boon.json.implementation.JsonFastParser;
import io.advantageous.boon.json.implementation.ObjectMapperImpl;
import io.advantageous.boon.json.serializers.impl.JsonSerializerImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static io.advantageous.boon.Boon.fromJson;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.toJson;
import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 4/24/14.
 */
public class BugReport167 {

    static User user;

    static User user2;
    static String json;

    static boolean ok;

    public static class User {

        String[] favoriteColors;

        public User(String... colors) {
            this.favoriteColors = colors;
        }

        public void setFavoriteColors(String[] colors) {
            this.favoriteColors = colors;
        }

        public String[] getFavoriteColors() {
            return  this.favoriteColors;
        }

        @Override
        public String toString() {
            return "User{" +
                    "favoriteColors=" + Arrays.toString(favoriteColors) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;

            User user = (User) o;

            if (!Arrays.equals(favoriteColors, user.favoriteColors)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return favoriteColors != null ? Arrays.hashCode(favoriteColors) : 0;
        }
    }


    public static void main(String... args) {
        BugReport167 test = new  BugReport167();

        test.test();
        test.test2();

        //test.test3();
    }

    @Test
    public void test() {
        user = new User("red", "yellow", "green", "purple");

        json = toJson(user);
        puts ( json );

        user2 = fromJson(json, User.class);


        ok = user.equals(user2) || die("Users should be equal", user, user2);

    }

    @Test
    public void test2() {
        user = new User("red", "yellow", "green", "purple");


        final ObjectMapper objectMapper = JsonFactory.createUseAnnotations(true);

        json = objectMapper.toJson(user);
        puts(json);

        user2 = objectMapper.fromJson(json, User.class);


        ok = user.equals(user2) || die("Users should be equal", user, user2);

    }



    //@Test
    public void test3() {
        user = new User("red", "yellow", "green", "purple");


        final JsonParserFactory jsonParserFactory = new JsonParserFactory().usePropertiesFirst().useAnnotations();
        final JsonSerializerFactory serializerFactory = new JsonSerializerFactory().includeNulls().useAnnotations();
        final ObjectMapper objectMapper = new ObjectMapperImpl(jsonParserFactory, serializerFactory);

        json = objectMapper.toJson(user);
        puts(json);

        user2 = objectMapper.fromJson(json, User.class);


        ok = user.equals(user2) || die("Users should be equal", user, user2);

    }


    @Test
    public void test4() {
        user = new User("red", "yellow", "green", "purple");

        JsonParser parser = new JsonFastParser();
        JsonSerializer serializer = new JsonSerializerImpl();

        json = serializer.serialize(user).toString();
        puts(json);

        user2 = MapObjectConversion.fromMap( (Map)parser.parse(json), User.class);


        ok = user.equals(user2) || die("Users should be equal", user, user2);

    }

}
