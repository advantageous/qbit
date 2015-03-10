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

package io.advantageous.boon;


import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.primitive.CharBuf;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.requireNonNull;

public class Maps {



    public static <V> List<V> lazyCreate( List<V> lazy ) {
        if (lazy == null) {

            lazy = new ArrayList<>();
        }
        return lazy;
    }

    public static <K,V> Map<K,V> lazyCreate( Map<K,V> lazy ) {
        if (lazy == null) {

            lazy = new LinkedHashMap();
        }
        return lazy;
    }


    public static <K,V> Map<K,V> lazyCreate( HashMap<K,V> lazy ) {
        if (lazy == null) {

            lazy = new HashMap();
        }
        return lazy;
    }


    public static <K,V> Map<K,V> lazyCreate( LinkedHashMap<K,V> lazy ) {
        if (lazy == null) {

            lazy = new LinkedHashMap();
        }
        return lazy;
    }


    public static <K,V> Map<K,V> lazyCreateLinked( Map<K,V> lazy ) {
        if (lazy == null) {

            lazy = new LinkedHashMap();
        }
        return lazy;
    }



    public static <K,V> Map<K,V> lazyCreate( ConcurrentHashMap<K,V> lazy ) {
        if (lazy == null) {

            lazy = new ConcurrentHashMap();
        }
        return lazy;
    }

    public static <K,V> Map<K,V> lazyCreateSafe( Map<K,V> lazy ) {
        if (lazy == null) {

            lazy = new ConcurrentHashMap();
        }
        return lazy;
    }


    @Universal
    public static int lengthOf( Map<?, ?> map ) {
        return len ( map );
    }

    @Universal
    public static <K, V> V atIndex( Map<K, V> map, K k ) {
        return idx(map, k );
    }

    @Universal
    public static <K, V> SortedMap<K, V> sliceOf( NavigableMap<K, V> map, K startIndex, K endIndex ) {
        return slc(map, startIndex, endIndex);
    }


    @Universal
    public static <K, V> SortedMap<K, V> endSliceOf( NavigableMap<K, V> map, K fromKey ) {
        return slcEnd(map, fromKey);
    }

        /**
         * Universal methods.
         */
    @Universal
    public static int len( Map<?, ?> map ) {
        return map.size();
    }


    @Universal
    public static <K, V> boolean in( K key, Map<K, V> map ) {
        return map.containsKey( key );
    }

    @Universal
    public static <K, V> void add( Map<K, V> map, Entry<K, V> entry ) {
        map.put( entry.key(), entry.value() );
    }

    @Universal
    public static <K, V> V idx( Map<K, V> map, K k ) {
        return map.get( k );
    }

    @Universal
    public static <K, V> void idx( Map<K, V> map, K k, V v ) {
        map.put( k, v );
    }



    public static <K, V> String idxStr( Map<K, V> map, K k ) {
        return Str.toString(map.get( k ));
    }


    public static <K, V> Integer idxInt( Map<K, V> map, K k ) {
        return (Integer)map.get( k );
    }



    public static <K, V> Long idxLong( Map<K, V> map, K k ) {
        return (Long)map.get( k );
    }


    public static <K, V> Map idxMap( Map<K, V> map, K k ) {
        return  (Map) map.get( k );
    }


    public static <K, V> List idxList( Map<K, V> map, K k ) {
        return  (List) map.get( k );
    }



    public static <K, V> long toLong( Map<K, V> map, K key ) {
        V value = map.get(key);
        long l = Conversions.toLong(value, Long.MIN_VALUE);
        if ( l == Long.MIN_VALUE ) {
            Exceptions.die("Cannot convert", key, "into long value", value);
        }
        return l;
    }


    public static <K, V> int toInt( Map<K, V> map, K key ) {
        V value = map.get(key);
        int v = Conversions.toInt ( value, Integer.MIN_VALUE );
        if ( v == Integer.MIN_VALUE ) {
            Exceptions.die("Cannot convert", key, "into int value", value);
        }
        return v;
    }

