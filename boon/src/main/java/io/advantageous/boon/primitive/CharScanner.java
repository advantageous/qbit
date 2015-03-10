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
import io.advantageous.boon.core.reflection.FastStringUtils;

import java.util.Arrays;


import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;


/**
 * This class uses int, float and double parsing that I saw in Jackson for int parsing.
 * I have mentioned this many times on my blog and to Tatu on a few occasions.
 *
 * The work is now a complete derivative, but early versions of the int parsing
 * were heavily based on the same approach used in Jackson.
 *
 * I believe at all times the usage was small enough to be covered under "fair use".
 * And now the work has been so derived and changed that it is a different beast.
 *
 */
public class CharScanner {





    protected static final int COMMA = ',';
    protected static final int CLOSED_CURLY = '}';
    protected static final int CLOSED_BRACKET = ']';

    protected static final int LETTER_E = 'e';
    protected static final int LETTER_BIG_E = 'E';


    protected static final int DECIMAL_POINT = '.';
    private static final int SPACE =  ' ';
    private static final int PIPE = '|';
    private static final char[][] EMPTY_CHAR_ARRAY_ARRAY = new char[0][0];

    public static int NEWLINE= '\n';

    public static int CARRIAGE_RETURN= '\r';



    protected static final int ALPHA_0 = '0';
    protected static final int ALPHA_1 = '1';
    protected static final int ALPHA_2 = '2';
    protected static final int ALPHA_3 = '3';
    protected static final int ALPHA_4 = '4';
    protected static final int ALPHA_5 = '5';
    protected static final int ALPHA_6 = '6';
    protected static final int ALPHA_7 = '7';
    protected static final int ALPHA_8 = '8';
    protected static final int ALPHA_9 = '9';

    protected static final int MINUS = '-';
    protected static final int PLUS = '+';



    protected static final int DOUBLE_QUOTE = '"';

    protected static final int ESCAPE = '\\';





    public static boolean isDigit( int c ) {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }


    public static boolean isDecimalDigit( int c ) {
        return isDigit ( c ) || isDecimalChar ( c );
    }



    public static boolean isDecimalChar ( int currentChar ) {
        switch ( currentChar ) {
            case MINUS:
            case PLUS:
            case LETTER_E:
            case LETTER_BIG_E:
            case DECIMAL_POINT:
                return true;
        }
        return false;

    }


    public static boolean hasDecimalChar( char[] chars, boolean negative ) {

        int index =0;

        if (negative) index++;

        for (; index < chars.length; index++) {
            switch ( chars[index] ) {
                case MINUS:
                case PLUS:
                case LETTER_E:
                case LETTER_BIG_E:
                case DECIMAL_POINT:
                    return true;
            }
        }
        return false;

    }

    public static boolean isDigits( final char[] inputArray ) {
        for ( int index = 0; index < inputArray.length; index++ ) {
            char a = inputArray[ index ];
            if ( !isDigit( a ) ) {
                return false;
            }
        }
        return true;
    }

