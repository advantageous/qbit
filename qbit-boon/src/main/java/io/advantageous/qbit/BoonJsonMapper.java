
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

import io.advantageous.qbit.json.JsonMapper;
import org.boon.Sets;
import org.boon.core.reflection.fields.FieldAccess;
import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.boon.json.JsonSerializer;
import org.boon.json.JsonSerializerFactory;
import org.boon.json.serializers.FieldFilter;

/**
 * Created by gcc on 10/15/14.
 *
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
            }).create();
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
