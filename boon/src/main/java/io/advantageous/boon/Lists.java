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


import io.advantageous.boon.collections.DoubleList;
import io.advantageous.boon.collections.FloatList;
import io.advantageous.boon.collections.IntList;
import io.advantageous.boon.collections.LongList;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Predicate;
import io.advantageous.boon.core.reflection.*;
import io.advantageous.boon.primitive.CharBuf;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;


public class Lists {


    public static <T> List<T> lazyAdd(List<T> list, T... items) {
        list = list == null ? new ArrayList<T>() : list;

        for (T item : items) {
            list.add(item);
        }
        return list;
    }


    public static <T> List<T> lazyAdd(ArrayList<T> list, T... items) {
        list = list == null ? new ArrayList<T>() : list;

        for (T item : items) {
            list.add(item);
        }
        return list;    }

    public static <T> List<T> safeLazyAdd(CopyOnWriteArrayList<T> list, T... items) {
        list = list == null ? new CopyOnWriteArrayList<T>() : list;
        for (T item : items) {
            list.add(item);
        }
        return list;
    }

    public static <T> List<T> lazyAdd(CopyOnWriteArrayList<T> list, T... items) {
        list = list == null ? new CopyOnWriteArrayList<T>() : list;
        for (T item : items) {
            list.add(item);
        }
        return list;
    }




    public static <T> List<T> lazyCreate(List<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }



    public static <T> List<T> lazyCreate(ArrayList<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }


    public static <T> List<T> lazyCreate(CopyOnWriteArrayList<T> list) {
        return list == null ? new CopyOnWriteArrayList<T>() : list;
    }


    public static <T> List<T> safeLazyCreate(CopyOnWriteArrayList<T> list) {
        return list == null ? new CopyOnWriteArrayList<T>() : list;
    }

    public static <T> T fromList( List<Object> list, Class<T> clazz ) {
        return MapObjectConversion.fromList(list, clazz);
    }

    public static <V> List<V> list( Class<V> clazz ) {
        return new ArrayList<>();
    }


    public static <V> List<V> copy( Collection<V> collection ) {
        return new ArrayList<>( collection );
    }


    public static <V> List<V> deepCopy( Collection<V> collection ) {
        List<V> list = new ArrayList<>(collection.size());

        for (V v : collection) {
            list.add( BeanUtils.copy(v));
        }
        return list;
    }

    public static <V> List<V> deepCopyToList( Collection<V> src,  List<V> dst) {

        for (V v : src) {
            dst.add( BeanUtils.copy( v ));
        }
        return  dst;
    }


    public static <V,T> List<T> deepCopy( Collection<V> src, Class<T> dest  ) {
        List<T> list = new ArrayList<>(src.size());

        for (V v : src) {
            list.add( BeanUtils.createFromSrc( v, dest ));
        }
        return list;
    }

    /**
     * Clones each list item into a new instance with copied fields.
     * It is like doing a clone operation.
     *
     * If the passed list is a LinkedList then the returned list will be a
     * LinkedList.
     *
     * If the passed list is a CopyOnWriteArrayList then the returned list will
     * be a CopyOnWriteArrayList list.
     *
     * All other lists become ArrayList.
     *
     * @param list list to clone
     * @param <V> generics
     * @return new list
     */
    @Universal
    public static <V> List<V> deepCopy( List<V> list ) {
        if ( list instanceof LinkedList ) {
            return deepCopyToList( list, new LinkedList<V>(  ) );
        } else if ( list instanceof CopyOnWriteArrayList ) {
            return deepCopyToList( list, new CopyOnWriteArrayList<V>(  ));
        } else {
            return deepCopy( (Collection)list);
        }
    }


    public static <V> List<List<V>> lists( Collection<V>... collections ) {
        List<List<V>> lists = new ArrayList<>(collections.length);
        for (Collection<V> collection : collections) {
            lists.add(new ArrayList<>(collection));
        }
        return lists;
    }



    public static <V> List<V> list( Iterable<V> iterable ) {
        List<V> list = new ArrayList<>();
        for ( V o : iterable ) {
            list.add( o );
        }
        return list;
    }



    public static <V> List<V> list( Collection<V> collection ) {
        return new ArrayList<>(collection);
    }

    public static <V> List<V> linkedList( Iterable<V> iterable ) {
        List<V> list = new LinkedList<>();
        for ( V o : iterable ) {
            list.add( o );
        }
        return list;
    }

