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

package io.advantageous.boon.primitive;


import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Lists;
import io.advantageous.boon.core.reflection.Invoker;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.Universal;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;

@SuppressWarnings ( { "unchecked", "SuspiciousSystemArraycopy" } )
public class Arry {


    @Universal
    public static <V> V[] array( Class<V> clasz, int size ) {
        Object newArray = Array.newInstance( clasz, size );
        return ( V[] ) newArray;
    }


    public static <V> V[] grow( V[] array, int size ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(),
                array.length + size );
        System.arraycopy( array, 0, newArray, 0, array.length );
        return ( V[] ) newArray;
    }


    public static <V> V[] grow( V[] array ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(),
                array.length * 2 );
        System.arraycopy( array, 0, newArray, 0, array.length );
        return ( V[] ) newArray;
    }

    public static <V> V[] shrink( V[] array, int size ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(),
                array.length - size );
        System.arraycopy( array, 0, newArray, 0, array.length - size );
        return ( V[] ) newArray;
    }

    public static <V> V[] compact( V[] array ) {

        int nullCount = 0;
        for ( V anArray1 : array ) {

            if ( anArray1 == null ) {
                nullCount++;
            }
        }
        Object newArray = Array.newInstance( array.getClass().getComponentType(),
                array.length - nullCount );

        int j = 0;
        for ( V anArray : array ) {

            if ( anArray == null ) {
                continue;
            }

            Array.set( newArray, j, anArray );
            j++;
        }
        return ( V[] ) newArray;
    }

    @SafeVarargs
    public static <V> V[] array( final V... array ) {
        return array;

    }

    /* Universal methods */

    @Universal
    public static <V> int len( V[] array ) {
        return array.length;
    }

    @Universal
    public static <V> int lengthOf( V[] array ) {
        return array.length;
    }

    @Universal
    public static <V> V idx( final V[] array, int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }

    @Universal
    public static Object idx( final Object array, int index ) {
        final int i = calculateIndex( array, index );

        return Array.get(array, i);
    }


    @Universal
    public static Object fastIndex( final Object array, int index ) {
        final int i = calculateIndex( array, index );

        return Array.get(array, index);
    }


    @Universal
    public static <V> V atIndex( final V[] array, int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }

    @Universal
    public static <V> void idx( final V[] array, int index, V value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }

    @Universal
    public static <V> void atIndex( final V[] array, int index, V value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }


    @Universal
    public static <V> V[] sliceOf( V[] array, int startIndex, int endIndex ) {

        return slc(array, startIndex, endIndex);
    }

    @Universal
    public static <V> V[] slc( V[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;
        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        Object newArray = Array.newInstance( array.getClass().getComponentType(), newLength );
        System.arraycopy( array, start, newArray, 0, newLength );
        return ( V[] ) newArray;
    }


    public static <V> V[] fastSlice( V[] array, int start, int end ) {

        final int newLength = end - start;


        Object newArray = Array.newInstance( array.getClass().getComponentType(), newLength );
        System.arraycopy( array, start, newArray, 0, newLength );
        return ( V[] ) newArray;
    }

    public static Object[] fastObjectArraySlice(Object[] array, int start, int end) {

        final int newLength = end - start;

        Object [] newArray = new Object[newLength];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static <V> boolean in( V value, V[] array ) {
        for ( V currentValue : array ) {
            if ( currentValue.equals( value ) ) {
                return true;
            }
        }
        return false;
    }


    @Universal
    public static <V> V[] sliceOf( V[] array, int startIndex ) {
        return slc(array, startIndex);
    }

    @Universal
    public static <V> V[] slc( V[] array, int startIndex ) {


        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        Object newArray = Array.newInstance( array.getClass().getComponentType(), newLength );
        System.arraycopy( array, start, newArray, 0, newLength );
        return ( V[] ) newArray;
    }


    @Universal
    public static <V> V[] copy( V[] array ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(), array.length );
        System.arraycopy( array, 0, newArray, 0, array.length );
        return ( V[] ) newArray;
    }


    @Universal
    public static <V> V[] add( V[] array, V v ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(), array.length + 1 );
        System.arraycopy( array, 0, newArray, 0, array.length );
        Array.set( newArray, array.length, v );
        return ( V[] ) newArray;
    }


    @Universal
    public static <V> V[] add( V[] array, V[] array2 ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(), array.length + array2.length );
        System.arraycopy( array, 0, newArray, 0, array.length );
        System.arraycopy( array2, 0, newArray, array.length, array2.length );

        return ( V[] ) newArray;
    }

    @Universal
    public static <V> V[] insert( V[] array, int index, V v ) {
        Object newArray = Array.newInstance( array.getClass().getComponentType(), array.length + 1 );
        if ( index != 0 ) {
            System.arraycopy( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;
        int remainingArrayLengthAfterIndex = array.length - index;

        if ( lastIndex ) {
            System.arraycopy( array, index, newArray, index + 1, remainingArrayLengthAfterIndex );

        } else {
            System.arraycopy( array, index, newArray, index + 1, remainingArrayLengthAfterIndex );

        }

        Array.set( newArray, index, v );
        return ( V[] ) newArray;
    }


    @Universal
    public static <V> V[] endSliceOf( V[] array, int endIndex ) {
        return slcEnd(array, endIndex);
    }

    @Universal
    public static <V> V[] slcEnd( V[] array, int endIndex ) {

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "end index %d, length %d",
                            endIndex, array.length )
            );
        }

        Object newArray = Array.newInstance( array.getClass().getComponentType(), newLength );
        System.arraycopy( array, 0, newArray, 0, newLength );
        return ( V[] ) newArray;
    }



    private static int calculateIndex(Object array, int originalIndex) {
        final int length = Array.getLength(array);
        int index = originalIndex;

        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            index = length + index;
        }

        /* Bounds check
            if it is still less than 0, then they
            have an negative index that is greater than length
         */
        if ( index < 0 ) {
            index = 0;
        }
        if ( index >= length ) {
            index = length - 1;
        }
        return index;

    }

    /* End universal methods. */
    private static <T> int calculateIndex( T[] array, int originalIndex ) {
        final int length = array.length;
        int index = originalIndex;

        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            index = length + index;
        }

        /* Bounds check
            if it is still less than 0, then they
            have an negative index that is greater than length
         */
        if ( index < 0 ) {
            index = 0;
        }
        if ( index >= length ) {
            index = length - 1;
        }
        return index;
    }



    /* End universal methods. */
    private static <T> int calculateEndIndex( T[] array, int originalIndex ) {
        final int length = array.length;
        int index = originalIndex;

        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            index = length + index;
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


    public static List<Map<String, Object>> toListOfMaps( Object... array ) {
        return MapObjectConversion.toListOfMaps(Lists.list(array));
    }


    public static Object reduceBy( final Object[] array, Object object ) {

        Object sum = null;
        for ( Object v : array ) {
            sum = Invoker.invokeReducer(object, sum, v);
        }
        return sum;
    }

    public static int len(Object obj) {
        return Array.getLength( obj );
    }

    @SuppressWarnings ( "unchecked" )
    public static <V> V[] array( Collection<V> collection ) {
        if ( collection.size() > 0 ) {
            Object newInstance = Array.newInstance( collection.iterator().next().getClass(),
                    collection.size() );
            return collection.toArray( ( V[] ) newInstance );
        } else {
            Exceptions.die("array(listStream): The collection has to have at least one item in it");
            return null;
        }
    }


    public static Object[] objectArray( Collection collection ) {
            return collection.toArray(new Object[collection.size()]);
    }


    public static Object[] objectArray( Iterable iter ) {
        if (iter instanceof  Collection) {
            return objectArray((Collection)iter);
        } else {
            return objectArray(Lists.list(iter));
        }
    }

    public static <V> V[] array(Class<V> cls,  Collection<V> collection) {
        Object newInstance = Array.newInstance( cls, collection.size() );
        return collection.toArray( ( V[] ) newInstance );
    }

    public static <T> boolean  equals(T[] array1, T[] array2) {
        if (array1==array2) {
            return true;
        }
        if (array1==null || array2==null) {
            return false;
        }

        int length = array1.length;
        if (array2.length != length) {
            return false;
        }

        for (int index=0; index<length; index++) {
            Object value1 = array1[index];
            Object value2 = array2[index];
            if (value1==null || value2==null) {
                return false;
            }
            return value1.equals(value2);

        }

        return true;
    }

}
