package io.advantageous.boon.collections;

import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Richard on 9/1/14.
 */
public class FakeMapEntrySet extends AbstractSet<Map.Entry<String, Object>> {


    Map.Entry<String, Object>[] array;

    public FakeMapEntrySet(int size, String[] keys, Object[] values) {

        array = new Map.Entry[size];

        for (int index = 0; index < size; index++) {
            array[index] = Maps.entry(keys[index], values[index]);
        }
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return Lists.list(this.array).iterator();
    }

    @Override
    public int size() {
        return array.length;
    }
}
