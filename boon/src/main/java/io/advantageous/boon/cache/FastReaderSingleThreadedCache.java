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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FastReaderSingleThreadedCache<KEY, VALUE> implements Cache<KEY, VALUE> {

    private final Map<KEY, VALUE> map = new HashMap<>();
    private final Deque<KEY> queue = new LinkedList<>();
    private final int limit;


    public FastReaderSingleThreadedCache( int limit ) {
        this.limit = limit;
    }

    public void put( KEY key, VALUE value ) {
        VALUE oldValue = map.put( key, value );

            /*If there was already an object under this key,
             then remove it before adding to queue
             Frequently used keys will be at the top so the search could be fast.
             */
        if ( oldValue != null ) {
            queue.removeFirstOccurrence( key );
        }
        queue.addFirst( key );

        if ( map.size() > limit ) {
            final KEY removedKey = queue.removeLast();
            map.remove( removedKey );
        }

    }


    public VALUE get( KEY key ) {

            /* Frequently used keys will be at the top so the search could be fast.*/
        queue.removeFirstOccurrence( key );
        queue.addFirst( key );
        return map.get( key );
    }


    public VALUE getSilent( KEY key ) {

        return map.get( key );
    }

    public void remove( KEY key ) {

            /* Frequently used keys will be at the top so the search could be fast.*/
        queue.removeFirstOccurrence( key );
        map.remove( key );
    }

    public int size() {
        return map.size();
    }

    public String toString() {
        return map.toString();
    }
}
