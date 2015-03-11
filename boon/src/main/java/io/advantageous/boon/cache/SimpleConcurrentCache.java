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

package io.advantageous.boon.cache;


import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.primitive.Int;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Uses striping to allow more than one thread to operate on the cache at a time, but
 * due to the nature of the independent tracking of each strip the exact LRUness liveness
 * is only an approximation. The tradeoff is speed for LRU approximation.
 *
 * Basically the very least recently used is not always harvested but one of the stripes
 * will harvest one of the least recently used.
 *
 * So if you cache size was 10,000 and you had 8 CPUs, then a reaping would only
 * get one of the least recently used, but maybe not the most least but within the least +-8.
 *
 * @param <K> Key
 * @param <V> Value
 */
public class SimpleConcurrentCache<K, V> implements Cache<K, V> {

    /** Cache regions.*/
    final SimpleCache<K, V>[] cacheRegions;

    private static final boolean useFastHash;

    private transient final int hashSeed = randomHashSeed( this );



    /**
     * Cache class
     * @param <K> key
     * @param <V> value
     */
    private static class SimpleThreadSafeCache<K, V> extends SimpleCache<K, V> {
        private final ReadWriteLock readWriteLock;

        SimpleThreadSafeCache( final int limit, CacheType type, boolean fair ) {

            super( limit, type );
            readWriteLock = new ReentrantReadWriteLock( fair );
        }


        @Override
        public void put( K key, V value ) {
            readWriteLock.writeLock().lock();
            try {

                super.put( key, value );
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }


        @Override
        public V get( K key ) {
            readWriteLock.writeLock().lock();
            V value;

            try {

                value = super.get( key );
            } finally {
                readWriteLock.writeLock().unlock();
            }
            return value;
        }

        @Override
        public void remove( K key ) {

            readWriteLock.writeLock().lock();

            try {

                super.remove( key );
            } finally {
                readWriteLock.writeLock().unlock();
            }

        }

        public V getSilent( K key ) {
            readWriteLock.writeLock().lock();

            V value;


            try {
                value = super.getSilent( key );
            } finally {
                readWriteLock.writeLock().unlock();
            }

            return value;

        }

        public int size() {
            readWriteLock.readLock().lock();
            int size = -1;
            try {
                size = super.size();
            } finally {
                readWriteLock.readLock().unlock();
            }
            return size;
        }

        public String toString() {
            readWriteLock.readLock().lock();
            String str;
            try {
                str = super.toString();
            } finally {
                readWriteLock.readLock().unlock();
            }
            return str;
        }


    }


    /**
     * New
     * @param limit limit of the cache size
     */
    public SimpleConcurrentCache( final int limit ) {
        this( limit, false, CacheType.LRU );
    }

    /**
     * New
     * @param limit limit of the cache size
     * @param type type of cache
     */
    public SimpleConcurrentCache( final int limit, CacheType type ) {
        this( limit, false, type );
    }

    /**
     * Limit of hte cache size
     * @param limit limit of the cache size
     * @param fair should we be fair?
     * @param type type of cache
     */
    public SimpleConcurrentCache( final int limit, boolean fair, CacheType type ) {
        int cores = Runtime.getRuntime().availableProcessors();
        int stripeSize = cores < 2 ? 8 : cores * 4;
        stripeSize = Int.roundUpToPowerOf2(stripeSize);
        cacheRegions = new SimpleCache[ stripeSize ];
        for ( int index = 0; index < cacheRegions.length; index++ ) {
            cacheRegions[ index ] = new SimpleThreadSafeCache<>( limit / cacheRegions.length, type, fair );
        }
    }