    public static List<?> toListOrSingletonList( Object item ) {
        if ( item == null ) {
            return new ArrayList<>();
        } else if ( item.getClass().isArray() ) {
            final int length = Array.getLength( item );
            List<Object> list = new ArrayList<>();
            for ( int index = 0; index < length; index++ ) {
                list.add( Array.get( item, index ) );
            }
            return list;
        } else if ( item instanceof Collection ) {
            return list( ( Collection ) item );
        } else if ( item instanceof Iterator ) {
            return list( ( Iterator ) item );
        } else if ( item instanceof Enumeration ) {
            return list( ( Enumeration ) item );
        } else if ( item instanceof Iterable ) {
            return list( ( Iterable ) item );
        } else {
            List<Object> list = new ArrayList<>();
            list.add( item );
            return list;
        }
    }


    public static <PROP> List<PROP> toList( List<?> inputList, Class<PROP> cls, String propertyPath ) {
        List<PROP> outputList = new ArrayList<>();

        for (Object o : inputList) {
            outputList.add((PROP) BeanUtils.idx(o, propertyPath));
        }

        return outputList;
    }

    public static IntList toIntList( List<?> inputList, String propertyPath ) {

        return IntList.toIntList(inputList, propertyPath);
    }


    public static FloatList toFloatList( List<?> inputList, String propertyPath ) {

        return FloatList.toFloatList(inputList, propertyPath);
    }


    public static DoubleList toDoubleList( List<?> inputList, String propertyPath ) {

        return DoubleList.toDoubleList(inputList, propertyPath);
    }


    public static LongList toLongList( List<?> inputList, String propertyPath ) {

        return LongList.toLongList(inputList, propertyPath);
    }

    public static List<?> toList( List<?> inputList, String propertyPath ) {
        List<Object> outputList = new ArrayList<>();

        for (Object o : inputList) {
            outputList.add(BeanUtils.idx(o, propertyPath));
        }

        return outputList;
    }

    public static List<?> toList( Object item ) {
       if ( item!= null && item.getClass().isArray() ) {
            final int length = Array.getLength( item );
            List<Object> list = new ArrayList<>();
            for ( int index = 0; index < length; index++ ) {
                list.add( Array.get( item, index ) );
            }
            return list;
        } else if ( item instanceof Collection ) {
            return list( ( Collection ) item );
        } else if ( item instanceof Iterator ) {
            return list( ( Iterator ) item );
        } else if ( item instanceof Enumeration ) {
            return list( ( Enumeration ) item );
        } else if ( item instanceof Iterable ) {
            return list( ( Iterable ) item );
        } else {
            return MapObjectConversion.toList( item );
        }
    }



    public static <V, WRAP> List<WRAP> convert(Class<WRAP> wrapper, Iterable<V> collection ) {
        List<WRAP> list = new ArrayList<>(  );

        for (V v : collection) {

            list.add ( Conversions.coerce(wrapper, v) );
        }
        return list;
    }

    public static <V, WRAP> List<WRAP> convert(Class<WRAP> wrapper, Collection<V> collection ) {
        List<WRAP> list = new ArrayList<>( collection.size() );

        for (V v : collection) {

            list.add ( Conversions.coerce(wrapper, v) );
        }
        return list;
    }


    public static <V, WRAP> List<WRAP> convert(Class<WRAP> wrapper, V[] collection ) {
        List<WRAP> list = new ArrayList<>( collection.length );

        for (V v : collection) {

            list.add ( Conversions.coerce(wrapper, v) );
        }
        return list;
    }


    public static <V, WRAP> List<WRAP> wrap(Class<WRAP> wrapper, Iterable<V> collection ) {
        List<WRAP> list = new ArrayList<>(  );

        for (V v : collection) {
            WRAP wrap = Reflection.newInstance ( wrapper, v );
            list.add ( wrap );
        }
        return list;
    }

    public static <V, WRAP> List<WRAP> wrap(Class<WRAP> wrapper, Collection<V> collection ) {

        if (collection.size()==0) {
            return Collections.EMPTY_LIST;
        }

        List<WRAP> list = new ArrayList<>( collection.size () );



        ClassMeta<WRAP> cls = ClassMeta.classMeta(wrapper);
        ConstructorAccess<WRAP> declaredConstructor = cls.declaredConstructor(collection.iterator().next().getClass());

        for (V v : collection) {
            WRAP wrap = declaredConstructor.create (  v );
            list.add ( wrap );
        }
        return list;
    }


