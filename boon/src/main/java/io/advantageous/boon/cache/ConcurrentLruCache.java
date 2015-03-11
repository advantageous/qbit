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

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * ConcurrentLruCache cache.
 * This has the limitation of using a single lock to update live status of key.
 *
 * @param <KEY> the key
 * @param <VALUE> the value
 */
public class ConcurrentLruCache<KEY, VALUE> implements Cache<KEY, VALUE> {



    /**
     * Map to hold the cache values
     */
    private final Map<KEY, VALUE> map = new ConcurrentHashMap<>();

    /** Queue to hold keys in the LRU cache.
     */
    private final Deque<KEY> queue = new ConcurrentLinkedDeque<>();

    /** Limit the amount you can hold in the map. */
    private final int limit;


    /** Creates an LRU Cache with a given limit.
     * @param limit limit
     */
    public ConcurrentLruCache( final int limit ) {
        this.limit = limit;
    }

    /**
     * Key
     * @param key the key
     * @param value the value
     */
    @Override
    public void put( KEY key, VALUE value ) {
        VALUE oldValue = map.put( key, value );
        if ( oldValue != null ) {
            removeThenAddKey( key );
        } else {
            addKey( key );
        }
        if ( map.size() > limit ) {
            map.remove( removeLast() );
        }
    }


    /**
     * Get the value at key
     * @param key the key
     * @return value
     */
    @Override
    public VALUE get( KEY key ) {
        removeThenAddKey( key );
        return map.get( key );
    }


    /**
     * Get the key without updating the LRU status for testing
     * @param key key
     * @return value
     */
    @Override
    public VALUE getSilent( KEY key ) {
        return map.get( key );
    }

    /**
     * Remove the key.
     * @param key the key
     */
    @Override
    public void remove( KEY key ) {
        removeFirstOccurrence( key );
        map.remove( key );
    }

    /**
     * Size of the cache.
     * @return size
     */
    @Override
    public int size() {
        return map.size();
    }


    /** Add a key. */
    private void addKey( KEY key ) {
            queue.addFirst(key);
      }

    /** Remove the last key. */
    private KEY removeLast() {
            final KEY removedKey = queue.removeLast();
            return removedKey;
     }

    /**
     * This removes the item from the queue and then re-adds it to increment the
     * live-ness of the item. It updates the LRU since this key was read.
     *
     * @param key key
     */
    private void removeThenAddKey( KEY key ) {
            queue.removeFirstOccurrence( key );
            queue.addFirst( key );

    }

    /**
     * Remove the key.
     * @param key
     */
    private void removeFirstOccurrence( KEY key ) {
            queue.removeFirstOccurrence(key);

    }



    public String toString() {
        return map.toString();
    }
}