    /**
     *
     * The more stripes the less accurate the LRU but the faster the access.
     * Life is all about engineering trade-offs.
     *
     * 100,000 entries ok with 100 stripes, but 10 entries not so cool with 10 stripes.
     *
     * Try to keep the accuracy in the 1% to 5% range.
     *
     * @param concurrency how many stripes
     * @param limit limit of the cache size
     * @param fair should we be fair?
     * @param type type of cache
     */
    public SimpleConcurrentCache( final int concurrency, final int limit, boolean fair, CacheType type ) {


        final int stripeSize = Int.roundUpToPowerOf2(concurrency);
        cacheRegions = new SimpleCache[ stripeSize ];
        for ( int index = 0; index < cacheRegions.length; index++ ) {
            cacheRegions[ index ] = new SimpleThreadSafeCache<>( limit / cacheRegions.length, type, fair );
        }
    }

    /**
     * The more stripes the less accurate the LRU but the faster the access.
     * Life is all about engineering trade-offs.
     *
     * @param concurrency how many stripes
     * @param limit limit of the cache size
     * @param fair should we be fair?
     */
    public SimpleConcurrentCache( final int concurrency, final int limit, boolean fair ) {


        final int stripeSize = Int.roundUpToPowerOf2(concurrency);
        cacheRegions = new SimpleCache[ stripeSize ];
        for ( int index = 0; index < cacheRegions.length; index++ ) {
            cacheRegions[ index ] = new SimpleThreadSafeCache<>( limit / cacheRegions.length, CacheType.LRU, fair );
        }
    }

    /** Get the map for this region. */
    private SimpleCache<K, V> map( K key ) {
        return cacheRegions[ stripeIndex( key ) ];
    }

    /**
     * Put the key in.
     * @param key the key
     * @param value the value
     */
    @Override
    public void put( K key, V value ) {

        map( key ).put( key, value );
    }

    /**
     * Take the key out.
     * @param key the key
     * @return value
     */
    @Override
    public V get( K key ) {
        return map( key ).get( key );
    }


    /**
     * for testing only.
     * @param key key to get the value with
     * @return the value
     */
    @Override
    public V getSilent( K key ) {
        return map( key ).getSilent( key );

    }

    /**
     * Remove the key
     * @param key the key
     */
    @Override
    public void remove( K key ) {
        map( key ).remove( key );
    }

    /** Get the size of the cache.
     *  This is not 100% accurate if cache is being concurrenly accessed.
     */
    @Override
    public int size() {
        int size = 0;
        for ( SimpleCache<K, V> cache : cacheRegions ) {
            size += cache.size();
        }
        return size;
    }

    /**
     * toString
     * @return string
     */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        for ( SimpleCache<K, V> cache : cacheRegions ) {
            builder.append( cache.toString() ).append( '\n' );
        }

        return builder.toString();
    }


    static final MethodAccess randomHashSeedMethod;

    static {

        boolean yes;
        MethodAccess randomHashSeed = null;
        try {
            Class cls = Class.forName( "sun.misc.Hashing" );

            ClassMeta classMeta = ClassMeta.classMeta(cls);

            yes = classMeta.respondsTo("randomHashSeed", Object.class)
                    && classMeta.classMethods().contains("randomHashSeed");

            randomHashSeed = classMeta.method("randomHashSeed");


        } catch ( Exception ex ) {
            yes = false;
        }

        useFastHash = yes;
        randomHashSeedMethod = randomHashSeed;
    }


    /** Create a hash seed.
     *
     * @param instance for me?
     * @return hash seed
     */
    private static int randomHashSeed( SimpleConcurrentCache instance ) {


        if ( useFastHash ) {
            //return sun.misc.Hashing.randomHashSeed( instance );
            return (int) randomHashSeedMethod.invoke(instance);
        }

        return 0;
    }


    /**
     * Calculate the hash.
     * @param k key
     * @return hash
     */
    private final int hash( Object k ) {
        int h = hashSeed;

        h ^= k.hashCode();

        h ^= ( h >>> 20 ) ^ ( h >>> 12 );
        return h ^ ( h >>> 7 ) ^ ( h >>> 4 );
    }


    /**
     * Returns index for hash code h.
     */
    static int indexFor( int h, int length ) {
        return h & ( length - 1 );
    }


    /**
     * Striping
     * @param key key to stripe
     * @return stripe
     */
    private int stripeIndex( K key ) {
        return indexFor( hash( key ), cacheRegions.length );
    }


}
