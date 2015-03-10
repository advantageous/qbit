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
import io.advantageous.boon.StringScanner;
import io.advantageous.boon.Universal;
import io.advantageous.boon.collections.IntList;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.Invoker;
import io.advantageous.boon.core.reflection.fields.FieldAccess;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;


/**
 * Integer operations.
 */
public class Int {


    /**
     * Grown an array
     * @param array array of items to grow
     * @param size size you would like to grow the array to
     * @return the new array
     */
    public static int[] grow( int[] array, final int size ) {

        int[] newArray = new int[ array.length + size ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    /**
     * Double the size of an array.
     * @param array the array you are going to double
     * @return the new array with the old values copied in
     */
    public static int[] grow( int[] array ) {
        Exceptions.requireNonNull( array );

        int[] newArray = new int[ array.length * 2 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    /**
     * Shrink an array
     * @param array the array you want to shrink
     * @param size what size do you want the new array
     * @return the new array
     */
    public static int[] shrink( int[] array, int size ) {

        int[] newArray = new int[ array.length - size ];

        System.arraycopy( array, 0, newArray, 0, array.length - size );
        return newArray;
    }


    /**
     * Takes out all of the 0s
     * @param array array to compact
     * @return return value
     */
    public static int[] compact( int[] array ) {
        Exceptions.requireNonNull( array );

        int nullCount = 0;
        for ( int ch : array ) {

            if ( ch == '\0' ) {
                nullCount++;
            }
        }
        int[] newArray = new int[ array.length - nullCount ];

        int j = 0;
        for ( int ch : array ) {

            if ( ch == '\0' ) {
                continue;
            }

            newArray[ j ] = ch;
            j++;
        }
        return newArray;
    }


    /**
     * Creates an array of bytes
     *
     * @param size size of the array you want to make
     * @return array
     */
    public static int[] arrayOfInt( final int size ) {
        return new int[ size ];
    }

    /**
     * @param array array
     * @return array
     */
    @Universal
    public static int[] array( final int... array ) {
        Exceptions.requireNonNull( array );
        return array;
    }


    /**
     * Returns the lengthOf an array.
     * @param array the array you are getting the length from.
     * @return array length
     */
    @Universal
    public static int lengthOf( int[] array ) {
        return len(array);
    }


    /**
     * Returns the lengthOf an array.
     * @param array the array you are getting the length from.
     * @return array length
     */
    @Universal
    public static int len( int[] array ) {
        return array.length;
    }



    /**
     * Returns the value at the given index.
     * @param array the array you are getting the from.
     * @return the value
     */
    @Universal
    public static int idx( final int[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }



    /**
     * Returns the value at the given index.
     * @param array the array you are getting the from.
     * @return the value
     */
    @Universal
    public static int atIndex( final int[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }


    /**
     * Sets the value at the given index.
     * @param array the array you are getting the from.
     */
    @Universal
    public static void idx( final int[] array, int index, int value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }


    /**
     * Sets the value at the given index.
     * @param array the array you are setting the value to.
     * @param value the new value.
     */
    @Universal
    public static void atIndex( final int[] array, int index, int value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }

    /**
     * Grabs a slice of this array
     * @param array the array you are getting slice from
     * @param startIndex the start index of the slice
     * @param endIndex the end index of the slice
     * @return the new slice
     */
    @Universal
    public static int[] slc( int[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        int[] newArray = new int[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    /**
     * Grabs a slice of this array
     * @param array the array you are getting slice from
     * @param startIndex the start index of the slice
     * @param endIndex the end index of the slice
     * @return the new slice
     */
    @Universal
    public static int[] sliceOf( int[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        int[] newArray = new int[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    /**
     * Grabs a slice of this array
     * @param array the array you are getting slice from
     * @param startIndex the start index of the slice
     * @return the new slice
     */
    @Universal
    public static int[] slc( int[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        int[] newArray = new int[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    /**
     * Grabs a slice of this array
     * @param array the array you are getting slice from
     * @param startIndex the start index of the slice
     * @return the new slice
     */
    @Universal
    public static int[] sliceOf( int[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        int[] newArray = new int[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    /**
     * Grabs a slice of this array
     * @param array the array you are getting slice from
     * @param endIndex the end index of the slice
     * @return the new slice
     */
    @Universal
    public static int[] slcEnd( int[] array, int endIndex ) {

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        int[] newArray = new int[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }


    /**
     * Grabs a slice of this array
     * @param array the array you are getting slice from
     * @param endIndex the end index of the slice
     * @return the new slice
     */
    @Universal
    public static int[] endSliceOf( int[] array, int endIndex ) {

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        int[] newArray = new int[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }


    /**
     * Checks to see if a value is in the array.
     * @param array the array you are getting slice from
     * @return true if the value is in the array
     */
    @Universal
    public static boolean in( int value, int[] array ) {
        for ( int currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Copies an array.
     * @param array the array you want to copy
     * @return the copied array
     */
    @Universal
    public static int[] copy( int[] array ) {
        int[] newArray = new int[ array.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    /**
     * Adds an int to the array. Grows the array by one and adds the int to the end.
     * @param array the array
     * @param v the value you are adding to the end.
     * @return the new array
     */
    @Universal
    public static int[] add( int[] array, int v ) {
        int[] newArray = new int[ array.length + 1 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        newArray[ array.length ] = v;
        return newArray;
    }

    /**
     * adds two array together and returns the results.
     * @param array first array
     * @param array2 second array
     * @return result
     */
    @Universal
    public static int[] add( int[] array, int[] array2 ) {
        int[] newArray = new int[ array.length + array2.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        System.arraycopy( array2, 0, newArray, array.length, array2.length );
        return newArray;
    }


    /**
     * Inserts a value in the midst of another array.
     * @param array array
     * @param idx index
     * @param v value
     * @return new array
     */
    @Universal
    public static int[] insert( final int[] array, final int idx, final int v ) {

        if ( idx >= array.length ) {
            return add( array, v );
        }

        final int index = calculateIndex( array, idx );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        int[] newArray = new int[ array.length + 1 ];

        if ( index != 0 ) {
            /* Copy up to the length in the array before the index. */
            /*                 src     sbegin  dst       dbegin   length of copy */
            System.arraycopy( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;
        int remainingIndex = array.length - index;

        if ( lastIndex ) {
            /* Copy the area after the insert. Make sure we don't write over the end. */
            /*                 src  sbegin   dst       dbegin     length of copy */
            System.arraycopy( array, index, newArray, index + 1, remainingIndex );

        } else {
            /* Copy the area after the insert.  */
            /*                 src  sbegin   dst       dbegin     length of copy */
            System.arraycopy( array, index, newArray, index + 1, remainingIndex );

        }

        newArray[ index ] = v;
        return newArray;
    }



    /**
     * Inserts an array in the midst of another array.
     * @param array array
     * @return new array
     */
    @Universal
    public static int[] insert( final int[] array, final int fromIndex, final int[] values ) {

        if ( fromIndex >= array.length ) {
            return add( array, values );
        }

        final int index = calculateIndex( array, fromIndex );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        int[] newArray = new int[ array.length + values.length ];

        if ( index != 0 ) {
            /* Copy up to the length in the array before the index. */
            /*                 src     sbegin  dst       dbegin   length of copy */
            System.arraycopy( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;

        int toIndex = index + values.length;
        int remainingIndex = newArray.length - toIndex;

        if ( lastIndex ) {
            /* Copy the area after the insert. Make sure we don't write over the end. */
            /*                 src  sbegin   dst       dbegin     length of copy */
            System.arraycopy( array, index, newArray, index + values.length, remainingIndex );

        } else {
            /* Copy the area after the insert.  */
            /*                 src  sbegin   dst       dbegin     length of copy */
            System.arraycopy( array, index, newArray, index + values.length, remainingIndex );

        }

        for ( int i = index, j = 0; i < toIndex; i++, j++ ) {
            newArray[ i ] = values[ j ];
        }
        return newArray;
    }


    /**
     * Calculates the index for slice notation so -1 is one minus length and so on.
     * @param array array in question
     * @param originalIndex the index give which might be negative or higher than length.
     * @return index
     */
    private static int calculateIndex( int[] array, int originalIndex ) {
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

    /**
     * Calculates the index for slice notation so -1 is one minus length and so on.
     * @param array array in question
     * @param originalIndex the index give which might be negative or higher than length.
     * @return end index
     */
    private static int calculateEndIndex( int[] array, int originalIndex ) {
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

    /** Public interface for a very fast reduce by. */
    public static interface ReduceBy {
        long reduce(long sum, int value);
    }

    /**
     * A very fast reduce by.
     * If performance is your thing, this seems to be as fast a plain for loop when benchmarking with JMH.
     *
     * @param array array of items to reduce by
     * @param reduceBy reduceBy interface
     * @return the final value
     */
    public static long reduceBy( final int[] array, ReduceBy reduceBy ) {


        long sum = 0;
        for ( int v : array ) {
            sum = reduceBy.reduce(sum, v);
        }
        return sum;
    }

    /**
     *
     * @param array array of items to reduce by
     * @param start where to start in the array
     * @param length where to end in the array
     * @param reduceBy the function to do the reduce by
     * @return the reduction
     */
    public static long reduceBy( final int[] array, final int start, final int length, ReduceBy reduceBy ) {


        long sum = 0;

        for (int index = start; index < length; index++) {
            int v = array[index];
            sum = reduceBy.reduce(sum, v);
        }
        return sum;
    }


    /**
     *
     * @param array array of items to reduce by
     * @param length where to end in the array
     * @param reduceBy the function to do the reduce by
     * @return the reduction
     */
    public static long reduceBy( final int[] array, final int length, ReduceBy reduceBy ) {


        long sum = 0;

        for (int index = 0; index < length; index++) {
            int v = array[index];
            sum = reduceBy.reduce(sum, v);
        }
        return sum;
    }




    /**
     * Reduce by functional support for int arrays.
     * @param array array of items to reduce by
     * @param object object that contains the reduce by function
     * @param <T> the type of object
     * @return the final reduction
     */
    public  static <T> long reduceBy( final int[] array, T object ) {
        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object );
        }


        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object);
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for ( int v : array ) {
                    sum = (long) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return handle(Long.class, throwable, "Unable to perform reduceBy");
            }
        } catch (Exception ex) {
            return reduceByR(array, object);
        }

    }





    /**
     * Reduce by functional support for int arrays.
     * @param array array of items to reduce by
     * @param object object that contains the reduce by function
     * @param <T> the type of object
     * @return the final reduction
     */
    public static <T> long reduceBy( final int[] array, T object, String methodName ) {

        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object, methodName);
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object, methodName);
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for ( int v : array ) {
                        sum = (long) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return handle(Long.class, throwable, "Unable to perform reduceBy");
            }
        } catch (Exception ex) {
            return reduceByR(array, object, methodName);
        }


    }


    /**
     * Fallback to reflection if the call-site will not work or did not work
     * @param array array of items to reduce by
     * @param object function object
     * @param <T> type of function object.
     * @return result
     */
    private  static <T> long reduceByR( final int[] array, T object ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object);


            long sum = 0;
            for ( int v : array ) {
                sum = (long) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return handle(Long.class, throwable, "Unable to perform reduceBy");
        }

    }


    /**
     * Reflection based reduce by.
     * @param array array of items to reduce by
     * @param object function
     * @param methodName name of method
     * @param <T> type of function
     * @return reduction
     */
    private  static <T> long reduceByR( final int[] array, T object, String methodName ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object, methodName);


            long sum = 0;
            for ( int v : array ) {
                sum = (long) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return handle(Long.class, throwable, "Unable to perform reduceBy");
        }

    }



    /**
     * Reflection based reduce by.
     * @param array array of items to reduce by
     * @param object function
     * @param methodName name of method
     * @param <T> type of function
     * @return reduction
     */
    private  static <T> long reduceByR( final int[] array, int length, T object, String methodName ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object, methodName);


            long sum = 0;
            for (int index=0; index< length; index++) {
                int v = array[index];
                sum = (long) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return handle(Long.class, throwable, "Unable to perform reduceBy");
        }

    }


    /**
     * Reflection based reduce by.
     * @param array array of items to reduce by
     * @param object function
     * @param <T> type of function
     * @return reduction
     */
    private  static <T> long reduceByR( final int[] array, int length, T object ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object);


            long sum = 0;
            for (int index=0; index< length; index++) {
                int v = array[index];
                sum = (long) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return handle(Long.class, throwable, "Unable to perform reduceBy");
        }

    }

    /**
     * Reduce By
     * @param array array of items to reduce by
     * @param length where to end in the array
     * @param object function
     * @return reduction
     */
    public static long reduceBy( final int[] array,  int length,
                                 Object object ) {


        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, length, object );
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object );
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for (int index=0; index < length; index++) {
                    int v = array[index];
                    sum = (long) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return handle(Long.class, throwable, "Unable to perform reduceBy");
            }
        } catch (Exception ex) {
            return reduceByR(array, length, object );
        }


    }



    /**
     * Reduce By
     * @param array array of items to reduce by
     * @param length where to end in the array
     * @param function function
     * @param functionName functionName
     * @return reduction
     */
    public static long reduceBy( final int[] array,  int length,
                                 Object function, String functionName ) {


        if (function.getClass().isAnonymousClass()) {
            return reduceByR(array, length, function, functionName );
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(function, functionName );
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for (int index=0; index < length; index++) {
                    int v = array[index];
                    sum = (long) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return handle(Long.class, throwable, "Unable to perform reduceBy");
            }
        } catch (Exception ex) {
            return reduceByR(array, length, function, functionName );
        }


    }


    /**
     * Reduce By
     * @param array array of items to reduce by
     * @param length where to end in the array
     * @param object function
     * @return reduction
     */
    public static long reduceBy( final int[] array, int start, int length,
                                 Object object ) {


        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object );
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object );
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for (int index=start; index < length; index++) {
                    int v = array[index];
                    sum = (long) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return handle(Long.class, throwable, "Unable to perform reduceBy");
            }
        } catch (Exception ex) {
            return reduceByR(array, object );
        }

    }

    /**
     * Checks to see if two values are the same
     * @param expected expected value
     * @param got got value
     * @return true if equal throws exception if not equal
     */
    public static boolean equalsOrDie(int expected, int got) {
        if (expected != got) {
            return die(Boolean.class, "Expected was", expected, "but we got ", got);
        }
        return true;
    }

    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or throws exception if not.
     */
    public static boolean equalsOrDie(int[] expected, int[] got) {

        if (expected.length != got.length) {
            die("Lengths did not match, expected length", expected.length,
                    "but got", got.length);
        }

        for (int index=0; index< expected.length; index++) {
            if (expected[index]!= got[index]) {
                die("value at index did not match index", index , "expected value",
                        expected[index],
                        "but got", got[index]);

            }
        }
        return true;
    }


    /**
     * Compares two values
     * @param expected expected value
     * @param got got value
     * @return true or false
     */
    public static boolean equals(int expected, int got) {

        return expected == got;
    }


    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or false if not.
     */
    public static boolean equals(int[] expected, int[] got) {

        if (expected.length != got.length) {
            return false;
        }

        for (int index=0; index< expected.length; index++) {
            if (expected[index]!= got[index]) {
               return false;
            }
        }
        return true;
    }

    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static int sum( int[] values ) {
        return sum( values, 0, values.length);
    }


    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static int sum( int[] values,  int length ) {
        return sum( values, 0, length);
    }

    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static int sum( int[] values, int start, int length ) {
        long sum = 0;
        for (int index = start; index < length; index++ ) {
            sum+= values[index];
        }

        if (sum < Integer.MIN_VALUE) {
            die ("overflow the sum is too small", sum);
        }


        if (sum > Integer.MAX_VALUE) {
            die ("overflow the sum is too big", sum);
        }

        return (int) sum;


    }



    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static long bigSum( int[] values ) {
        return bigSum(values, 0, values.length);
    }


    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static long bigSum( int[] values,  int length ) {
        return bigSum(values, 0, length);
    }

    /**
     * Big Sum
     * @param values values in int
     * @return sum
     */
    public static long bigSum( int[] values, int start, int length ) {
        long sum = 0;
        for (int index = start; index < length; index++ ) {
            sum+= values[index];
        }

        return sum;


    }



    /**
     * Max
     * @param values values in int
     * @return max
     */
    public static int max( int[] values, final int start, final int length ) {
        int max = Integer.MIN_VALUE;
        for (int index = start; index < length; index++ ) {
            if ( values[index] > max ) {
                max = values[index];
            }
        }

        return max;
    }


    /**
     * max
     * @param values values in int
     * @return max
     */
    public static int max( int[] values ) {
        return max(values, 0, values.length);
    }


    /**
     * max
     * @param values values in int
     * @return max
     */
    public static int max( int[] values, int length ) {
        return max(values, 0, length);
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static int min( int[] values, final int start, final int length ) {
        int min = Integer.MAX_VALUE;
        for (int index = start; index < length; index++ ) {
            if (values[index] < min) min = values[index];
        }
        return min;
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static int min( int[] values ) {
       return min(values, 0, values.length);
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static int min( int[] values, int length ) {
        return min(values, 0, length);
    }




    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static int mean( int[] values, final int start, final int length ) {
        return (int) Math.round(meanDouble(values, start, length));
    }




    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static int mean( int[] values, final int length ) {
        return (int) Math.round(meanDouble(values, 0, length));
    }


    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static int mean( int[] values ) {

        return (int) Math.round(meanDouble(values, 0, values.length));
    }



    /**
     * Calculate Variance.
     *
     * @param values values
     * @param start start
     * @param length length
     * @return variance
     */
    public static int variance(int[] values, final int start, final int length) {
        return (int) Math.round(varianceDouble(values, start, length));
    }


    private static double meanDouble( int[] values, final int start, final int length ) {
        double mean = ((double)bigSum(values, start, length))/ ((double) length);
        return mean;
    }



    /**
     * Calculate Variance.
     *
     * @param values values
     * @param start start
     * @param length length
     * @return variance
     */
    public static double varianceDouble(int[] values, final int start, final int length) {
        double mean = meanDouble(values, start, length);
        double temp = 0;
        for(int index = start; index < length; index++) {
            double a = values[index];
            temp += (mean-a)*(mean-a);
        }
        return temp / length;
    }


    /**
     * Calculate Variance.
     *
     * @param values values
     * @param length length
     * @return variance
     */
    public static int variance(int[] values,  final int length) {
        return (int) Math.round(varianceDouble(values, 0, length));
    }


    /**
     * Calculate Variance.
     *
     * @param values values
     * @return variance
     */
    public static int variance(int[] values) {
        return (int) Math.round(varianceDouble(values, 0, values.length));
    }


    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @param start start
     * @param length length
     * @return standard deviation
     */
    public static int standardDeviation(int[] values, final int start, final int length) {
        double variance = varianceDouble(values, start, length);
        return (int)Math.round(Math.sqrt(variance));
    }


    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @param length length
     * @return standard deviation
     */
    public static int standardDeviation(int[] values,  final int length) {
        double variance = varianceDouble(values, 0, length);
        return (int)Math.round(Math.sqrt(variance));
    }


    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @return standard deviation
     */
    public static int standardDeviation(int[] values) {
        double variance = varianceDouble(values, 0, values.length);
        return (int)Math.round(Math.sqrt(variance));
    }


    /**
     * Calculate Median
     *
     * @param start start
     * @param values values
     * @param length length
     * @return median
     */
    public static int median(int[] values, final int start, final int length) {
        int[] sorted = new int[length];
        System.arraycopy(values, start, sorted, 0, length);
        Arrays.sort(sorted);

        if (length % 2 == 0) {
            int middle = sorted.length / 2;
            double median = (sorted[middle-1] + sorted[middle]) / 2.0;
            return (int) Math.round(median);
        } else {
            return sorted[sorted.length / 2];
        }
    }


    /**
     * Calculate Median
     * @param values values
     * @param length length
     * @return median
     */
    public static int median(int[] values, final int length) {
        return median(values, 0, length);
    }


    /**
     * Calculate Median
     * @param values values
     * @return median
     */
    public static int median(int[] values) {
        return median(values, 0, values.length);
    }



    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or false if not.
     */
    public static boolean equals(int start, int end, int[] expected, int[] got) {

        if (expected.length != got.length) {
            return false;
        }

        for (int index=start; index< end; index++) {
            if (expected[index]!= got[index]) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(int array[]) {
        if (array == null)
            return 0;

        int result = 1;
        for (int element : array) {

            result = 31 * result + element;
        }

        return result;
    }

    public static int hashCode(int start, int end, int array[]) {
        if (array == null)
            return 0;

        int result = 1;

        for (int index=start; index< end; index++) {

            result = 31 * result + array[index];
        }

        return result;
    }


    /**
     * Calculate a sum of a property from a list.
     * @param inputList input list
     * @param propertyPath to item we want to sum
     * @return sum
     */
    public static long sum( Collection<?> inputList, String propertyPath ) {
        if (inputList.size() == 0 ) {
            return 0;
        }

        long sum = 0l;

        if (propertyPath.contains(".") || propertyPath.contains("[")) {

            String[] properties = StringScanner.splitByDelimiters(propertyPath, ".[]");

            for (Object o : inputList) {
                sum+=BeanUtils.getPropertyInt(o, properties);
            }

        } else {

            Map<String, FieldAccess> fields =  BeanUtils.getFieldsFromObject(inputList.iterator().next());
            FieldAccess fieldAccess = fields.get(propertyPath);
            for (Object o : inputList) {
                sum += fieldAccess.getInt(o);
            }
        }

        return sum;
    }

    private static double mean( Collection<?> inputList, String propertyPath ) {
        double mean = sum(inputList, propertyPath)/inputList.size();
        return Math.round(mean);
    }


    public static double variance(Collection<?> inputList, String propertyPath) {
        double mean = mean(inputList, propertyPath);
        double temp = 0;


        double a;

        if (propertyPath.contains(".") || propertyPath.contains("[")) {

            String[] properties = StringScanner.splitByDelimiters(propertyPath, ".[]");

            for (Object o : inputList) {
                a =BeanUtils.getPropertyInt(o, properties);
                temp += (mean-a)*(mean-a);
            }

        } else {

            Map<String, FieldAccess> fields =  BeanUtils.getFieldsFromObject(inputList.iterator().next());
            FieldAccess fieldAccess = fields.get(propertyPath);
            for (Object o : inputList) {
                a = fieldAccess.getInt(o);
                temp += (mean-a)*(mean-a);
            }
        }

        return Math.round(temp / inputList.size());

    }



    /**
     * Calculate standard deviation.
     *
     * @return standard deviation
     */
    public static double standardDeviation(Collection<?> inputList, String propertyPath) {
        double variance = variance(inputList, propertyPath);
        return Math.round(Math.sqrt(variance));
    }

    /**
     * Calculate standard deviation.
     *
     * @return standard deviation
     */
    public static int median(Collection<?> inputList, String propertyPath) {
        return IntList.toIntList(inputList, propertyPath).median();
    }


    /**
     * Round up to the nearest power of 2
     * @param number number you want to round up to.
     * @return rounded up to the power of 2.
     */
    public static int roundUpToPowerOf2( int number ) {
        int rounded = number >= 1_000
                ? 1_000
                : ( rounded = Integer.highestOneBit( number ) ) != 0
                ? ( Integer.bitCount( number ) > 1 ) ? rounded << 1 : rounded
                : 1;

        return rounded;
    }

}