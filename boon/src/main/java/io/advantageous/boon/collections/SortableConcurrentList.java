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


import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Just a concurrent list.
 *
 * @param <T> T
 */
public class SortableConcurrentList<T extends Comparable> implements List<T> {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final List<T> list;

    public SortableConcurrentList(List<T> list) {
        this.list = list;
    }

    public SortableConcurrentList() {
        this.list = new ArrayList<>();
    }

    public boolean remove(Object o) {
        readWriteLock.writeLock().lock();
        boolean ret;
        try {
            ret = list.remove(o);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        readWriteLock.readLock().lock();
        try {
            return list.containsAll(c);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        readWriteLock.writeLock().lock();
        try {
            return list.addAll(c);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        readWriteLock.writeLock().lock();
        try {
            return list.addAll(index, c);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        readWriteLock.writeLock().lock();
        try {
            return list.removeAll(c);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        readWriteLock.writeLock().lock();
        try {
            return list.retainAll(c);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public boolean add(T t) {
        readWriteLock.writeLock().lock();
        boolean ret;
        try {
            ret = list.add(t);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return ret;
    }

    public void clear() {
        readWriteLock.writeLock().lock();
        try {
            list.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    public int size() {
        readWriteLock.readLock().lock();
        try {
            return list.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readWriteLock.readLock().lock();
        try {
            return list.isEmpty();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public boolean contains(Object o) {
        readWriteLock.readLock().lock();
        try {
            return list.contains(o);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        readWriteLock.readLock().lock();
        try {
            return new ArrayList<>(list).iterator();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Object[] toArray() {

        readWriteLock.readLock().lock();
        try {
            return list.toArray();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public <T> T[] toArray(final T[] a) {

        readWriteLock.readLock().lock();
        try {
            return list.toArray(a);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public T get(int index) {
        readWriteLock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public T set(int index, T element) {
        readWriteLock.writeLock().lock();
        try {
            return list.set(index, element);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void add(int index, T element) {
        readWriteLock.writeLock().lock();
        try {
            list.add(index, element);
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public T remove(int index) {
        readWriteLock.writeLock().lock();
        try {
            return list.remove(index);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public int indexOf(Object o) {
        readWriteLock.readLock().lock();
        try {
            return list.indexOf(o);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        readWriteLock.readLock().lock();
        try {
            return list.lastIndexOf(o);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        readWriteLock.readLock().lock();
        try {
            return new ArrayList(list).listIterator();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        readWriteLock.readLock().lock();
        try {
            return new ArrayList(list).listIterator(index);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        readWriteLock.readLock().lock();
        try {
            return list.subList(fromIndex, toIndex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        readWriteLock.readLock().lock();
        try {
            return list.toString();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }


    public void sort() {
        readWriteLock.writeLock().lock();
        try {

            Collections.sort(list);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    public List<T> sortAndReturnPurgeList(float removePercent) {
        readWriteLock.writeLock().lock();
        try {
            int size = list.size();
            int removeSize = (int) (size - (size * removePercent));
            int start = size - removeSize;

            Collections.sort(list);

            List<T> removeList = new ArrayList<>(list.subList(0, start));
            list.removeAll(removeList);
            return removeList;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}