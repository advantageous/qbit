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

    Object fromJson(String json);

    <T> T fromJson(String json, Class<T> cls);


    <T> List<T> fromJsonArray(String json, Class<T> componentClass);

    String toJson(Object object);

    <K,V> Map<K, V> fromJsonMap(String json, Class<K> componentClassKey, Class<V> componentClassValue);
}
