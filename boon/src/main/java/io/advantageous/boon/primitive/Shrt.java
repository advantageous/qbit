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

import static io.advantageous.boon.Exceptions.die;


public class Shrt {



    public static short[] shorts( short... array ) {
        return array;
    }

    public static short[] grow( short[] array, final int size ) {
        Exceptions.requireNonNull(array);

        short[] newArray = new short[ array.length + size ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static short[] grow( short[] array ) {
        Exceptions.requireNonNull( array );

        short[] newArray = new short[ array.length * 2 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static short[] shrink( short[] array, int size ) {
        Exceptions.requireNonNull( array );

        short[] newArray = new short[ array.length - size ];

        System.arraycopy( array, 0, newArray, 0, array.length - size );
        return newArray;
    }


    public static short[] compact( short[] array ) {
        Exceptions.requireNonNull( array );

        int nullCount = 0;
        for ( short ch : array ) {

            if ( ch == '\0' ) {
                nullCount++;
            }
        }
        short[] newArray = new short[ array.length - nullCount ];

        int j = 0;
        for ( short ch : array ) {

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
    public static short[] arrayOfShort( final int size ) {
        return new short[ size ];
    }

    /**
     * @param array array
     * @return array
     */
    @Universal
    public static short[] array( final short... array ) {
        Exceptions.requireNonNull( array );
        return array;
    }


    @Universal
    public static int len( short[] array ) {
        return array.length;
    }


    @Universal
    public static int lengthOf( short[] array ) {
        return array.length;
    }



    @Universal
    public static short idx( final short[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }


    @Universal
    public static void idx( final short[] array, int index, short value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }


    @Universal
    public static short[] slc( short[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        short[] newArray = new short[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }


    @Universal
    public static short[] sliceOf( short[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        short[] newArray = new short[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static short[] slc( short[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        short[] newArray = new short[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static short[] sliceOf( short[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        short[] newArray = new short[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static short[] endSliceOf( short[] array, int endIndex ) {
        Exceptions.requireNonNull( array );

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        short[] newArray = new short[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static short[] slcEnd( short[] array, int endIndex ) {
        Exceptions.requireNonNull( array );

        final int end = calculateEndIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        short[] newArray = new short[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }

    @Universal
    public static boolean in( short value, short[] array ) {
        for ( short currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }


    @Universal
    public static short[] copy( short[] array ) {
        Exceptions.requireNonNull( array );
        short[] newArray = new short[ array.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    @Universal
    public static short[] add( short[] array, short v ) {
        Exceptions.requireNonNull( array );
        short[] newArray = new short[ array.length + 1 ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        newArray[ array.length ] = v;
        return newArray;
    }

    @Universal
    public static short[] add( short[] array, short[] array2 ) {
        Exceptions.requireNonNull( array );
        short[] newArray = new short[ array.length + array2.length ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        System.arraycopy( array2, 0, newArray, array.length, array2.length );
        return newArray;
    }


    @Universal
    public static short[] insert( final short[] array, final int idx, final short v ) {
        Exceptions.requireNonNull( array );

        if ( idx >= array.length ) {
            return add( array, v );
        }

        final int index = calculateIndex( array, idx );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        short[] newArray = new short[ array.length + 1 ];

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
    public static short[] insert( final short[] array, final int fromIndex, final short[] values ) {
        Exceptions.requireNonNull( array );

        if ( fromIndex >= array.length ) {
            return add( array, values );
        }

        final int index = calculateIndex( array, fromIndex );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        short[] newArray = new short[ array.length + values.length ];

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
    private static int calculateIndex( short[] array, int originalIndex ) {
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
    private static int calculateEndIndex( short[] array, int originalIndex ) {
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


    public static int reduceBy( final short[] array, Object object ) {

        int sum = 0;
        for ( short v : array ) {
            sum = (short) Invoker.invokeReducer(object, sum, v);
        }
        return sum;
    }



    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or throws exception if not.
     */
    public static boolean equalsOrDie(short[] expected, short[] got) {

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
    public static boolean equals(short[] expected, short[] got) {

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


}
