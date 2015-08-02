/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
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
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit;

import io.advantageous.boon.core.Sets;
import io.advantageous.boon.core.reflection.Mapper;
import io.advantageous.boon.core.reflection.MapperComplex;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.qbit.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * created by gcc on 10/15/14.
 *
 * @author Rick Hightower
 */
public class BoonJsonMapper implements JsonMapper {

    private final ThreadLocal<JsonParserAndMapper> parser = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {
            return new JsonParserFactory().setIgnoreSet(Sets.set("metaClass")).createFastObjectMapperParser();
        }
    };


    private final ThreadLocal<JsonSerializer> serializer = new ThreadLocal<JsonSerializer>() {
        @Override
        protected JsonSerializer initialValue() {
            return new JsonSerializerFactory().setUseAnnotations(true)
                    .addFilter((parent, fieldAccess) -> !fieldAccess.name().equals("metaClass")).create();
        }
    };


    @Override
    public Object fromJson(String json) {
        return parser.get().parse(json);
    }

    @Override
    public <T> T fromJson(String json, Class<T> cls) {
        return parser.get().parse(cls, json);
    }

    @Override
    public <T> List<T> fromJsonArray(String json, Class<T> componentClass) {
        return parser.get().parseList(componentClass, json);
    }


    @Override
    public String toJson(Object object) {
        return serializer.get().serialize(object).toString();
    }

    @Override
    public <K, V> Map<K, V> fromJsonMap(String json, Class<K> componentClassKey, Class<V> componentClassValue) {

        throw new RuntimeException("Not supported yet");
    }


}
