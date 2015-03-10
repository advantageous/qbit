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
import io.advantageous.boon.core.reflection.Invoker;
import io.advantageous.boon.Universal;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;


public class Flt {


    public static float[] grow( float[] array, final int size ) {
        Exceptions.requireNonNull(array);

        float[] newArray = new float[ array.length + size ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static float[] grow( float[] array ) {
        Exceptions.requireNonNull( array );

        float[] newArray = new float[ array.length * 2 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static float[] shrink( float[] array, int size ) {
        Exceptions.requireNonNull( array );

        float[] newArray = new float[ array.length - size ];

        System.arraycopy( array, 0, newArray, 0, array.length - size );
        return newArray;
    }


    public static float[] compact( float[] array ) {
        Exceptions.requireNonNull( array );

        int nullCount = 0;
        for ( float ch : array ) {

            if ( ch == '\0' ) {
                nullCount++;
            }
        }
        float[] newArray = new float[ array.length - nullCount ];

        int j = 0;
        for ( float ch : array ) {

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
    public static float[] arrayOfFloat( final int size ) {
        return new float[ size ];
    }

    /**
     * @param array array
     * @return array
     */
    @Universal
    public static float[] array( final float... array ) {
        Exceptions.requireNonNull( array );
        return array;
    }



    @Universal
    public static int lengthOf( float[] array ) {
        return len(array);
    }

    @Universal
    public static int len( float[] array ) {
        return array.length;
    }


    @Universal
    public static float atIndex( final float[] array, final int index ) {
        return idx(array, index);
    }

    @Universal
    public static float idx( final float[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }



    @Universal
    public static void atIndex( final float[] array, int index, float value ) {
        idx (array, index, value);
    }

    @Universal
    public static void idx( final float[] array, int index, float value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }

    @Universal
    public static float[] sliceOf( float[] array, int startIndex, int endIndex ) {

        return slc(array, startIndex, endIndex);
    }

    @Universal
    public static float[] slc( float[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        float[] newArray = new float[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    @Universal
    public static float[] sliceOf( float[] array, int startIndex ) {

        return slc(array, startIndex);
    }

    @Universal
    public static float[] slc( float[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        float[] newArray = new float[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }



    @Universal
    public static float[] endOfSlice( float[] array, int endIndex ) {
        return slcEnd(array, endIndex);
    }

    @Universal
    public static float[] slcEnd( float[] array, int endIndex ) {

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        float[] newArray = new float[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static boolean in( float value, float[] array ) {
        for ( float currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }


    @Universal
    public static float[] copy( float[] array ) {
        Exceptions.requireNonNull( array );
        float[] newArray = new float[ array.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    @Universal
    public static float[] add( float[] array, float v ) {
        Exceptions.requireNonNull( array );
        float[] newArray = new float[ array.length + 1 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        newArray[ array.length ] = v;
        return newArray;
    }

    @Universal
    public static float[] add( float[] array, float[] array2 ) {
        Exceptions.requireNonNull( array );
        float[] newArray = new float[ array.length + array2.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        System.arraycopy( array2, 0, newArray, array.length, array2.length );
        return newArray;
    }


    @Universal
    public static float[] insert( final float[] array, final int idx, final float v ) {

        if ( idx >= array.length ) {
            return add( array, v );
        }

        final int index = calculateIndex( array, idx );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        float[] newArray = new float[ array.length + 1 ];

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
    public static float[] insert( final float[] array, final int fromIndex, final float[] values ) {
        Exceptions.requireNonNull( array );

        if ( fromIndex >= array.length ) {
            return add( array, values );
        }

        final int index = calculateIndex( array, fromIndex );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        float[] newArray = new float[ array.length + values.length ];

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
    private static int calculateIndex( float[] array, int originalIndex ) {
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


    /* End universal methods. */
    private static int calculateEndIndex( float[] array, int originalIndex ) {
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


    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or throws exception if not.
     */
    public static boolean equalsOrDie(float[] expected, float[] got) {

        if (expected.length != got.length) {
            Exceptions.die("Lengths did not match, expected length", expected.length,
                    "but got", got.length);
        }

        for (int index=0; index< expected.length; index++) {
            if (expected[index]!= got[index]) {
                Exceptions.die("value at index did not match index", index, "expected value",
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
    public static boolean equals(float[] expected, float[] got) {

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


    /** Public interface for a very fast reduce by. */
    public static interface ReduceBy {
        double reduce(double sum, float value);
    }

    /**
     * A very fast reduce by.
     * If performance is your thing, this seems to be as fast a plain for loop when benchmarking with JMH.
     *
     * @param array array of items to reduce by
     * @param reduceBy reduceBy interface
     * @return the final value
     */
    public static double reduceBy( final float[] array, ReduceBy reduceBy ) {


        double sum = 0;
        for ( float v : array ) {
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
    public static double reduceBy( final float[] array, final int start, final int length, ReduceBy reduceBy ) {


        double sum = 0;

        for (int index = start; index < length; index++) {
            float v = array[index];
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
    public static double reduceBy( final float[] array, final int length, ReduceBy reduceBy ) {


        double sum = 0;

        for (int index = 0; index < length; index++) {
            float v = array[index];
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
    public  static <T> double reduceBy( final float[] array, T object ) {
        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object );
        }


        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object);
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                double sum = 0;
                for ( float v : array ) {
                    sum = (double) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    public static <T> double reduceBy( final float[] array, T object, String methodName ) {

        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object, methodName);
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object, methodName);
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                double sum = 0;
                for ( float v : array ) {
                    sum = (double) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    private  static <T> double reduceByR( final float[] array, T object ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object);


            double sum = 0;
            for ( float v : array ) {
                sum = (double) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    private  static <T> double reduceByR( final float[] array, T object, String methodName ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object, methodName);


            double sum = 0;
            for ( float v : array ) {
                sum = (double) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    private  static <T> double reduceByR( final float[] array, int length, T object, String methodName ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object, methodName);


            double sum = 0;
            for (int index=0; index< length; index++) {
                float v = array[index];
                sum = (double) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
        }

    }


    /**
     * Reflection based reduce by.
     * @param array array of items to reduce by
     * @param object function
     * @param <T> type of function
     * @return reduction
     */
    private  static <T> double reduceByR( final float[] array, int length, T object ) {
        try {

            Method method = Invoker.invokeReducerLongIntReturnLongMethod(object);


            double sum = 0;
            for (int index=0; index< length; index++) {
                float v = array[index];
                sum = (double) method.invoke(object, sum, v);

            }
            return sum;

        } catch (Throwable throwable) {
            return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
        }

    }

    /**
     * Reduce By
     * @param array array of items to reduce by
     * @param length where to end in the array
     * @param object function
     * @return reduction
     */
    public static double reduceBy( final float[] array,  int length,
                                 Object object ) {


        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, length, object );
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object );
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                double sum = 0;
                for (int index=0; index < length; index++) {
                    float v = array[index];
                    sum = (double) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    public static double reduceBy( final float[] array,  int length,
                                 Object function, String functionName ) {


        if (function.getClass().isAnonymousClass()) {
            return reduceByR(array, length, function, functionName );
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(function, functionName );
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                double sum = 0;
                for (int index=0; index < length; index++) {
                    float v = array[index];
                    sum = (double) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    public static double reduceBy( final float[] array, int start, int length,
                                 Object object ) {


        if (object.getClass().isAnonymousClass()) {
            return reduceByR(array, object );
        }

        try {
            ConstantCallSite callSite = Invoker.invokeReducerLongIntReturnLongMethodHandle(object );
            MethodHandle methodHandle = callSite.dynamicInvoker();
            try {

                double sum = 0;
                for (int index=start; index < length; index++) {
                    float v = array[index];
                    sum = (double) methodHandle.invokeExact( sum, v );

                }
                return sum;
            } catch (Throwable throwable) {
                return Exceptions.handle(Long.class, throwable, "Unable to perform reduceBy");
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
    public static boolean equalsOrDie(float expected, float got) {
        if (expected != got) {
            return Exceptions.die(Boolean.class, "Expected was", expected, "but we got ", got);
        }
        return true;
    }


    /**
     * Compares two values
     * @param expected expected value
     * @param got got value
     * @return true or false
     */
    public static boolean equals(float expected, float got) {

        return expected == got;
    }



    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static double sum( float[] values ) {
        return sum( values, 0, values.length);
    }


    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static float sum( float[] values,  int length ) {
        return sum( values, 0, length);
    }

    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static float sum( float[] values, int start, int length ) {
        double sum = 0;
        for (int index = start; index < length; index++ ) {
            sum+= values[index];
        }

        if (sum < Float.MIN_VALUE) {
            Exceptions.die("overflow the sum is too small", sum);
        }


        if (sum > Float.MAX_VALUE) {
            Exceptions.die("overflow the sum is too big", sum);
        }

        return (float) sum;


    }



    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static double bigSum( float[] values ) {
        return bigSum(values, 0, values.length);
    }


    /**
     * Sum
     * Provides overflow protection.
     * @param values values in int
     * @return sum
     */
    public static double bigSum( float[] values,  int length ) {
        return bigSum(values, 0, length);
    }

    /**
     * Big Sum
     * @param values values in int
     * @return sum
     */
    public static double bigSum( float[] values, int start, int length ) {
        double sum = 0;
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
    public static float max( float[] values, final int start, final int length ) {
        float max = Float.MIN_VALUE;
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
    public static float max( float[] values ) {
        return max(values, 0, values.length);
    }


    /**
     * max
     * @param values values in int
     * @return max
     */
    public static float max( float[] values, int length ) {
        return max(values, 0, length);
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static float min( float[] values, final int start, final int length ) {
        float min = Float.MAX_VALUE;
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
    public static float min( float[] values ) {
        return min(values, 0, values.length);
    }


    /**
     * Min
     * @param values values in int
     * @return min
     */
    public static float min( float[] values, int length ) {
        return min(values, 0, length);
    }




    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static float mean( float[] values, final int start, final int length ) {
        return (float) meanDouble(values, start, length);
    }




    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static float mean( float[] values, final int length ) {
        return (float) meanDouble(values, 0, length);
    }


    /**
     * Average
     * @param values values in int
     * @return average
     */
    public static float mean( float[] values ) {

        return (float) meanDouble(values, 0, values.length);
    }



    /**
     * Calculate Variance.
     *
     * @param values values
     * @param start start
     * @param length length
     * @return variance
     */
    public static float variance(float[] values, final int start, final int length) {
        return (float) varianceDouble(values, start, length);
    }


    private static double meanDouble( float[] values, final int start, final int length ) {
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
    public static double varianceDouble(float[] values, final int start, final int length) {
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
    public static float variance(float[] values,  final int length) {
        return (float) varianceDouble(values, 0, length);
    }


    /**
     * Calculate Variance.
     *
     * @param values values
     * @return variance
     */
    public static float variance(float[] values) {
        return (float) varianceDouble(values, 0, values.length);
    }


    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @param start start
     * @param length length
     * @return standard deviation
     */
    public static float standardDeviation(float[] values, final int start, final int length) {
        double variance = varianceDouble(values, start, length);
        return (float)Math.sqrt(variance);
    }


    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @param length length
     * @return standard deviation
     */
    public static float standardDeviation(float[] values,  final int length) {
        double variance = varianceDouble(values, 0, length);
        return (float)Math.sqrt(variance);
    }


    /**
     * Calculate standard deviation.
     *
     * @param values values
     * @return standard deviation
     */
    public static float standardDeviation(float[] values) {
        double variance = varianceDouble(values, 0, values.length);
        return (float)Math.sqrt(variance);
    }


    /**
     * Calculate Median
     *
     * @param start start
     * @param values values
     * @param length length
     * @return median
     */
    public static float median(float[] values, final int start, final int length) {
        float[] sorted = new float[length];
        System.arraycopy(values, start, sorted, 0, length);
        Arrays.sort(sorted);

        if (length % 2 == 0) {
            int middle = sorted.length / 2;
            double median = (sorted[middle-1] + sorted[middle]) / 2.0;
            return (float) median;
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
    public static float median(float[] values, final int length) {
        return median(values, 0, length);
    }


    /**
     * Calculate Median
     * @param values values
     * @return median
     */
    public static float median(float[] values) {
        return median(values, 0, values.length);
    }



    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or false if not.
     */
    public static boolean equals(int start, int end, float[] expected, float[] got) {

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

    public static int hashCode(float array[]) {
        if (array == null) {
            return 0;
        }

        int result = 1;
        for (float item : array) {

            int bits = Float.floatToIntBits(item);
            result = 31 * result + bits;
        }

        return result;
    }

    public static int hashCode(int start, int end, float array[]) {
        if (array == null) {
            return 0;
        }

        int result = 1;

        for (int index=start; index< end; index++) {

            int bits = Float.floatToIntBits(array[index]);
            result = 31 * result + bits;
        }

        return result;
    }


}
