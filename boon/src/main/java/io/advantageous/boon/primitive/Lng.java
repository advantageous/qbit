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
import io.advantageous.boon.Universal;
import io.advantageous.boon.core.reflection.Invoker;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;


public class Lng {


    public static String str( long value ) {
        return String.format( "%,d", value );
    }


    public static long[] grow( long[] array, final int size ) {
        Exceptions.requireNonNull( array );

        long[] newArray = new long[ array.length + size ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static long[] grow( long[] array ) {
        Exceptions.requireNonNull( array );

        long[] newArray = new long[ array.length * 2 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static long[] shrink( long[] array, int size ) {
        Exceptions.requireNonNull( array );

        long[] newArray = new long[ array.length - size ];

        System.arraycopy( array, 0, newArray, 0, array.length - size );
        return newArray;
    }


    public static long[] compact( long[] array ) {
        Exceptions.requireNonNull( array );

        int nullCount = 0;
        for ( long ch : array ) {

            if ( ch == '\0' ) {
                nullCount++;
            }
        }
        long[] newArray = new long[ array.length - nullCount ];

        int j = 0;
        for ( long ch : array ) {

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
    public static long[] arrayOfLong( final int size ) {
        return new long[ size ];
    }

    /**
     * @param array array
     * @return array
     */
    @Universal
    public static long[] array( final long... array ) {
        Exceptions.requireNonNull( array );
        return array;
    }


    @Universal
    public static int len( long[] array ) {
        return array.length;
    }


    @Universal
    public static int lengthOf( long[] array ) {
        return array.length;
    }


    @Universal
    public static long idx( final long[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }


    @Universal
    public static long atIndex( final long[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }


    @Universal
    public static void idx( final long[] array, int index, long value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }


    @Universal
    public static void atIndex( final long[] array, int index, long value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }


    @Universal
    public static long[] slc( long[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        long[] newArray = new long[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    @Universal
    public static long[] sliceOf( long[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        long[] newArray = new long[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    @Universal
    public static long[] slc( long[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        long[] newArray = new long[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    @Universal
    public static long[] sliceOf( long[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        long[] newArray = new long[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static long[] slcEnd( long[] array, int endIndex ) {

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        long[] newArray = new long[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }


    @Universal
    public static long[] endSliceOf( long[] array, int endIndex ) {

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        long[] newArray = new long[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static boolean in( long value, long[] array ) {
        for ( long currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }


    @Universal
    public static long[] copy( long[] array ) {
        Exceptions.requireNonNull( array );
        long[] newArray = new long[ array.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    @Universal
    public static long[] add( long[] array, long v ) {
        Exceptions.requireNonNull( array );
        long[] newArray = new long[ array.length + 1 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        newArray[ array.length ] = v;
        return newArray;
    }

    @Universal
    public static long[] add( long[] array, long[] array2 ) {
        Exceptions.requireNonNull( array );
        long[] newArray = new long[ array.length + array2.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        System.arraycopy( array2, 0, newArray, array.length, array2.length );
        return newArray;
    }


    @Universal
    public static long[] insert( final long[] array, final int idx, final long v ) {
        Exceptions.requireNonNull( array );

        if ( idx >= array.length ) {
            return add( array, v );
        }

        final int index = calculateIndex( array, idx );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        long[] newArray = new long[ array.length + 1 ];

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


    @Universal
    public static long[] insert( final long[] array, final int fromIndex, final long[] values ) {
        Exceptions.requireNonNull( array );

        if ( fromIndex >= array.length ) {
            return add( array, values );
        }

        final int index = calculateIndex( array, fromIndex );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        long[] newArray = new long[ array.length + values.length ];

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


    /* End universal methods. */
    private static int calculateIndex( long[] array, int originalIndex ) {
        final int length = array.length;

        Exceptions.requireNonNull( array, "array cannot be null" );


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



    /* End universal methods. */
    private static int calculateEndIndex( long[] array, int originalIndex ) {
        final int length = array.length;

        Exceptions.requireNonNull( array, "array cannot be null" );


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
        long reduce(long sum, long value);
    }

    /**
     * A very fast reduce by.
     * If performance is your thing, this seems to be as fast a plain for loop when benchmarking with JMH.
     *
     * @param array array of items to reduce by
     * @param reduceBy reduceBy interface
     * @return the final value
     */
    public static long reduceBy( final long[] array, ReduceBy reduceBy ) {


        long sum = 0;
        for ( long v : array ) {
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
    public static long reduceBy( final long[] array, final int start, final int length, ReduceBy reduceBy ) {


        long sum = 0;

        for (int index = start; index < length; index++) {
            long v = array[index];
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
    public static long reduceBy( final long[] array, final int length, ReduceBy reduceBy ) {


        long sum = 0;

        for (int index = 0; index < length; index++) {
            long v = array[index];
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
    public  static <T> long reduceBy( final long[] array, T object ) {
        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object );
        }


        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object);
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for ( long v : array ) {
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
    public static <T> long reduceBy( final long[] array, T object, String methodName ) {

        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object, methodName);
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object, methodName);
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                long sum = 0;
                for ( long v : array ) {
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
    private  static <T> long reduceByR( final long[] array, T object ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object);


            long sum = 0;
            for ( long v : array ) {
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
    private  static <T> long reduceByR( final long[] array, T object, String methodName ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object, methodName);


            long sum = 0;
            for ( long v : array ) {
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
    private  static <T> long reduceByR( final long[] array, int length, T object, String methodName ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object, methodName);


            long sum = 0;
            for (int index=0; index< length; index++) {
                long v = array[index];
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
    private  static <T> long reduceByR( final long[] array, int length, T object ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object);


            long sum = 0;
            for (int index=0; index< length; index++) {
                long v = array[index];
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
    public static long reduceBy( final long[] array,  int length,
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
                    long v = array[index];
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
    public static long reduceBy( final long[] array,  int length,
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
                    long v = array[index];
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
    public static long reduceBy( final long[] array, int start, int length,
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
                    long v = array[index];
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
     * Some quick validation for an expected value
     * @param expected expected this
     * @param got got this
     * @return returns true or throws an exception
     */
    public static boolean equalsOrDie(long expected, long got) {
        if (expected != got) {
            return die(Boolean.class, "Expected was", expected, "but we got ", got);
        }
        return true;
    }


    /**
     * Some quick validation for an expected value
     * @param expected expected this
     * @param got got this
     * @return returns true or false
     */
    public static boolean equals(long expected, long got) {

        return expected == got;
    }



    /**
     * Sum
     * @param values values in int
     * @return sum
     */
    public static long sum( long[] values ) {
        return sum(values, 0, values.length);
    }


    /**
     * Sum
     * @param values values in int
     * @return sum
     */
    public static long sum( long[] values,  int length ) {
        return sum(values, 0, length);
    }

    /**
     * Big Sum
     * @param values values in int
     * @return sum
     */
    public static long sum( long[] values, int start, int length ) {
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
    public static long max( long[] values, final int start, final int length ) {
        long max = Long.MIN_VALUE;
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
    public static long max( long[] values ) {
        return max(values, 0, values.length);
    }


    /**
     * max
     * @param values values in int
     * @return max
     */
    public static long max( long[] values, int length ) {
        return max(values, 0, length);
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static long min( long[] values, final int start, final int length ) {
        long min = Long.MAX_VALUE;
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
    public static long min( long[] values ) {
        return min(values, 0, values.length);
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static long min( long[] values, int length ) {
        return min(values, 0, length);
    }




    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static long mean( long[] values, final int start, final int length ) {
        return (long) Math.round(meanDouble(values, start, length));
    }




    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static long mean( long[] values, final int length ) {
        return Math.round(meanDouble(values, 0, length));
    }


    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static long mean( long[] values ) {

        return Math.round(meanDouble(values, 0, values.length));
    }


    /**
     * Calculates variance
     * @param values values
     * @param start start
     * @param length length
     * @return variance
     */
    public static long variance(long[] values, final int start, final int length) {
        return (long) Math.round(varianceDouble(values, start, length));
    }


    /**
     * Used internally to avoid loss and rounding errors a bit.
     * @param values values
     * @param start start
     * @param length length
     * @return meanDouble
     */
    public static double meanDouble( long[] values, final int start, final int length ) {
        double mean = ((double)sum(values, start, length))/ ((double) length);
        return mean;
    }

    /**
     * Internal to avoid rounding errors
     * @param values values
     * @param start start
     * @param length length
     * @return double value
     */
    public static double varianceDouble(long[] values, final int start, final int length) {
        double mean = meanDouble(values, start, length);
        double temp = 0;
        for(int index = start; index < length; index++) {
            double a = values[index];
            temp += (mean-a)*(mean-a);
        }
        return temp / length;
    }

    /**
     * Calculate variance
     * @param values values
     * @param length length
     * @return variance variance
     */
    public static long variance(long[] values,  final int length) {
        return Math.round(varianceDouble(values, 0, length));
    }

    /**
     * Calculate variance
     * @param values values
     * @return variance variance
     */
    public static long variance(long[] values) {
        return Math.round(varianceDouble(values, 0, values.length));
    }

    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @param start start
     * @param length length
     * @return standard deviation
     */
    public static long standardDeviation(long[] values, final int start, final int length) {
        double variance = varianceDouble(values, start, length);
        return (int)Math.round(Math.sqrt(variance));
    }

    /**
     * Calculate standard deviation.
     * @param values values
     * @param length length
     * @return standard deviation
     */
    public static long standardDeviation(long[] values,  final int length) {
        double variance = varianceDouble(values, 0, length);
        return Math.round(Math.sqrt(variance));
    }


    /**
     * Calculate Standard Deviation
     * @param values values
     * @return standardDeviation
     */
    public static int standardDeviation(long[] values) {
        double variance = varianceDouble(values, 0, values.length);
        return (int)Math.round(Math.sqrt(variance));
    }

    /**
     * Calculate Median
     * @param values values
     * @param start start
     * @param length length
     * @return median
     */
    public static long median(long[] values, final int start, final int length) {
        long[] sorted = new long[length];
        System.arraycopy(values, start, sorted, 0, length);
        Arrays.sort(sorted);

        if (length % 2 == 0) {
            int middle = sorted.length / 2;
            double median = (sorted[middle-1] + sorted[middle]) / 2.0;
            return Math.round(median);
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
    public static long median(long[] values, final int length) {
        return median(values, 0, length);
    }


    /**
     * Calculate Median
     * @param values values
     * @return median
     */
    public static long median(long[] values) {
        return median(values, 0, values.length);
    }



    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or throws exception if not.
     */
    public static boolean equalsOrDie(long[] expected, long[] got) {

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
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or false if not.
     */
    public static boolean equals(long[] expected, long[] got) {

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
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or false if not.
     */
    public static boolean equals(int start, int end, long[] expected, long[] got) {

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

    public static int hashCode(long array[]) {
        if (array == null)
            return 0;

        int result = 1;
        for (long element : array) {
            int elementHash = (int)(element ^ (element >>> 32));
            result = 31 * result + elementHash;
        }

        return result;
    }

    public static int hashCode(int start, int end, long array[]) {
        if (array == null)
            return 0;

        int result = 1;

        for (int index=start; index< end; index++) {
            long element = array[index];
            int elementHash = (int)(element ^ (element >>> 32));
            result = 31 * result + elementHash;

        }

        return result;
    }

}
