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

package io.advantageous.boon.collections;

import io.advantageous.boon.Maps;
import io.advantageous.boon.Sets;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.primitive.Arry;

import java.util.*;

/**
 * This maps only builds once you ask for a key for the first time.
 * It is designed to not incur the overhead of creating a map unless needed.
 */
public class LazyMap extends AbstractMap<String, Object> {


    final static boolean althashingThreshold = System.getProperty("jdk.map.althashing.threshold") != null;
    private final boolean delayMap;
    /* Holds the actual map that will be lazily created. */
    private Map<String, Object> map;
    /* The size of the map. */
    private int size;
    /* The keys  stored in the map. */
    private String[] keys;
    /* The values stored in the map. */
    private Object[] values;

    public LazyMap() {
        keys = new String[5];
        values = new Object[5];
        this.delayMap = false;
    }


    public LazyMap(int initialSize) {
        keys = new String[initialSize];
        values = new Object[initialSize];
        this.delayMap = false;

    }


    public LazyMap(int initialSize, boolean delayMap) {
        keys = new String[initialSize];
        values = new Object[initialSize];
        this.delayMap = delayMap;

    }

    public LazyMap(final List<String> keys, final List values, boolean delayMap) {

        this.keys = Arry.array(String.class, keys);
        this.values = Arry.array(Object.class, values);

        this.size = this.keys.length;

        this.delayMap = delayMap;

    }

    public Object put(String key, Object value) {
        if (map == null) {
            keys[size] = key;
            values[size] = value;
            size++;
            if (size == keys.length) {
                keys = Arry.grow(keys);
                values = Arry.grow(values);
            }
            return null;
        } else {
            return map.put(key, value);
        }
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        if (map != null) map.entrySet();

        if (delayMap) {

            return new FakeMapEntrySet(size, keys, values);
        } else {
            buildIfNeeded();
            return map.entrySet();
        }
    }

    @Override
    public int size() {
        if (map == null) {
            return size;
        } else {
            return map.size();
        }
    }

    @Override
    public boolean isEmpty() {
        if (map == null) {
            return size == 0;
        } else {
            return map.isEmpty();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        if (map == null) {
            throw new RuntimeException("wrong type of map");
        } else {
            return map.containsValue(value);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        buildIfNeeded();
        return map.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        buildIfNeeded();
        return map.get(key);
    }

    private void buildIfNeeded() {
        if (map == null) {

            /** added to avoid hash collision attack. */
            if (Sys.is1_7OrLater() && althashingThreshold) {
                map = new LinkedHashMap<>(size, 0.01f);
            } else {
                map = new TreeMap<>();
            }

            for (int index = 0; index < size; index++) {
                map.put(keys[index], values[index]);
            }
            this.keys = null;
            this.values = null;
        }
    }

    @Override
    public Object remove(Object key) {

        buildIfNeeded();
        return map.remove(key);

    }

    @Override
    public void putAll(Map m) {
        buildIfNeeded();
        map.putAll(m);
    }

    @Override
    public void clear() {
        if (map == null) {
            size = 0;
        } else {
            map.clear();
        }
    }

    @Override
    public Set<String> keySet() {


        if (map == null) {
            return Sets.set(size, keys);
        } else {
            return map.keySet();
        }

    }

    @Override
    public Collection<Object> values() {
        if (map == null) {
            return Arrays.asList(values);
        } else {
            return map.values();
        }

    }

    @Override
    public boolean equals(Object o) {
        buildIfNeeded();
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        buildIfNeeded();
        return map.hashCode();
    }

    @Override
    public String toString() {

        buildIfNeeded();
        return map.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        if (map == null) {
            return null;
        } else {
            if (map instanceof LinkedHashMap) {
                return ((LinkedHashMap) map).clone();
            } else {
                return Maps.copy(this);
            }
        }
    }

    public LazyMap clearAndCopy() {
        LazyMap map = new LazyMap(size);
        for (int index = 0; index < size; index++) {
            map.put(keys[index], values[index]);
        }
        size = 0;
        return map;
    }
}
