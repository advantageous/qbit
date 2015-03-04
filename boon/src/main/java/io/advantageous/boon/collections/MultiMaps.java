package io.advantageous.boon.collections;

import java.util.ArrayList;

/**
 * Created by Richard on 9/28/14.
 */
public class MultiMaps {

    public static <K, V> MultiMap <K, V> multiMap() {
        return new MultiMapImpl(ArrayList.class);
    }

    public static MultiMap safeMultiMap() {
        return new MultiMapImpl(ConcurrentLinkedHashSet.class);
    }
}
