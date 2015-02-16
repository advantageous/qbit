/*******************************************************************************

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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__                                                                                               
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.vertx;

import io.advantageous.qbit.util.MultiMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.boon.Exceptions.die;

/**
 * @author   rhightower on 10/13/14.
 */
public class MultiMapWrapper implements MultiMap<String, String> {


    final org.vertx.java.core.MultiMap vertxMap;

    public MultiMapWrapper(org.vertx.java.core.MultiMap vertxMap) {
        this.vertxMap = vertxMap;
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {


        final Iterator<Entry<String, String>> iterator = vertxMap.iterator();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<String, Collection<String>> next() {
                final Entry<String, String> next = iterator.next();

                return new Entry<String, Collection<String>>() {
                    @Override
                    public String getKey() {
                        return next.getKey();
                    }

                    @Override
                    public Collection<String> getValue() {
                        return (Collection<String>) MultiMapWrapper.this.getAll(next.getKey());
                    }

                    @Override
                    public Collection<String> setValue(Collection<String> value) {
                        return null;
                    }
                };
            }
        };

    }

    @Override
    public void add(String key, String s) {

        vertxMap.add(key, s);
    }

    @Override
    public String getFirst(String key) {
        return vertxMap.get(key);
    }

    @Override
    public Iterable<String> getAll(String key) {
        return vertxMap.getAll(key);
    }

    @Override
    public boolean removeValueFrom(String key, String s) {
        return die(Boolean.class, "NOT SUPPORTED");
    }

    @Override
    public boolean removeMulti(String key) {
        vertxMap.remove(key);
        return true;
    }

    @Override
    public Iterable<String> keySetMulti() {
        return vertxMap.names();
    }

    @Override
    public Iterable<String> valueMulti() {
        /* We could support this. */
        return die(Iterable.class, "NOT SUPPORTED");
    }

    @Override
    public void putAll(MultiMap<String, String> params) {

        Set<String> keys = params.keySet();

        for (String key : keys) {
            Iterable<String> values = params.getAll(key);
            for (String value : values) {
                this.add(key, value);
            }
        }
    }

    @Override
    public Map<? extends String, ? extends Collection<String>> baseMap() {

        return die(Map.class, "NOT SUPPORTED");
    }

    @Override
    public String getSingleObject(String name) {
        return vertxMap.get(name);
    }

    @Override
    public int size() {
        return vertxMap.size();
    }

    @Override
    public boolean isEmpty() {
        return vertxMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return vertxMap.contains((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        return die(Boolean.class, "NOT SUPPORTED");

    }

    @Override
    public String get(Object key) {
        return vertxMap.get((String) key);
    }

    @Override
    public String put(String key, String value) {
        vertxMap.add(key, value);
        return null;
    }

    @Override
    public String remove(Object key) {
        vertxMap.remove(key.toString());
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

        Set<? extends Entry<? extends String, ? extends String>> entries = m.entrySet();

        for (Entry<? extends String, ? extends String> entry : entries) {
            vertxMap.add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {

        vertxMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return vertxMap.names();
    }

    @Override
    public Collection<String> values() {
        return die(Collection.class, "Not supported");
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return die(Set.class, "Not supported");
    }
}