    @Universal
    public static <K, V> SortedMap<K, V> copy( SortedMap<K, V> map ) {
        if ( map instanceof TreeMap ) {
            return new TreeMap<>( map );
        } else if ( map instanceof ConcurrentSkipListMap ) {
            return new ConcurrentSkipListMap<>( map );
        } else {
            return new TreeMap<>( map );
        }
    }

    @Universal
    public static <K, V> Map<K, V> copy( Map<K, V> map ) {
        if ( map instanceof LinkedHashMap ) {
            return new LinkedHashMap<>( map );
        } else if ( map instanceof ConcurrentHashMap ) {
            return new ConcurrentHashMap<>( map );
        } else {
            return new LinkedHashMap<>( map );
        }
    }


    /** Grabs the first value from a tree map (Navigable map). */
    @Universal
    public static <K, V> V first( NavigableMap<K, V> map ) {
        return map.firstEntry().getValue();
    }



    /** Grabs the last value from a tree map (Navigable map). */
    @Universal
    public static <K, V> V last( NavigableMap<K, V> map ) {
        return map.lastEntry().getValue()   ;
    }



    /** Grabs the value after this key from a tree map (Navigable map). */
    @Universal
    public static <K, V> V after( NavigableMap<K, V> map, final K index ) {
        return map.get( map.higherKey( index ) );
    }

    /** Grabs the value before this key from a tree map (Navigable map). */
    @Universal
    public static <K, V> V before( NavigableMap<K, V> map, final K index ) {
        return map.get( map.lowerKey( index ) );
    }


    @Universal
    public static <K, V> SortedMap<K, V> slc( NavigableMap<K, V> map, K startIndex, K endIndex ) {
        return map.subMap( startIndex, endIndex );
    }


    @Universal
    public static <K, V> SortedMap<K, V> slcEnd( NavigableMap<K, V> map, K fromKey ) {
        return map.tailMap( fromKey );
    }

    @Universal
    public static <K, V> SortedMap<K, V> slc( NavigableMap<K, V> map, K toKey ) {
        return map.headMap( toKey );
    }

    /**
     * End universal methods.
     */

    public static <K, V> boolean valueIn( V value, Map<K, V> map ) {
        return map.containsValue( value );
    }


    public static <K, V> Entry<K, V> entry( final K k, final V v ) {
        return new Pair<>( k, v );
    }

    public static <K, V> Entry<K, V> entry( Entry<K, V> entry ) {
        return new Pair<>( entry );
    }

    public static Map<?, ?> mapFromArray( Object... args ) {
        Map<Object, Object> map = map(Object.class, Object.class);

        if (args.length % 2 != 0) {
            return Exceptions.die(Map.class, "mapFromArray arguments must be equal");
        }

        Object lastKey = null;
        for (int index = 0; index < args.length; index++) {

            if (index % 2 == 0) {
                lastKey = args[index];
            } else {
                map.put( lastKey, args[index] );
            }

        }
        return map;

    }


    public static <K, V> Map<K, V> map( Class<K> keyClass, Class<V> valueClass ) {
        return new LinkedHashMap<>( 10 );
    }

    public static <K, V> Map<K, V> safeMap( Class<K> keyClass, Class<V> valueClass ) {
        return new ConcurrentHashMap<>( 10 );
    }

