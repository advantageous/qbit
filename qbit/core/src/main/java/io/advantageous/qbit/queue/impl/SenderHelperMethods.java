package io.advantageous.qbit.queue.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SenderHelperMethods {




    public static Object[] fastObjectArraySlice(final Object[] array,
                                         @SuppressWarnings("SameParameterValue") final int start,
                                         final int end) {
        final int newLength = end - start;
        final Object[] newArray = new Object[newLength];
        System.arraycopy(array, start, newArray, 0, newLength);
        return newArray;
    }

    public static Object[] objectArrayFromIterable(final Iterable iter) {
        if (iter instanceof Collection) {
            final Collection collection = (Collection) iter;
            return collection.toArray(new Object[collection.size()]);
        } else {
            return objectArrayFromIterable(list(iter));
        }
    }


    public static Object[] objectArray(final Collection collection) {
            return collection.toArray(new Object[collection.size()]);
    }

    /** This seems horrible. */
    public static <V> List<V> list(final Iterable<V> iterable) {
        final List<V> list = new ArrayList<>();
        for (V o : iterable) {
            list.add(o);
        }
        return list;
    }


}