    public static char[][] splitExact( final char[] inputArray,
                                       final char split, final int resultsArrayLength ) {
        /** Holds the results. */
        char[][] results = new char[ resultsArrayLength ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c = '\u0000';
        int index = 0;

        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {

                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                currentLineLength = 0;
                resultIndex++;
            }
        }

        if ( c != split ) {

            results[ resultIndex ] = Chr.copy(
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

    public static char[][] splitExact( final char[] inputArray,
                                       final int resultsArrayLength, char... delims ) {
        /** Holds the results. */
        char[][] results = new char[ resultsArrayLength ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c = '\u0000';
        int index = 0;
        int j;
        char split;


        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    results[ resultIndex ] = Chr.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the char

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Chr.in( c, delims ) ) {

            results[ resultIndex ] = Chr.copy(
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



    public static char[][] splitLines( final char[] inputArray) {
        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        int c = '\u0000';
        int index = 0;

        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == NEWLINE || c == CARRIAGE_RETURN ) {

                if ( resultIndex == results.length ) {

                    results = _grow( results );
                }


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength  );
                startCurrentLineIndex = index + 1; //skip the char

                currentLineLength = 0;
                resultIndex++;
            }
        }

        if ( c != NEWLINE || c != CARRIAGE_RETURN ) {

            if (resultIndex < results.length) {
                results[ resultIndex ] = Chr.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
                resultIndex++;
            }
        }

        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static char[][] split( final char[] inputArray,
                                  final char split ) {
        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c = 0;
        int index = 0;



        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {

                if ( resultIndex == results.length ) {

                    results = _grow( results );
                }


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                currentLineLength = 0;
                resultIndex++;
            }
        }



        if (c!=split) {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[resultIndex] = Chr.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1);
            resultIndex++;
        } else if (index == inputArray.length) {
            //noop
        } else {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[ resultIndex ] = Chr.copy(
                    inputArray, startCurrentLineIndex, inputArray.length - index - 1);
            resultIndex++;

        }

        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }


    public static char[][] splitFrom( final char[] inputArray,
                                  final char split, int from ) {
        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        int index = from;
        int resultIndex = 0;
        int startCurrentLineIndex = from;
        int currentLineLength = 1;


        char c = 0;



        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {

                if ( resultIndex == results.length ) {

                    results = _grow( results );
                }


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                currentLineLength = 0;
                resultIndex++;
            }
        }



        if (c!=split) {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[resultIndex] = Chr.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1);
            resultIndex++;
        } else if (index == inputArray.length) {
            //noop
        } else {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[ resultIndex ] = Chr.copy(
                    inputArray, startCurrentLineIndex, inputArray.length - index - 1);
            resultIndex++;

        }

        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static char[][] split( final char[] inputArray,
                                  final char split, final int limit ) {


        if (inputArray.length==0) {
            return EMPTY_CHAR_ARRAY_ARRAY;
        }
        /** Holds the results. */
        char[][] results = new char[ limit + 1 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c=0;
        int index = 0;


        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                resultIndex++;

                if ( resultIndex >= limit ) {
                    break;
                }




                currentLineLength = 0;

            }
        }




        if (c!=split) {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[resultIndex] = Chr.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1);
            resultIndex++;
        } else if (index == inputArray.length) {
            //noop
        } else {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[ resultIndex ] = Chr.copy(
                    inputArray, startCurrentLineIndex, inputArray.length - index - 1);
            resultIndex++;

        }

        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }

    public static char[][] splitFromStartWithLimit( final char[] inputArray,
                                  final char split, final int start,
                                  final int limit ) {


        if (inputArray.length==0) {
            return EMPTY_CHAR_ARRAY_ARRAY;
        }
        /** Holds the results. */
        char[][] results = new char[ limit + 1 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c=0;
        int index = start;


        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == split ) {


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                resultIndex++;

                if ( resultIndex >= limit ) {
                    break;
                }




                currentLineLength = 0;

            }
        }




        if (c!=split) {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[resultIndex] = Chr.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1);
            resultIndex++;
        } else if (index == inputArray.length) {
            //noop
        } else {

            if ( resultIndex == results.length ) {

                results = _grow( results );
            }
            results[ resultIndex ] = Chr.copy(
                    inputArray, startCurrentLineIndex, inputArray.length - index - 1);
            resultIndex++;

        }

        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }


    public static char[][] splitByChars( final char[] inputArray,
                                         final char... delims ) {
        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c = '\u0000';
        int index = 0;
        int j;
        char split;


        for (; index < inputArray.length; index++, currentLineLength++ ) {

            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    if ( resultIndex == results.length ) {

                        results = _grow( results );
                    }


                    results[ resultIndex ] = Chr.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the char

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Chr.in( c, delims ) ) {

            results[ resultIndex ] = Chr.copy(
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

    public static char[][] splitByCharsFromToDelims( final char[] inputArray, int from, int to,
                                                     final char... delims ) {
        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        final int length = to - from;

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c = '\u0000';
        int index = from;
        int j;
        char split;


        for (; index < length; index++, currentLineLength++ ) {

            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    if ( resultIndex == results.length ) {

                        results = _grow( results );
                    }


                    results[ resultIndex ] = Chr.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the char

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Chr.in( c, delims ) ) {

            results[ resultIndex ] = Chr.copy(
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

    public static char[][] splitByCharsNoneEmpty( final char[] inputArray,
                                                  final char... delims ) {

        final char[][] results = splitByChars( inputArray, delims );
        return compact( results );
    }


    public static char[][] splitByCharsNoneEmpty( final char[] inputArray, int from, int to,
                                                  final char... delims ) {

        final char[][] results = splitByCharsFromToDelims( inputArray, from, to, delims );
        return compact( results );
    }

    public static char[][] compact( char[][] array ) {

        int nullCount = 0;
        for ( char[] ch : array ) {

            if ( ch == null || ch.length == 0 ) {
                nullCount++;
            }
        }
        char[][] newArray = new char[ array.length - nullCount ][];

        int j = 0;
        for ( char[] ch : array ) {

            if ( ch == null || ch.length == 0 ) {
                continue;
            }

            newArray[ j ] = ch;
            j++;
        }
        return newArray;
    }


    private static char[][] _grow( char[][] array ) {

        char[][] newArray = new char[ array.length * 2 ][];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }

    private static char[][] __shrink( char[][] array, int size ) {
        char[][] newArray = new char[ array.length - size ][];

        System.arraycopy( array, 0, ( char[][] ) newArray, 0, array.length - size );
        return newArray;
    }


    final static String MIN_LONG_STR_NO_SIGN = String.valueOf( Long.MIN_VALUE );
    final static String MAX_LONG_STR = String.valueOf( Long.MAX_VALUE );


    final static String MIN_INT_STR_NO_SIGN = String.valueOf( Integer.MIN_VALUE );
    final static String MAX_INT_STR = String.valueOf( Integer.MAX_VALUE );





    public static boolean isLong( char[] digitChars ) {
        return isLong ( digitChars, 0, digitChars.length );
    }

    public static boolean isLong( char[] digitChars, int offset, int len
                                   ) {
        String cmpStr = digitChars[offset]=='-' ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
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




    public static boolean isInteger( char[] digitChars ) {
           return isInteger ( digitChars, 0, digitChars.length );
    }

    public static boolean isInteger( char[] digitChars, int offset, int len) {


        String cmpStr = (digitChars[offset] == '-') ? MIN_INT_STR_NO_SIGN : MAX_INT_STR;
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

    public static int parseInt( char[] digitChars ) {
        return parseInt(digitChars, 0, digitChars.length);
    }




    public static int parseInt(char[] digitChars, int offset, int to) {

        try {


            int num;
            boolean negative=false;
            char c = digitChars[ offset ];
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


    public static int parseIntFromToIgnoreDot( char[] digitChars, int offset, int to ) {

            int num;
            boolean negative=false;
            char c = digitChars[ offset ];
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


    public static long parseLongFromToIgnoreDot( char[] digitChars, int offset, int to ) {

        long num;
        boolean negative=false;
        char c = digitChars[ offset ];
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

    public static long parseLong(char[] digitChars, int offset, int to) {


        long num;
        boolean negative=false;
        char c = digitChars[ offset ];
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


    public static long parseLong( char[] digitChars ) {
        return parseLong(digitChars, 0, digitChars.length);
    }



    public static Number parseJsonNumber( char[] buffer ) {
        return parseJsonNumber( buffer, 0, buffer.length );
    }



    public static Number parseJsonNumber( char[] buffer, int from, int to ) {
        return parseJsonNumber( buffer, from, to, null );
    }



    public static final boolean isNumberDigit (int c)  {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }



    public static boolean isDelimiter ( int c ) {

        return c == COMMA || c == CLOSED_CURLY || c == CLOSED_BRACKET;
    }

    public static Number parseJsonNumber( char[] buffer, int from, int max, int size[] ) {
        Number value = null;
        boolean simple = true;
        int digitsPastPoint = 0;

        int index = from;


        if (buffer[index] == '-') {
            index++;
        }

        boolean foundDot = false;
        for (;index<max; index++)  {
            char ch = buffer[ index ];
            if ( isNumberDigit(ch) ) {

                if (foundDot==true) {
                    digitsPastPoint++;
                }
            } else if ( ch <= 32 || isDelimiter( ch ) ) { break;}
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
                value = parseInt(buffer, from, index);
            } else {
                value = parseLong(buffer, from, index);
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


    public static float parseFloat( char[] buffer, int from, int to ) {
        return (float) parseDouble( buffer, from , to );
    }

    public static float parseFloat( char[] buffer ) {
        return (float) parseDouble( buffer, 0 , buffer.length );
    }


    public static double parseDouble( char[] buffer ) {
        return parseDouble( buffer, 0, buffer.length );
    }

    public static double parseDouble( char[] buffer, int from, int to ) {
        double value;
        boolean simple = true;
        int digitsPastPoint = 0;

        int index = from;
        boolean negative=false;



        if (buffer[index] == '-') {
            index++;
            negative=true;
        }

        boolean foundDot = false;
        for (;index<to; index++)  {
            char ch = buffer[ index ];
            if ( isNumberDigit(ch) ) {

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
                value = parseInt(buffer, from, index);
            } else {
                value = parseLong(buffer, from, index);
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



        if (value==0.0 && negative) {
            return -0.0;
        } else {
            return value;
        }
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


    public static int skipWhiteSpace( char [] array) {
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

    public static int skipWhiteSpace( char [] array, int index ) {
        int c;
        for (; index< array.length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index;
    }




    public static int skipWhiteSpace( char [] array, int index, final int length ) {
        int c;
        for (; index< length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index;
    }

    public static char[] readNumber( char[] array, int idx ) {
        final int startIndex = idx;

        while (true) {
            if ( !CharScanner.isDecimalDigit ( array[idx] )) {
                break;
            } else {
                idx++;
                if (idx  >= array.length) break;
            }
        }

        return  Arrays.copyOfRange ( array, startIndex, idx );


    }



    public static char[] readNumber( char[] array, int idx, final int len ) {
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








    public static int  skipWhiteSpaceFast( char [] array ) {
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



    public static int  skipWhiteSpaceFast( char [] array, int index ) {
        char c;
        for (; index< array.length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index-1;
    }

    public static String errorDetails( String message, char[] array, int index, int ch ) {
        CharBuf buf = CharBuf.create( 255 );

        buf.addLine( message );


        buf.addLine( "" );
        buf.addLine( "The current character read is " + debugCharDescription( ch ) );


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


    public static String debugCharDescription( int c ) {
        String charString;
        if ( c == ' ' ) {
            charString = "[SPACE]";
        } else if ( c == '\t' ) {
            charString = "[TAB]";

        } else if ( c == '\n' ) {
            charString = "[NEWLINE]";

        } else {
            charString = "'" + (char)c + "'";
        }

        charString = charString + " with an int value of " + ( ( int ) c );
        return charString;
    }



    public static final boolean isDoubleQuote ( int c ) {
        return c == DOUBLE_QUOTE;
    }


    public static final boolean isEscape ( int c ) {
        return c == ESCAPE;
    }



    public  static boolean hasEscapeChar (char []array, int index, int[] indexHolder) {
        char currentChar;
        for ( ; index < array.length; index++ ) {
            currentChar = array[index];
            if ( isDoubleQuote ( currentChar )) {
                indexHolder[0] = index;
                return false;
            } else if ( isEscape (currentChar) ) {
                indexHolder[0] = index;
                return  true;
            }

        }

        indexHolder[0] = index;
        return false;
    }




    public static int findEndQuote (final char[] array,  int index) {
        char currentChar;
        boolean escape = false;

        for ( ; index < array.length; index++ ) {
            currentChar = array[index];
            if ( isDoubleQuote (currentChar )) {
                if (!escape) {
                    break;
                }
            }
            if ( isEscape (currentChar) ) {
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


    public static char[][] splitComma(char[] inputArray) {

        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        int c = '\u0000';
        int index = 0;

        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == COMMA ) {

                if ( resultIndex == results.length ) {

                    results = _grow( results );
                }


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                currentLineLength = 0;
                resultIndex++;
            }
        }

        if ( c != COMMA  ) {

            results[ resultIndex ] = Chr.copy(
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


    public static char[][] splitBySpace( char[] inputArray) {
        return splitByChar(SPACE, inputArray);
    }

    public static char[][] splitByChar(int splitChar, char[] inputArray) {

        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;

        final int SPLIT_CHAR = splitChar;

        int c = '\u0000';
        int index = 0;

        for (; index < inputArray.length; index++, currentLineLength++ ) {
            c = inputArray[ index ];
            if ( c == SPLIT_CHAR ) {

                if ( resultIndex == results.length ) {

                    results = _grow( results );
                }


                results[ resultIndex ] = Chr.copy(
                        inputArray, startCurrentLineIndex, currentLineLength - 1 );
                startCurrentLineIndex = index + 1; //skip the char

                currentLineLength = 0;
                resultIndex++;
            }
        }

        if ( c != SPLIT_CHAR  ) {

            results[ resultIndex ] = Chr.copy(
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

    public static int findChar(char c, char[] line) {
        int idx = -1;
        for ( int index = 0;  index < line.length; index++) {
            if ( line[index] == c ) {idx = index; break;}
        }
        return idx;
    }

    public static int findWhiteSpace(char[] line) {
        int idx = -1;
        for ( int index = 0;  index < line.length; index++) {

            char c = line[index];

            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    return index;

            }
        }
        return idx;
    }


    public static int findWhiteSpace( int start, char[] line) {
        int idx = -1;
        for ( int index = start;  index < line.length; index++) {

            char c = line[index];

            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    return index;

            }
        }
        return idx;
    }


    public static int findChar(char c, int startIndex, char[] line) {
        int idx = -1;
        for ( int index = startIndex;  index < line.length; index++) {
            if ( line[index] == c ) {idx = index; break;}
        }
        return idx;
    }


    public static int findString(String matchString, int startIndex, char[] line) {
        return findChars(FastStringUtils.toCharArray(matchString), startIndex, line);
    }

    public static int findString(String matchString, char[] line) {
        return findChars(FastStringUtils.toCharArray(matchString), 0, line);
    }


    public static int findChars(char[] matchChars, char[] line) {
        return findChars(matchChars, 0, line);
    }

    public static boolean matchChars(char[] matchChars, int startIndex, char[] line) {

        if ((line.length - startIndex) < matchChars.length) {
            return false;
        }

        int i = findChar(matchChars[0], startIndex, line);

        if (i == -1) {
            return false;
        }



        loop:
        for (; i < line.length; i++) {
            for (int j = 0; j < matchChars.length; j++) {
                char c = matchChars[j];
                if (c == line[i]) {
                    if (j+1 == matchChars.length) {
                        return true;
                    }
                    i++;
                    continue;
                } else {
                    i++;
                    break loop;
                }
            }
        }

        return false;
    }

    public static int findChars(char[] matchChars, int startIndex, char[] line) {

        if ((line.length - startIndex) < matchChars.length) {
            return -1;
        }

        int index = findChar(matchChars[0], startIndex, line);

        if (index == -1) {
            return -1;
        }


        int i= index;

        loop:
        for (; i < line.length; i++) {
            for (int j = 0; j < matchChars.length; j++) {
                char c = matchChars[j];
                if (c == line[i]) {

                    if (j+1 == matchChars.length) {
                        return index;
                    }
                    i++;
                    continue;
                } else {
                    i++;
                    break loop;
                }
            }
        }

        return findChars(matchChars, i, line);
    }

    public static char[][] splitByPipe(char[] inputArray) {
        return splitByChar(PIPE, inputArray);

    }
}
