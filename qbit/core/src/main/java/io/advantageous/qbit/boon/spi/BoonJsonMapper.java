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

package io.advantageous.qbit.boon.spi;

import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Sets;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.Mapper;
import io.advantageous.boon.core.reflection.MapperComplex;
import io.advantageous.boon.core.reflection.fields.FieldAccessMode;
import io.advantageous.boon.core.value.ValueContainer;
import io.advantageous.boon.core.value.ValueMap;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.qbit.json.JsonMapper;

import java.util.*;
import java.util.function.Consumer;

/**
 * JsonMapper is the primary interface to provide JSON serialization and deserialization for QBit.
 * The default implementation of the JsonMapper is BoonJsonMapper.
 * <p>
 * BoonJsonMapper is thread safe.
 * created by gcc on 10/15/14.
 *
 * @author Rick Hightower
 */
public class BoonJsonMapper implements JsonMapper {

    /**
     * Holds the JsonParserAndMapper parser to parse JSON.
     */
    private final ThreadLocal<JsonParserAndMapper> parser = new ThreadLocal<JsonParserAndMapper>() {
        @Override
        protected JsonParserAndMapper initialValue() {
            return new JsonParserFactory().setIgnoreSet(Sets.set("metaClass")).createFastObjectMapperParser();
        }
    };


    /**
     * Holds the JsonSerializer to deserialize JSON into Java objects.
     */
    private final ThreadLocal<JsonSerializer> serializer = new ThreadLocal<JsonSerializer>() {
        @Override
        protected JsonSerializer initialValue() {
            return new JsonSerializerFactory().setUseAnnotations(true)
                    .addFilter((parent, fieldAccess) -> !fieldAccess.name().equals("metaClass")).create();
        }
    };


    /**
     * Holds the Mapper to convert Maps into Java objects.
     */
    private final ThreadLocal<Mapper> mapper = new ThreadLocal<Mapper>() {
        @Override
        protected Mapper initialValue() {

            /**
             * MapperComplex(FieldAccessMode fieldAccessType, boolean useAnnotations,
             boolean caseInsensitiveFields, Set<String> ignoreSet,
             String view, boolean respectIgnore, boolean acceptSingleValueAsArray) {
             fieldsAccessor = FieldAccessMode.create( fieldAccessType, useAnnotations, caseInsensitiveFields );
             this.ignoreSet = ignoreSet;
             this.view = view;
             this.respectIgnore = respectIgnore;
             this.acceptSingleValueAsArray = acceptSingleValueAsArray;
             this.outputType = true;
             }
             */
            return new MapperComplex(false, FieldAccessMode.PROPERTY_THEN_FIELD, true, false, Collections.emptySet(), null, true, true);
        }
    };


    /**
     * Convert a JSON string into one Java Object.
     *
     * @param json json
     * @return Java object
     */
    @Override
    public Object fromJson(String json) {
        return parser.get().parse(json);
    }


    /**
     * Convert from json string using Class as a suggestion for how to do the parse.
     *
     * @param json json
     * @param cls  cls
     * @param <T>  Type
     * @return Java object of Type T
     */
    @Override
    public <T> T fromJson(String json, Class<T> cls) {
        return parser.get().parse(cls, json);
    }


    /**
     * Converts from a json string using componentClass as a guide to a List.
     *
     * @param json           json
     * @param componentClass componentClass
     * @param <T>            Type
     * @return List of Java objects of Type T.
     */
    @Override
    public <T> List<T> fromJsonArray(String json, Class<T> componentClass) {
        return parser.get().parseList(componentClass, json);
    }


    /**
     * Converts from Object into JSON string.
     *
     * @param object object to convert to JSON.
     * @return json string
     */
    @Override
    public String toJson(Object object) {
        return serializer.get().serialize(object).toString();
    }


    /**
     * Converts from a json string using componentClassKey and componentClassValue as a guide to a Map.
     *
     * @param json                json string
     * @param componentClassKey   componentClassKey type of Key
     * @param componentClassValue componentClassValue type of value
     * @param <K>                 K type of map key
     * @param <V>                 V type of map value
     * @return Map
     */
    @Override
    public <K, V> Map<K, V> fromJsonMap(String json, Class<K> componentClassKey, Class<V> componentClassValue) {
        Map map = (Map) parser.get().parse(json);
        final Mapper mapper = this.mapper.get();
        return extractMap(componentClassKey, componentClassValue, map, mapper);
    }