    public static <V, WRAP> List<WRAP> wrap(Class<WRAP> wrapper, V[] collection ) {
        List<WRAP> list = new ArrayList<>( collection.length );

        for (V v : collection) {
            WRAP wrap = Reflection.newInstance ( wrapper, v );
            list.add ( wrap );
        }
        return list;
    }

    public static <V> List<V> list( Enumeration<V> enumeration ) {
        List<V> list = new ArrayList<>();
        while ( enumeration.hasMoreElements() ) {
            list.add( enumeration.nextElement() );
        }
        return list;
    }


    public static <V> Enumeration<V> enumeration( final List<V> list ) {
        final Iterator<V> iter = list.iterator();
        return new Enumeration<V>() {
            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public V nextElement() {
                return iter.next();
            }
        };

    }


    public static <V> List<V> list( Iterator<V> iterator ) {
        List<V> list = new ArrayList<>();
        while ( iterator.hasNext() ) {
            list.add( iterator.next() );
        }
        return list;
    }



    @SafeVarargs
    public static <V> List<V> list( final V... array ) {
        if ( array == null ) {
            return new ArrayList<>();
        }
        List<V> list = new ArrayList<>( array.length );
        Collections.addAll( list, array );
        return list;
    }

    public static <V> List<V> safeList(Class<V> cls) {
        return new CopyOnWriteArrayList<>(  );
    }

    @SafeVarargs
    public static <V> List<V> safeList( final V... array ) {
        return new CopyOnWriteArrayList<>( array );
    }

    @SafeVarargs
    public static <V> List<V> linkedList( final V... array ) {
        if ( array == null ) {
            return new LinkedList<>();
        }
        List<V> list = new LinkedList<>();
        Collections.addAll( list, array );
        return list;
    }


    public static <V> List<V> safeList( Collection<V> collection ) {
        return new CopyOnWriteArrayList<>( collection );
    }

    public static <V> List<V> linkedList( Collection<V> collection ) {
        return new LinkedList<>( collection );
    }

    /**
     * Universal methods
     */
    @Universal
    public static int len( List<?> list ) {
        return list.size();
    }


    @Universal
    public static int lengthOf( List<?> list ) {
        return len (list);
    }

    public static boolean isEmpty( List<?> list ) {
        return list == null || list.size() == 0;
    }

    @Universal
    public static <V> boolean in( V value, List<?> list ) {
        return list.contains( value );
    }

    @Universal
    public static <V> void add( List<V> list, V value ) {
        list.add( value );
    }


    @Universal
    public static <V> void add( List<V> list, V... values ) {
        for (V v : values) {
            list.add( v );
        }
    }

    @Universal
    public static <T> T atIndex( List<T> list, final int index ) {

        return idx(list, index);

    }

    @Universal
    public static <T> T idx( List<T> list, final int index ) {
        int i = calculateIndex( list, index );
        if ( i > list.size() - 1 ) {
            i = list.size() - 1;
        }
        return list.get( i );

    }

    public static <T> List idxList( List<T> list, final int index ) {
        return (List) idx(list, index);
    }


    public static <T> Map idxMap( List<T> list, final int index ) {
        return (Map) idx(list, index);
    }


    @Universal
    public static <V> void atIndex( List<V> list, int index, V v ) {
        idx (list, index, v);
    }

    @Universal
    public static <V> void idx( List<V> list, int index, V v ) {
        int i = calculateIndex( list, index );
        list.set( i, v );
    }


    @Universal
    public static <V> List<V> sliceOf( List<V> list, int startIndex, int endIndex ) {
        return slc(list, startIndex, endIndex);
    }

    @Universal
    public static <V> List<V> slc( List<V> list, int startIndex, int endIndex ) {
        int start = calculateIndex( list, startIndex );
        int end = calculateIndex( list, endIndex );
        return list.subList( start, end );
    }


    @Universal
    public static <V> List<V> sliceOf( List<V> list, int startIndex ) {
        return slc(list, startIndex);
    }

    @Universal
    public static <V> List<V> slc( List<V> list, int startIndex ) {
        return slc( list, startIndex, list.size() );
    }