    public static <K, V> Map<K, V> map( K k0, V v0 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        return map;
    }


    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9 ) {
        Map<K, V> map = new LinkedHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        return map;
    }


    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10 ) {
        Map<K, V> map = new LinkedHashMap<>( 11 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );

        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11 ) {
        Map<K, V> map = new LinkedHashMap<>( 12 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );

        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12 ) {
        Map<K, V> map = new LinkedHashMap<>( 13 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );

        return map;
    }



    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13 ) {
        Map<K, V> map = new LinkedHashMap<>( 14 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );

        return map;
    }


    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13,
                                        K k14, V v14) {
        Map<K, V> map = new LinkedHashMap<>( 15 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );
        map.put( k14, v14 );

        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13,
                                        K k14, V v14, K k15, V v15) {
        Map<K, V> map = new LinkedHashMap<>( 16 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );
        map.put( k14, v14 );
        map.put( k15, v15 );

        return map;
    }


    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13,
                                        K k14, V v14, K k15, V v15, K k16, V v16) {
        Map<K, V> map = new LinkedHashMap<>( 17 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );
        map.put( k14, v14 );
        map.put( k15, v15 );
        map.put( k16, v16 );

        return map;
    }


    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13,
                                        K k14, V v14, K k15, V v15, K k16, V v16, K k17, V v17) {
        Map<K, V> map = new LinkedHashMap<>( 18 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );
        map.put( k14, v14 );
        map.put( k15, v15 );
        map.put( k16, v16 );
        map.put( k17, v17 );


        return map;
    }

    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13,
                                        K k14, V v14, K k15, V v15, K k16, V v16, K k17, V v17,
                                        K k18, V v18) {
        Map<K, V> map = new LinkedHashMap<>( 19 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );
        map.put( k14, v14 );
        map.put( k15, v15 );
        map.put( k16, v16 );
        map.put( k17, v17 );
        map.put( k18, v18 );

        return map;
    }


    public static <K, V> Map<K, V> map( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                        V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                        K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13,
                                        K k14, V v14, K k15, V v15, K k16, V v16, K k17, V v17,
                                        K k18, V v18, K k19, V v19) {
        Map<K, V> map = new LinkedHashMap<>( 20 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        map.put( k10, v10 );
        map.put( k11, v11 );
        map.put( k12, v12 );
        map.put( k13, v13 );
        map.put( k14, v14 );
        map.put( k15, v15 );
        map.put( k16, v16 );
        map.put( k17, v17 );
        map.put( k18, v18 );
        map.put( k19, v19 );

        return map;
    }





    public static <K, V> Map<K, V> map( List<K> keys, List<V> values ) {
        Map<K, V> map = new LinkedHashMap<>( 10 + keys.size() );
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }


    public static <K, V> Map<K, V> map( LinkedHashSet<K> keys, LinkedHashSet<V> values ) {
        Map<K, V> map = new LinkedHashMap<>( 10 + keys.size() );
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    /**
     * Note, you need to make sure that the iterators are from some sort of ordered collection.
     */
    public static <K, V> Map<K, V> map( Iterable<K> keys, Iterable<V> values ) {
        Map<K, V> map = new LinkedHashMap<>();
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> Map<K, V> map( K[] keys, V[] values ) {

        Map<K, V> map = new LinkedHashMap<>( 10 + keys.length );
        int index = 0;
        for ( K k : keys ) {
            if ( index < keys.length ) {
                V v = values[ index ];
                map.put( k, v );
            } else {
                map.put( k, null );
            }
            index++;
        }
        return map;
    }


    @SafeVarargs
    public static <K, V> Map<K, V> map( Entry<K, V>... entries ) {
        Map<K, V> map = new LinkedHashMap<>( entries.length );
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    @SafeVarargs
    public static <K, V> Map<K, V> mapByEntries( Entry<K, V>... entries ) {
        Map<K, V> map = new LinkedHashMap<>( entries.length );
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                                       K k9, V v9 ) {
        NavigableMap<K, V> map = new TreeMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Collection<K> keys, Collection<V> values ) {
        NavigableMap<K, V> map = new TreeMap<>();
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> sortedMap( Iterable<K> keys, Iterable<V> values ) {
        NavigableMap<K, V> map = new TreeMap<>();
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> sortedMap( K[] keys, V[] values ) {

        NavigableMap<K, V> map = new TreeMap<>();
        int index = 0;
        for ( K k : keys ) {
            if ( index < keys.length ) {
                V v = values[ index ];
                map.put( k, v );
            } else {
                map.put( k, null );
            }
            index++;
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> sortedMap( List<Entry<K, V>> entries ) {
        NavigableMap<K, V> map = new TreeMap<>();
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    //

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                       V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                                       K k9, V v9 ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, Collection<K> keys, Collection<V> values ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, K[] keys, V[] values ) {

        NavigableMap<K, V> map = new TreeMap<>( comparator );
        int index = 0;
        for ( K k : keys ) {
            if ( index < keys.length ) {
                V v = values[ index ];
                map.put( k, v );
            } else {
                map.put( k, null );
            }
            index++;
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> sortedMap( Comparator<K> comparator, List<Entry<K, V>> entries ) {
        NavigableMap<K, V> map = new TreeMap<>( comparator );
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    public static <K, V> Map<K, V> safeMap( Map<K, V> map ) {
        return new ConcurrentHashMap<>(map);
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        return map;
    }


    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3, K k4, V v4 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3, K k4, V v4, K k5, V v5 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3, K k4, V v4, K k5, V v5, K k6, V v6 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                            V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                            K k9, V v9 ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        return map;
    }

    public static <K, V> Map<K, V> safeMap( Collection<K> keys, Collection<V> values ) {
        Map<K, V> map = new ConcurrentHashMap<>( 10 + keys.size() );
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> Map<K, V> safeMap( Iterable<K> keys, Iterable<V> values ) {
        Map<K, V> map = new ConcurrentHashMap<>();
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> Map<K, V> safeMap( K[] keys, V[] values ) {

        Map<K, V> map = new ConcurrentHashMap<>( 10 + keys.length );
        int index = 0;
        for ( K k : keys ) {
            if ( index < keys.length ) {
                V v = values[ index ];
                map.put( k, v );
            } else {
                map.put( k, null );
            }
            index++;
        }
        return map;
    }


    @SafeVarargs
    public static <K, V> Map<K, V> safeMap( Entry<K, V>... entries ) {
        Map<K, V> map = new ConcurrentHashMap<>( entries.length );
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        return map;
    }


    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                                           K k9, V v9 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Collection<K> keys, Collection<V> values ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Iterable<K> keys, Iterable<V> values ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( K[] keys, V[] values ) {

        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        int index = 0;
        for ( K k : keys ) {
            if ( index < keys.length ) {
                V v = values[ index ];
                map.put( k, v );
            } else {
                map.put( k, null );
            }
            index++;
        }
        return map;
    }


    @SafeVarargs
    public static <K, V> NavigableMap<K, V> safeSortedMap( Entry<K, V>... entries ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>();
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
                                                           V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                                                           K k9, V v9 ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        map.put( k0, v0 );
        map.put( k1, v1 );
        map.put( k2, v2 );
        map.put( k3, v3 );
        map.put( k4, v4 );
        map.put( k5, v5 );
        map.put( k6, v6 );
        map.put( k7, v7 );
        map.put( k8, v8 );
        map.put( k9, v9 );
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, Collection<K> keys, Collection<V> values ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        Iterator<V> iterator = values.iterator();
        for ( K k : keys ) {
            if ( iterator.hasNext() ) {
                V v = iterator.next();
                map.put( k, v );
            } else {
                map.put( k, null );
            }
        }
        return map;
    }

    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, K[] keys, V[] values ) {

        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        int index = 0;
        for ( K k : keys ) {
            if ( index < keys.length ) {
                V v = values[ index ];
                map.put( k, v );
            } else {
                map.put( k, null );
            }
            index++;
        }
        return map;
    }


    public static <K, V> NavigableMap<K, V> safeSortedMap( Comparator<K> comparator, List<Entry<K, V>> entries ) {
        NavigableMap<K, V> map = new ConcurrentSkipListMap<>( comparator );
        for ( Entry<K, V> entry : entries ) {
            map.put( entry.key(), entry.value() );
        }
        return map;
    }


    @SuppressWarnings ( { "unchecked", "rawtypes" } )
    public static <T> T idx( Class<T> clz, Map map, Object key ) {
        Object value = map.get( key.toString() );
        if ( value == null ) {
            return ( T ) value;
        }
        if ( value.getClass() != clz ) {
            T t = Conversions.coerce( clz, value );
            return t;
        } else {
            return ( T ) value;
        }
    }


    public static <T> T fromMap( Map<String, Object> map, Class<T> clazz ) {
        return MapObjectConversion.fromMap(map, clazz);
    }

    public static Object fromMap( final Map<String, Object> map ) {
        return MapObjectConversion.fromMap ( map );
    }


    public static Map<String, Object> toMap( final Object object ) {
        return MapObjectConversion.toMap ( object );
    }


    public static <T> Map<String, List<T>> toMultiValueMap( final String propertyPath, final Collection<T> collection ) {
        LinkedHashMap<String, List<T>> map = new LinkedHashMap<>( collection.size() );

        for ( T item : collection ) {
            Object oKey = BeanUtils.idx(item, propertyPath);
            if ( oKey == null ) {
                continue;
            }
            String key = Conversions.coerce( Typ.string, oKey );

            List<T> list = map.get( key );
            if ( list == null ) {
                list = new ArrayList<>();
                map.put( key, list );
            }
            list.add( item );

        }
        return map;

    }


    public static <T> Map<String, T> toMap( final String propertyPath, final Collection<T> collection ) {
        return toMap( Typ.string, propertyPath, collection );
    }

    public static <T> NavigableMap<String, T> toSortedMap( final String propertyPath, final Collection<T> collection ) {
        return toSortedMap( Typ.string, propertyPath, collection );
    }

    public static <T> NavigableMap<String, T> toSafeSortedMap( final String propertyPath, final Collection<T> collection ) {
        return toSafeSortedMap( Typ.string, propertyPath, collection );
    }

    public static <T> Map<String, T> toSafeMap( final String propertyPath, final Collection<T> collection ) {
        return toSafeMap( Typ.string, propertyPath, collection );
    }


    public static <K, T> Map<K, T> toMap( Class<K> keyType, final String propertyPath, final Collection<T> collection ) {
        LinkedHashMap<K, T> map = new LinkedHashMap<>( collection.size() );
        doPopulateMapWithCollectionAndPropPath( keyType, propertyPath, collection, map );
        return map;
    }

    public static <K, T> NavigableMap<K, T> toSortedMap( Class<K> keyType, final String propertyPath, final Collection<T> collection ) {
        TreeMap<K, T> map = new TreeMap<>();
        doPopulateMapWithCollectionAndPropPath( keyType, propertyPath, collection, map );
        return map;
    }

    public static <K, T> NavigableMap<K, T> toSafeSortedMap( Class<K> keyType, final String propertyPath, final Collection<T> collection ) {
        ConcurrentSkipListMap<K, T> map = new ConcurrentSkipListMap<>();
        doPopulateMapWithCollectionAndPropPath( keyType, propertyPath, collection, map );
        return map;
    }

    public static <K, T> Map<K, T> toSafeMap( Class<K> keyType, final String propertyPath, final Collection<T> collection ) {
        ConcurrentHashMap<K, T> map = new ConcurrentHashMap<>();
        doPopulateMapWithCollectionAndPropPath( keyType, propertyPath, collection, map );
        return map;
    }


    private static <K, T> void doPopulateMapWithCollectionAndPropPath( Class<K> keyType, String propertyPath, Collection<T> collection, Map<K, T> map ) {
        for ( T item : collection ) {
            Object oKey = BeanUtils.idx( item, propertyPath );
            if ( oKey == null ) {
                continue;
            }
            K key = Conversions.coerce( keyType, oKey );
            map.put( key, item );

        }
    }


    public static  <K, V> void copyKeys(Collection<K> keys, Map<K, V> sourceMap, Map<K,V> destinationMap) {
        for (K key : keys) {
            V value = sourceMap.get(key);
            if (value != null) {
                destinationMap.put(key, value);
            }
        }
    }

    public static <K, V> Map<K, V> copyKeys(Collection<K> keys, Map<K, V> sourceMap) {
        Map<K,V> destinationMap = new ConcurrentHashMap<>();
        for (K key : keys) {
            V value = sourceMap.get(key);
            if (value != null) {
                destinationMap.put(key, value);
            }
        }
        return destinationMap;
    }


    public static String asPrettyJsonString(Map<String, Object> map) {
        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintMap(map).toString();
    }

    public static Map <String, Object> toPrettyMap(Object object) {
        return MapObjectConversion.toPrettyMap(object);
    }
}