    private <K, V> Map<K, V> extractMap(Class<K> componentClassKey, Class<V> componentClassValue, Map<Object, Object> map, Mapper mapper) {
        final Map<K, V> results = new TreeMap<>();

        /* Convert each entry give the componentClassKey and the componentClassValue. */
        map.entrySet().forEach(entry -> {

            /** value. */
            final Object value = entry.getValue() instanceof ValueContainer ? ((ValueContainer) entry.getValue()).toValue() : entry.getValue();
            /** key */
            final Object key = entry.getKey() instanceof ValueContainer ? ((ValueContainer) entry.getKey()).toValue() : entry.getKey();
            /** Converted key. */
            final K convertedKey;
            /** Converted value. */
            final V convertedValue;

            /** If the key is a map then convert it into the type with the mapper. */
            if (key instanceof Map) {
                convertedKey = mapper.fromMap(((Map<String, Object>) key), componentClassKey);
            } else {

                /** If the key is not a map then convert it into the type with Conversions. */
                convertedKey = Conversions.coerce(componentClassKey, key);
            }

            if (value instanceof Map) {

                /** If the value is a map use the mapper to convert to an object unless the
                 * componentClassValue is Object then just convert to a map of basic types.
                 */
                if (!(componentClassValue == Object.class)) {
                    convertedValue = mapper.fromMap(((Map<String, Object>) value), componentClassValue);
                } else {
                    /**
                     * componentClassValue is Object to convert it to a regular map.
                     */
                    if (value instanceof ValueMap) {
                        convertedValue = convertToMap((ValueMap) value);
                    } else {
                        convertedValue = (V) value;
                    }
                }
            } else if (value instanceof List) {


                convertedValue = (V) convertList(value, mapper);


            } else {
                /** We are not a map so just convert normally. */
                if (!(componentClassValue == Object.class)) {
                    convertedValue = Conversions.coerce(componentClassValue, value);
                } else {
                    /** Unless componentClassValue is Object then we want to pull out the values. */
                    if (value instanceof Value) {
                        convertedValue = (V) ((Value) value).toValue();
                    } else {
                        convertedValue = (V) value;
                    }
                }
            }

            results.put(convertedKey, convertedValue);

        });

        return results;
    }

    private Object convertList(Object value, Mapper mapper) {

        final List list = (List) value;
        final Object convertedValue;
        if (list.size() == 0) {
            convertedValue = list;
        } else {
            final List convertedList = new ArrayList(list.size());

            list.forEach(item -> {

                final Object itemValue;

                if (item instanceof ValueContainer) {
                    itemValue = ((ValueContainer) item).toValue();
                } else {
                    itemValue = item;
                }

                final Object newItemValue;
                /** We are not a map so just convert normally. */
                if (itemValue instanceof List) {
                    newItemValue = convertList(itemValue, mapper);
                } else if (itemValue instanceof Map) {
                    Map m = ((Map) itemValue);

                    if (m instanceof ValueMap) {
                        newItemValue = convertToMap(((ValueMap) m));
                    } else {
                        newItemValue = extractMap(String.class, Object.class, m, mapper);
                    }

                } else {
                    /** Unless componentClassValue is Object then we want to pull out the values. */
                    if (itemValue instanceof Value) {
                        newItemValue = ((Value) itemValue).toValue();
                    } else {
                        newItemValue = itemValue;
                    }
                }
                convertedList.add(newItemValue);
            });
            convertedValue = convertedList;
        }
        return convertedValue;
    }

    /**
     * Helper method.
     * Converts a value map into a regular map of Java basic types.
     *
     * @param valueMap valueMap
     * @param <V>      V
     * @return regular map
     */
    private <V> V convertToMap(ValueMap valueMap) {
        final Map<String, Object> map = new LinkedHashMap<>(valueMap.size());

        valueMap.entrySet().forEach(new Consumer<Map.Entry<String, Object>>() {
            @Override
            public void accept(Map.Entry<String, Object> entry) {

                Object value = entry.getValue();

                /* If the value is a value container then grab what is inside. */
                if (value instanceof ValueContainer) {
                    ValueContainer valueContainer = ((ValueContainer) entry.getValue());
                    value = valueContainer.toValue();
                }

                /* If value is a Value then pull the real value. */
                if (value instanceof Value) {
                    map.put(entry.getKey(), ((Value) value).toValue());
                } else if (value instanceof ValueMap) {
                    /* If value is a value map then convert it into a regular map. */
                    map.put(entry.getKey(), convertToMap(((ValueMap) value)));
                } else if (value instanceof List) {
                    map.put(entry.getKey(), convertList(value, mapper.get()));
                } else {
                    map.put(entry.getKey(), value);
                }

            }
        });

        return (V) map;
    }


}