    @Universal
    public static <V> List<V> endSliceOf( List<V> list, int endIndex ) {
        return slcEnd( list, endIndex );
    }


    @Universal
    public static <V> List<V> slcEnd( List<V> list, int endIndex ) {
        return slc( list, 0, endIndex );
    }


    @Universal
    public static <V> List<V> copy( List<V> list ) {
        if ( list instanceof LinkedList ) {
            return new LinkedList<>( list );
        } else if ( list instanceof CopyOnWriteArrayList ) {
            return new CopyOnWriteArrayList<>( list );
        } else {
            return new ArrayList<>( list );
        }
    }


    @Universal
    public static <V> List<V> copy( CopyOnWriteArrayList<V> list ) {
        return new CopyOnWriteArrayList<>( list );
    }

    @Universal
    public static <V> List<V> copy( ArrayList<V> list ) {
        return new ArrayList<>( list );
    }

    @Universal
    public static <V> List<V> copy( LinkedList<V> list ) {
        return new LinkedList<>( list );
    }


    @Universal
    public static <V> void insert( List<V> list, int index, V v ) {
        int i = calculateIndex( list, index );
        list.add( i, v );
    }


    /* End universal methods. */
    private static <T> int calculateIndex( List<T> list, int originalIndex ) {
        final int length = list.size();

        int index = originalIndex;

        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            index = ( length + index );
        }


