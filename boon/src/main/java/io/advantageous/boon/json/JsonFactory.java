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

package io.advantageous.boon.json;

import io.advantageous.boon.json.implementation.ObjectMapperImpl;

import java.io.Reader;
import java.util.List;

/**
 * Created by rick on 1/4/14.
 */
public class JsonFactory {


    private static ObjectMapper json = JsonFactory.create();

    public static ObjectMapper create () {
        JsonParserFactory jsonParserFactory = new JsonParserFactory();
        jsonParserFactory.lax();

        return new ObjectMapperImpl(jsonParserFactory,  new JsonSerializerFactory());
    }

    public static String toJson(Object value) {
         return json.toJson( value );
    }

    public static void toJson(Object value, Appendable appendable) {
         json.toJson( value, appendable );
    }

    public static <T> T fromJson(String str, Class<T> clazz) {
        return json.fromJson(str, clazz);
    }


    public static <T> List<T> fromJsonArray(String str, Class<T> clazz) {
        return json.parser().parseList(clazz, str);
    }

    public static Object fromJson(String str) {
         return json.fromJson(str);
    }

    public static Object fromJson(Reader reader) {
        return json.fromJson(reader);
    }

    public static ObjectMapper create (JsonParserFactory parserFactory, JsonSerializerFactory serializerFactory) {
        return new ObjectMapperImpl(parserFactory, serializerFactory);
    }

    public static ObjectMapper createUseProperties (boolean useJsonDates) {
        JsonParserFactory jpf = new JsonParserFactory();
        jpf.usePropertiesFirst();
        JsonSerializerFactory jsf = new JsonSerializerFactory();

        jsf.usePropertiesFirst();

        if (useJsonDates) {
            jsf.useJsonFormatForDates();
        }
        return new ObjectMapperImpl(jpf, jsf);
    }

    public static ObjectMapper createUseAnnotations (boolean useJsonDates) {
        JsonParserFactory jpf = new JsonParserFactory();
        JsonSerializerFactory jsf = new JsonSerializerFactory();

        jsf.useAnnotations();

        if (useJsonDates) {
            jsf.useJsonFormatForDates();
        }
        return new ObjectMapperImpl(jpf, jsf);
    }


    public static ObjectMapper createUseJSONDates () {
        JsonParserFactory jpf = new JsonParserFactory();
        JsonSerializerFactory jsf = new JsonSerializerFactory();
        jsf.useJsonFormatForDates();
        return new ObjectMapperImpl(jpf, jsf);
    }

    public static String niceJson(String str) {
        return str.replace('\'', '\"');
    }
}
