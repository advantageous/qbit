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


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentLinkedHashSet<T> implements Set<T> {

    List<T> list = new CopyOnWriteArrayList<>();
    Set<T> set = new ConcurrentHashSet<>();

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray(new Object[list.size()]);
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {

        return list.toArray(a);
    }

    @Override
    public synchronized boolean add(T t) {
        list.remove(t);
        list.add(t);
        return set.add(t);
    }


    public synchronized boolean addFirst(T t) {
        list.remove(t);
        list.add(0, t);
        return set.add(t);
    }

    @Override
    public synchronized boolean remove(Object o) {
        list.remove(o);
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return set.addAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        list.retainAll(c);
        return set.retainAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        list.removeAll(c);
        return set.removeAll(c);
    }

    @Override
    public synchronized void clear() {
        set.clear();
        list.clear();
    }
}
