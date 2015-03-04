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


import java.util.Arrays;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;

public class ByteScanner {


    public static boolean isDigits( final char[] inputArray ) {
        for ( int index = 0; index < inputArray.length; index++ ) {
            char a = inputArray[ index ];
            if ( !CharScanner.isDigit(a) ) {
                return false;
            }
        }
        return true;
    }


    public static boolean hasDecimalChar( byte[] chars, boolean negative ) {

        int index =0;

        if (negative) index++;

        for (; index < chars.length; index++) {
            switch ( chars[index] ) {
                case CharScanner.MINUS:
                case CharScanner.PLUS:
                case CharScanner.LETTER_E:
                case CharScanner.LETTER_BIG_E:
                case CharScanner.DECIMAL_POINT:
                    return true;
            }
        }
        return false;

    }


    public static byte[][] splitExact( final byte[] inputArray,
                                       final int split, final int resultsArrayLength ) {
        /** Holds the results. */
        byte[][] results = new byte[ resultsArrayLength ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        byte c = 0;
        int index = 0;

        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {

                results[ resultIndex ] = Byt.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the byte

                currentLineLength = 0;
                resultIndex++;
            }
        }

        if ( c != split ) {

            results[ resultIndex ] = Byt.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
            resultIndex++;
        }

        int actualLength = resultIndex;
        if ( actualLength < resultsArrayLength ) {
            final int newSize = resultsArrayLength - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static byte[][] splitExact( final byte[] inputArray,
                                       final int resultsArrayLength, int... delims ) {
        /** Holds the results. */
        byte[][] results = new byte[ resultsArrayLength ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        byte c = '\u0000';
        int index = 0;
        int j;
        int split;


        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    results[ resultIndex ] = Byt.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the byte

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Byt.inIntArray( c, delims ) ) {

            results[ resultIndex ] = Byt.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
            resultIndex++;
        }


        int actualLength = resultIndex;
        if ( actualLength < resultsArrayLength ) {
            final int newSize = resultsArrayLength - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static byte[][] split( final byte[] inputArray,
                                  final int split ) {
        /** Holds the results. */
        byte[][] results = new byte[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        byte c = '\u0000';
        int index = 0;

        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {

                if ( resultIndex == results.length ) {

                    results = _grow( results );
                }


                results[ resultIndex ] = Byt.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the byte

                currentLineLength = 0;
                resultIndex++;
            }
        }

        if ( c != split ) {

            results[ resultIndex ] = Byt.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
            resultIndex++;
        }

        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static byte[][] splitByChars( final byte[] inputArray,
                                         char... delims ) {
        /** Holds the results. */
        byte[][] results = new byte[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        byte c = '\u0000';
        int index = 0;
        int j;
        int split;


        for (; index < inputArray.length; index++, currentLineLength++ ) {

            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    if ( resultIndex == results.length ) {

                        results = _grow( results );
                    }


                    results[ resultIndex ] = Byt.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the byte

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Chr.in(c, delims) ) {

            results[ resultIndex ] = Byt.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
            resultIndex++;
        }


        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }


    public static byte[][] splitByCharsFromToDelims( final byte[] inputArray, int from, int to,
                                                     final byte... delims ) {
        /** Holds the results. */
        byte[][] results = new byte[ 16 ][];

        final int length = to - from;

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        int c = '\u0000';
        int index = from;
        int j;
        int split;


        for (; index < length; index++, currentLineLength++ ) {

            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    if ( resultIndex == results.length ) {

                        results = _grow( results );
                    }


                    results[ resultIndex ] = Byt.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the char

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Byt.in( c, delims ) ) {

            results[ resultIndex ] = Byt.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
            resultIndex++;
        }


        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static byte[][] splitByCharsNoneEmpty( byte[] inputArray,
                                                  char... delims ) {

        final byte[][] results = splitByChars( inputArray, delims );
        return compact( results );
    }


    public static byte[][] splitByCharsNoneEmpty( final byte[] inputArray, int from, int to,
                                                  final byte... delims ) {

        final byte[][] results = splitByCharsFromToDelims( inputArray, from, to, delims );
        return compact( results );
    }

    public static byte[][] compact( byte[][] array ) {

        int nullCount = 0;
        for ( byte[] ch : array ) {

            if ( ch == null || ch.length == 0 ) {
                nullCount++;
            }
        }
        byte[][] newArray = new byte[ array.length - nullCount ][];

        int j = 0;
        for ( byte[] ch : array ) {

            if ( ch == null || ch.length == 0 ) {
                continue;
            }

            newArray[ j ] = ch;
            j++;
        }
        return newArray;
    }


    private static byte[][] _grow( byte[][] array ) {
        Exceptions.requireNonNull(array);

        byte[][] newArray = new byte[ array.length * 2 ][];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }

    private static byte[][] __shrink( byte[][] array, int size ) {
        Exceptions.requireNonNull( array );
        byte[][] newArray = new byte[ array.length - size ][];

        System.arraycopy( array, 0, newArray, 0, array.length - size );
        return newArray;
    }


    final static String MIN_INT_STR_NO_SIGN = String.valueOf( Integer.MIN_VALUE ).substring( 1 );
    final static String MAX_INT_STR = String.valueOf( Integer.MAX_VALUE );


    final static String MIN_LONG_STR_NO_SIGN = String.valueOf( Long.MIN_VALUE ).substring( 1 );
    final static String MAX_LONG_STR = String.valueOf( Long.MAX_VALUE );


    public static boolean isInteger( byte[] digitChars, int offset, int len
                                      ) {
        String cmpStr = digitChars[offset] == '-' ? MIN_INT_STR_NO_SIGN : MAX_INT_STR;
        int cmpLen = cmpStr.length();
        if ( len < cmpLen ) return true;
        if ( len > cmpLen ) return false;

        for ( int i = 0; i < cmpLen; ++i ) {
            int diff = digitChars[ offset + i ] - cmpStr.charAt( i );
            if ( diff != 0 ) {
                return ( diff < 0 );
            }
        }
        return true;
    }

    public static boolean isLong( byte[] digitChars, int offset, int len
                                   ) {
        String cmpStr = digitChars[offset] == '-' ? MIN_INT_STR_NO_SIGN : MAX_INT_STR;
        int cmpLen = cmpStr.length();
        if ( len < cmpLen ) return true;
        if ( len > cmpLen ) return false;

        for ( int i = 0; i < cmpLen; ++i ) {
            int diff = digitChars[ offset + i ] - cmpStr.charAt( i );
            if ( diff != 0 ) {
                return ( diff < 0 );
            }
        }
        return true;
    }


    public static int parseInt( byte[] digitChars ) {
        return parseIntFromTo( digitChars, 0, digitChars.length );
    }




    public static int parseIntFromTo( byte[] digitChars, int offset, int to ) {

        try {


            int num;
            boolean negative=false;
            int c = digitChars[ offset ];
            if (c == '-') {
                offset++;
                negative=true;
            }
            if (negative) {
                num = (digitChars[ offset ] - '0');
                if ( ++offset < to ) {
                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                    if ( ++offset < to ) {
                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                        if ( ++offset < to ) {
                            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                            if ( ++offset < to ) {
                                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                if ( ++offset < to ) {
                                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                    if ( ++offset < to ) {
                                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                        if ( ++offset < to ) {
                                            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                            if ( ++offset < to ) {
                                                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                                if ( ++offset < to ) {
                                                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                num = (digitChars[ offset ] - '0');
                if ( ++offset < to ) {
                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                    if ( ++offset < to ) {
                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                        if ( ++offset < to ) {
                            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                            if ( ++offset < to ) {
                                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                if ( ++offset < to ) {
                                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                    if ( ++offset < to ) {
                                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                        if ( ++offset < to ) {
                                            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                            if ( ++offset < to ) {
                                                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                                if ( ++offset < to ) {
                                                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
            return negative ? num*-1 : num;
        } catch ( Exception ex ) {
            return Exceptions.handle(int.class, ex);
        }
    }

    public static int parseIntIgnoreDot( byte[] digitChars, int offset, int len ) {
        int num = digitChars[ offset ] - '0';
        int to = len + offset;
        // This looks ugly, but appears the fastest way (as per measurements)
        if ( ++offset < to ) {
            num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
            if ( ++offset < to ) {
                num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                if ( ++offset < to ) {
                    num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                    if ( ++offset < to ) {
                        num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                        if ( ++offset < to ) {
                            num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                            if ( ++offset < to ) {
                                num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                if ( ++offset < to ) {
                                    num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                    if ( ++offset < to ) {
                                        num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                        if ( ++offset < to ) {
                                            num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return num;
    }

    public static long parseLong( byte[] digitChars, int offset, int len ) {
        int len1 = len - 9;
        long val = parseIntFromTo(digitChars, offset, len1) * L_BILLION;
        return val + ( long ) parseIntFromTo(digitChars, offset + len1, 9);
    }

    public static long parseLongIgnoreDot( byte[] digitChars, int offset, int len ) {
        int len1 = len - 9;
        long val = parseIntIgnoreDot( digitChars, offset, len1 ) * L_BILLION;
        return val + ( long ) parseIntIgnoreDot( digitChars, offset + len1, 9 );
    }



    public static Number parseJsonNumber( byte[] buffer ) {
        return parseJsonNumber( buffer, 0, buffer.length );
    }



    public static Number parseJsonNumber( byte[] buffer, int from, int to ) {
        return parseJsonNumber( buffer, from, to, null );
    }


    public static Number parseJsonNumber( byte[] buffer, int from, int max, int size[] ) {
        Number value = null;
        boolean simple = true;
        int digitsPastPoint = 0;

        int index = from;


        if (buffer[index] == '-') {
            index++;
        }

        boolean foundDot = false;
        for (;index<max; index++)  {
            int ch = buffer[ index ];
            if ( CharScanner.isNumberDigit(ch) ) {

                if (foundDot==true) {
                    digitsPastPoint++;
                }
            } else if ( ch <= 32 || CharScanner.isDelimiter(ch) ) { break;}
            else if ( ch == '.' ) {
                foundDot = true;
            }
            else if (ch == 'E' || ch == 'e' || ch == '-' || ch == '+') {
                simple = false;
            } else {
                Exceptions.die("unexpected character " + ch);
            }
        }


        if ( digitsPastPoint >= powersOf10.length-1 ) {
            simple = false;
        }


        final int length = index -from;

        if (!foundDot && simple) {
            if ( isInteger( buffer, from, length ) ) {
                value = parseIntFromTo( buffer, from, index );
            } else {
                value = parseLongFromTo( buffer, from, index );
            }
        }
        else if ( foundDot && simple ) {
            long lvalue;


            if ( length < powersOf10.length ) {

                if ( isInteger( buffer, from, length ) ) {
                    lvalue = parseIntFromToIgnoreDot( buffer, from, index );
                } else {
                    lvalue = parseLongFromToIgnoreDot( buffer, from, index );
                }

                double power = powersOf10[ digitsPastPoint ];
                value = lvalue / power;

            } else {
                value =  Double.parseDouble( new String( buffer, from, length ) );

            }


        } else {
            value =  Double.parseDouble( new String( buffer, from, index - from ) );
        }


        if (size != null) {
            size[0] = index;
        }

        return value;
    }


    public static long parseLongFromTo( byte[] digitChars, int offset, int to ) {


        long num;
        boolean negative=false;
        int c = digitChars[ offset ];
        if (c == '-') {
            offset++;
            negative=true;
        }

        c = digitChars[ offset ];
        num = (c - '0');
        offset++;

        long digit;

        for (; offset < to; offset++) {
            c = digitChars [offset];
            digit = ( c - '0' );
            num = ( num * 10 ) + digit;
        }

        return negative ? num * -1 : num;

    }


    public static int parseIntFromToIgnoreDot( byte[] digitChars, int offset, int to ) {

        int num;
        boolean negative=false;
        int c = digitChars[ offset ];
        if (c == '-') {
            offset++;
            negative=true;
        }

        c = digitChars[ offset ];
        num = (c - '0');
        offset++;

        for (; offset < to; offset++) {
            c = digitChars[ offset ];
            if (c != '.') {
                num = ( num * 10 ) + ( c - '0' );
            }

        }

        return negative ? num * -1 : num;
    }


    public static long parseLongFromToIgnoreDot( byte[] digitChars, int offset, int to ) {

        long num;
        boolean negative=false;
        int c = digitChars[ offset ];
        if (c == '-') {
            offset++;
            negative=true;
        }

        c = digitChars[ offset ];
        num = (c - '0');
        offset++;

        for (; offset < to; offset++) {
            c = digitChars[ offset];
            if (c != '.') {
                num = ( num * 10 ) + ( c - '0' );
            }

        }

        return negative ? num * -1 : num;
    }




    private final static long L_BILLION = 1000000000;



    public static float parseFloat( byte[] buffer, int from, int to ) {
        return (float) parseDouble( buffer, from , to );
    }


    public static double parseDouble( byte[] buffer ) {
        return parseDouble( buffer, 0, buffer.length );
    }

    public static double parseDouble( byte[] buffer, int from, int to ) {
        double value;
        boolean simple = true;
        int digitsPastPoint = 0;

        int index = from;


        if (buffer[index] == '-') {
            index++;
        }

        boolean foundDot = false;
        for (;index<to; index++)  {
            int ch = buffer[ index ];
            if ( CharScanner.isNumberDigit(ch) ) {

                if (foundDot==true) {
                    digitsPastPoint++;
                }
            } else if ( ch == '.' ) {
                foundDot = true;
            }
            else if (ch == 'E' || ch == 'e' || ch == '-' || ch == '+') {
                simple = false;
            } else {
                Exceptions.die("unexpected character " + ch);
            }
        }


        if ( digitsPastPoint >= powersOf10.length-1 ) {
            simple = false;
        }


        final int length = index -from;

        if (!foundDot && simple) {
            if ( isInteger( buffer, from, length ) ) {
                value = parseIntFromTo( buffer, from, index );
            } else {
                value = parseLongFromTo( buffer, from, index );
            }
        }
        else if ( foundDot && simple ) {
            long lvalue;


            if ( length < powersOf10.length ) {

                if ( isInteger( buffer, from, length ) ) {
                    lvalue = parseIntFromToIgnoreDot( buffer, from, index );
                } else {
                    lvalue = parseLongFromToIgnoreDot( buffer, from, index );
                }

                double power = powersOf10[ digitsPastPoint ];
                value = lvalue / power;

            } else {
                value =  Double.parseDouble( new String( buffer, from, length ) );

            }


        } else {
            value =  Double.parseDouble( new String( buffer, from, index - from ) );
        }



        return value;
    }

    private static double powersOf10[] = {
            1.0,
            10.0,
            100.0,
            1_000.0,
            10_000.0,
            100_000.0,
            1_000_000.0,
            10_000_000.0,
            100_000_000.0,
            1_000_000_000.0,
            10_000_000_000.0,
            100_000_000_000.0,
            1_000_000_000_000.0,
            10_000_000_000_000.0,
            100_000_000_000_000.0,
            1_000_000_000_000_000.0,
            10_000_000_000_000_000.0,
            100_000_000_000_000_000.0,
            1_000_000_000_000_000_000.0,

    };


    public static double simpleDouble( byte[] buffer, boolean simple,  int digitsPastPoint, int startIndex, int endIndex ) {

        double sign;



        if ( simple ) {
            long value;
            final int length = endIndex - startIndex;

            if ( isInteger( buffer, startIndex, length ) ) {
                value = parseIntIgnoreDot( buffer, startIndex, length );
            } else {
                value = parseLongIgnoreDot( buffer, startIndex, length );
            }
            if ( digitsPastPoint < powersOf10.length ) {
                double power = powersOf10[ digitsPastPoint ];
                return value / power;

            }


        }

        return Double.parseDouble( new String( buffer, startIndex, ( endIndex - startIndex ) ) );
    }


    public static int skipWhiteSpace( byte [] array, int index ) {
        int c;
        for (; index< array.length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index;
    }


    public static int skipWhiteSpace( byte [] array, int index, final int length ) {
        int c;
        for (; index< length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index;
    }

    public static byte[] readNumber( byte[] array, int idx ) {
        final int startIndex = idx;

        while (true) {
            if ( !CharScanner.isDecimalDigit ( array[idx] )) {
                break;
            } else {
                idx++;
                if (idx  >= array.length) break;
            }
        }

        return  Arrays.copyOfRange(array, startIndex, idx);


    }



    public static byte[] readNumber( byte[] array, int idx, final int len ) {
        final int startIndex = idx;

        while (true) {
            if ( !CharScanner.isDecimalDigit ( array[idx] )) {
                break;
            } else {
                idx++;
                if (idx  >= len ) break;
            }
        }

        return  Arrays.copyOfRange ( array, startIndex, idx );


    }








    public static int  skipWhiteSpaceFast( byte [] array ) {
        int c;
        int index=0;
        for (; index< array.length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index;
    }



    public static int  skipWhiteSpaceFast( byte [] array, int index ) {
        int c;
        for (; index< array.length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index-1;
    }

    /**
     * Turns a single nibble into an ascii HEX digit.
     *
     * @param nibble the nibble to serializeObject.
     * @return the encoded nibble (1/2 byte).
     */
    protected static int encodeNibbleToHexAsciiCharByte( final int nibble ) {

        switch ( nibble ) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
                return nibble + 0x30; // 0x30('0') - 0x39('9')
            case 0x0A:
            case 0x0B:
            case 0x0C:
            case 0x0D:
            case 0x0E:
            case 0x0F:
                return nibble + 0x57; // 0x41('a') - 0x46('f')
            default:
                Exceptions.die("illegal nibble: " + nibble);
                return -1;
        }
    }

    /**
     * Turn a single bytes into two hex character representation.
     *
     * @param decoded the byte to serializeObject.
     * @param encoded the array to which each encoded nibbles are now ascii hex representations.
     */
    public static void encodeByteIntoTwoAsciiCharBytes( final int decoded, final byte[] encoded ) {


        encoded[ 0 ] = ( byte ) encodeNibbleToHexAsciiCharByte( ( decoded >> 4 ) & 0x0F );
        encoded[ 1 ] = ( byte ) encodeNibbleToHexAsciiCharByte(  decoded & 0x0F  );
    }


    public static String errorDetails( String message, byte[] array, int index, int ch ) {
        CharBuf buf = CharBuf.create( 255 );

        buf.addLine( message );


        buf.addLine( "" );
        buf.addLine( "The current character read is " + CharScanner.debugCharDescription(ch) );


        buf.addLine( message );

        int line = 0;
        int lastLineIndex = 0;

        for ( int i = 0; i < index && i < array.length; i++ ) {
            if ( array[ i ] == '\n' ) {
                line++;
                lastLineIndex = i + 1;
            }
        }

        int count = 0;

        for ( int i = lastLineIndex; i < array.length; i++, count++ ) {
            if ( array[ i ] == '\n' ) {
                break;
            }
        }


        buf.addLine( "line number " + (line + 1) );
        buf.addLine( "index number " + index );


        try {
            buf.addLine( new String( array, lastLineIndex, count ) );
        } catch ( Exception ex ) {

            try {
                int start =  index = ( index - 10 < 0 ) ? 0 : index - 10;

                buf.addLine( new String( array, start, index ) );
            } catch ( Exception ex2 ) {
                buf.addLine( new String( array, 0, array.length ) );
            }
        }
        for ( int i = 0; i < ( index - lastLineIndex ); i++ ) {
            buf.add( '.' );
        }
        buf.add( '^' );

        return buf.toString();
    }



    public  static boolean hasEscapeChar (byte []array, int index, int[] indexHolder) {
        int currentChar;
        for ( ; index < array.length; index++ ) {
            currentChar = array[index];
            if ( CharScanner.isDoubleQuote(currentChar)) {
                indexHolder[0] = index;
                return false;
            } else if ( CharScanner.isEscape(currentChar) ) {
                indexHolder[0] = index;
                return  true;
            }

        }

        indexHolder[0] = index;
        return false;
    }



    public static int findEndQuote (final byte[] array,  int index) {
        int currentChar;
        boolean escape = false;

        for ( ; index < array.length; index++ ) {
            currentChar = array[index];
            if ( CharScanner.isDoubleQuote(currentChar)) {
                if (!escape) {
                    break;
                }
            }
            if ( CharScanner.isEscape(currentChar) ) {
                if (!escape) {
                    escape = true;
                } else {
                    escape = false;
                }
            } else {
                escape = false;
            }
        }
        return index;
    }



    public static int findEndQuoteUTF8 (final byte[] array,  int index) {
        int currentChar;
        boolean escape = false;

        for ( ; index < array.length; index++ ) {
            currentChar = array[index];
            if (currentChar>=0)  {
                if ( CharScanner.isDoubleQuote(currentChar)) {
                    if (!escape) {
                        break;
                    }
                }
                if ( CharScanner.isEscape(currentChar) ) {
                    if (!escape) {
                        escape = true;
                    } else {
                        escape = false;
                    }
                } else {
                    escape = false;
                }
            } else {
                index = skipUTF8NonCharOrLongChar(currentChar, index);
            }
        }
        return index;
    }




    private static int skipUTF8NonCharOrLongChar(final int c, int index) {


        if ( ( c >> 5 ) == -2 ) {
            index++;
        } else if ( ( c >> 4 ) == -2 ) {
            index+=2;
        } else if ( ( c >> 3 ) == -2 ) {
            index+=3;
        }

        return index;
    }


    public  static boolean hasEscapeCharUTF8 (byte []array, int index, int[] indexHolder) {
        int currentChar;
        for ( ; index < array.length; index++ ) {
            currentChar = array[index];
            if (currentChar>=0)  {

                if ( CharScanner.isDoubleQuote(currentChar)) {
                indexHolder[0] = index;
                return false;
            } else if ( CharScanner.isEscape(currentChar) ) {
                indexHolder[0] = index;
                return  true;
            }
            } else {
                index = skipUTF8NonCharOrLongChar(currentChar, index);

            }

        }

        indexHolder[0] = index;
        return false;
    }


}