        /* Bounds check
            if it is still less than 0, then they
            have an negative index that is greater than length
         */
        if ( index < 0 ) {
            index = 0;
        }
        if ( index > length ) {
            index = length;
        }
        return index;
    }


    public static <T> List<T> listFromProperty( Class<T> propertyType, String propertyPath, Collection<?> list ) {
        List<T> newList = new ArrayList<>( list.size() );

        for ( Object item : list ) {
            T newItem = ( T ) BeanUtils.idx( item, propertyPath );
            newList.add( newItem );
        }

        return newList;

    }


    public static <T> List<T> listFromProperty( Class<T> propertyType, String propertyPath, Iterable<?> list ) {
        List<T> newList = new ArrayList<>(  );

        for ( Object item : list ) {
            T newItem = ( T ) BeanUtils.idx( item, propertyPath );
            newList.add( newItem );
        }

        return newList;

    }
    public static List<Map<String, Object>> toListOfMaps( List<?> list ) {
        return MapObjectConversion.toListOfMaps( list );
    }

    public static void setListProperty(List<?> list, String propertyName, Object value) {
        for (Object object : list) {
            BeanUtils.idx(object, propertyName, value);
        }
    }


    public static List<?> mapBy( Object[] objects, Object instance, String methodName) {

        List list = new ArrayList(objects.length);
        for (Object o : objects) {
            list.add( Invoker.invoke(instance, methodName, o));
        }
        return list;
    }


    public static List<?> mapBy(Object[] objects, Class<?> cls, String methodName) {

        List list = new ArrayList(objects.length);
        for (Object o : objects) {
            list.add( Invoker.invoke(cls,methodName, o ));
        }
        return list;
    }



    public static List<?> mapBy(Iterable<?> objects, Class<?> cls, String methodName) {

        List list = new ArrayList();
        for (Object o : objects) {
            list.add( Invoker.invoke(cls, methodName, o ));
        }
        return list;
    }


    public static List<?> mapBy(Iterable<?> objects, Object instance, String methodName) {

        List list = new ArrayList();
        for (Object o : objects) {
            list.add( Invoker.invoke(instance, methodName, o ));
        }
        return list;
    }


    public static List<?> mapBy(Collection<?> objects, Class<?> cls, String methodName) {

        List list = new ArrayList(objects.size());

        MethodAccess methodAccess = Invoker.invokeMethodAccess(cls, methodName);

        for (Object o : objects) {
            list.add( methodAccess.invokeStatic(o));
        }
        return list;
    }

    public static List<?> mapBy(Collection<?> objects, Object function) {


            MethodAccess methodAccess = Invoker.invokeFunctionMethodAccess(function);

            List list = new ArrayList();
            for (Object o : objects) {
                list.add( methodAccess.invoke(function, o));
            }

            return list;
    }


    public static <T> List<T> mapBy(Class<T> cls, Collection<?> objects, Object function) {
        return (List<T>) mapBy(objects, function);
    }

    public static List<?> mapBy(Iterable<?> objects, Object function) {


        MethodAccess methodAccess = Invoker.invokeFunctionMethodAccess(function);

        List list = new ArrayList();
        for (Object o : objects) {
            list.add( methodAccess.invoke(function, o));
        }
        return list;
    }


    public static List<?> mapBy(Object[] objects, Object function) {

        MethodAccess methodAccess = Invoker.invokeFunctionMethodAccess(function);

        List list = new ArrayList(objects.length);
        for (Object o : objects) {
            list.add( methodAccess.invoke(function, o));
        }
        return list;
    }



     public static List<?> mapBy(Collection<?> objects, Object object, String methodName) {


         MethodAccess methodAccess = Invoker.invokeMethodAccess(object.getClass(), methodName);

        List list = new ArrayList(objects.size());
        for (Object o : objects) {
            list.add( methodAccess.invoke(object, o));
        }
        return list;
    }


    public static <V, N> List<N> mapBy(  final V[] array, Function<V, N> function ) {
        List<N> list = new ArrayList<>( array.length );

        for ( V v : array ) {
            list.add( function.apply( v ) );
        }
        return list;
    }

    public static <V, N> List<N> mapBy( final Collection<V> array, Function<V, N> function ) {
        List<N> list = new ArrayList<>( array.size() );

        for ( V v : array ) {
            list.add( function.apply( v ) );
        }
        return list;
    }


    public static Object reduceBy( final Iterable<?> array, Object object ) {

        Object sum = null;
        for ( Object v : array ) {
            sum = Invoker.invokeReducer(object, sum, v);
        }
        return sum;
    }



    public static <T> List<T> filterBy( final Iterable<T> array, Predicate<T> predicate ) {
        List<T> list = new ArrayList<>(  );

        for ( T v : array ) {
            if ( predicate.test(v)) {
                list.add(  v  );
            }
        }
        return list;
    }


    public static <T> List<T> filterBy(  final Collection<T> array, Predicate<T> predicate ) {
        List<T> list = new ArrayList<>( array.size()  );

        for ( T v : array ) {
            if ( predicate.test(v)) {
                list.add(  v  );
            }
        }
        return list;
    }


    public static <T> List<T> filterBy( Predicate<T> predicate, final T[] array ) {
        List<T> list = new ArrayList<>( array.length  );

        for ( T v : array ) {
            if ( predicate.test(v)) {
                list.add(  v  );
            }
        }
        return list;
    }



    public static <T> List<T> filterBy(  final Iterable<T> array, Object object ) {
        List<T> list = new ArrayList<>(  );

        for ( T v : array ) {
            if ( Invoker.invokeBooleanReturn(object, v) ) {
                list.add(  v  );
            }
        }
        return list;
    }


    public static <T> List<T> filterBy( final Collection<T> array, Object object ) {
        List<T> list = new ArrayList<>( array.size()  );

        for ( T v : array ) {
            if ( Invoker.invokeBooleanReturn(object, v) ) {
                list.add(  v  );
            }
        }
        return list;
    }


    public static <T> List<T> filterBy(  final T[] array, Object object ) {
        List<T> list = new ArrayList<>( array.length  );

        for ( T v : array ) {
            if ( Invoker.invokeBooleanReturn(object, v) ) {
                list.add(  v  );
            }
        }
        return list;
    }




    public static <T> List<T> filterBy(  final Iterable<T> array, Object object, String methodName ) {
        List<T> list = new ArrayList<>(  );

        for ( T v : array ) {
            if ( (boolean) Invoker.invokeEither(object, methodName, v) ) {
                list.add(  v  );
            }
        }
        return list;
    }


    public static <T> List<T> filterBy( final Collection<T> array, Object object, String methodName ) {
        List<T> list = new ArrayList<>( array.size()  );

        for ( T v : array ) {
            if ( (boolean) Invoker.invokeEither(object, methodName, v) ) {
                list.add(  v  );
            }
        }
        return list;
    }


    public static <T> List<T> filterBy(  final T[] array, Object object, String methodName ) {
        List<T> list = new ArrayList<>( array.length  );

        for ( T v : array ) {
            if ( (boolean) Invoker.invokeEither(object, methodName, v) ) {
                list.add(  v  );
            }
        }
        return list;
    }



    public static String toPrettyJson(List list) {
        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintCollection(list, false, 0).toString();
    }
}
