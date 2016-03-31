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

package io.advantageous.qbit.json;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for JSON parsing.
 * QBit allows Jackson, GSON, or Boon to be plugged in as JSON serializer providers.
 * <p>
 * created by gcc on 10/14/14.
 */
public interface JsonMapper {

    /**
     * Convert a JSON string into one Java Object.
     *
     * @param json json
     * @return Java object
     */
    Object fromJson(String json);

    /**
     * Convert from json string using Class as a suggestion for how to do the parse.
     *
     * @param json json
     * @param cls  cls
     * @param <T>  Type
     * @return Java object of Type T
     */
    <T> T fromJson(String json, Class<T> cls);


    /**
     * Converts from a json string using componentClass as a guide to a List.
     *
     * @param json           json
     * @param componentClass componentClass
     * @param <T>            Type
     * @return List of Java objects of Type T.
     */
    <T> List<T> fromJsonArray(String json, Class<T> componentClass);

    /**
     * Converts from Object into JSON string.
     *
     * @param object object to convert to JSON.
     * @return json string
     */
    String toJson(Object object);

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
    <K, V> Map<K, V> fromJsonMap(String json, Class<K> componentClassKey, Class<V> componentClassValue);
}
