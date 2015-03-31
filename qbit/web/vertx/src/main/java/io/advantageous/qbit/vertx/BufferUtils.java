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

package io.advantageous.qbit.vertx;

import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.vertx.java.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

/**
 * @author rhightower on 1/26/15.
 */
public class BufferUtils {


    public static void writeString(final Buffer buffer, final String value) {

        byte[] string = value.getBytes(StandardCharsets.UTF_8);
        buffer.appendShort((short) string.length);
        buffer.appendBytes(string);

    }

    public static void writeMap(final Buffer buffer, final MultiMap<String, String> params) {
        buffer.appendShort((short) params.size());

        final Set<String> keys = params.keySet();

        for (String key : keys) {

            writeString(buffer, key);

            final Collection<String> values = (Collection<String>) params.getAll(key);
            buffer.appendShort((short) values.size());

            for (String value : values) {
                writeString(buffer, value);
            }
        }
    }

    public static String readString(final Buffer buffer, final int[] location) {

        final short size = buffer.getShort(location[0]);

        int start = location[0] + 2;
        int end = start + size;

        final String utf_8 = buffer.getString(start, end, StandardCharsets.UTF_8.displayName());

        location[0] = end;

        return utf_8;
    }

    public static MultiMap<String, String> readMap(Buffer buffer, int[] locationHolder) {


        int location = locationHolder[0];

        final short size = buffer.getShort(location);


        MultiMap<String, String> map = size > 0 ? new MultiMapImpl<>() : MultiMap.EMPTY;


        location += 2;

        locationHolder[0] = location;

        for (int index = 0; index < size; index++) {


            String key = readString(buffer, locationHolder);
            location = locationHolder[0];

            short valuesSize = buffer.getShort(location);
            location += 2;

            locationHolder[0] = location;

            for (int valueIndex = 0; valueIndex < valuesSize; valueIndex++) {

                String value = readString(buffer, locationHolder);
                map.add(key, value);

            }

        }
        return map;
    }


}
