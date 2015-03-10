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

import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.primitive.Chr;
import io.advantageous.boon.primitive.CharBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static io.advantageous.boon.Exceptions.die;

public class Str {


    /**
     * Empty string
     */
    public final static String EMPTY_STRING = "";


    /**
     * gets length
     * @param str string
     * @return length
     */
    @Universal
    public static int lengthOf( String str ) {
        return len(str);
    }


    /**
     * Gets slice of a string.
     * @param str string
     * @param start start index of slice
     * @return new string
     */
    @Universal
    public static String sliceOf( String str, int start ) {
        return slc(str, start);
    }


    /**
     * Get slice of string
     * @param str string
     * @param start start index
     * @param end end index
     * @return new string
     */
    @Universal
    public static String sliceOf( String str, int start, int end ) {
        return slc(str, start, end);
    }


    /**
     * Gets end slice of a string.
     * @param str string
     * @param end end index of slice
     * @return new string
     */
    @Universal
    public static String endSliceOf( String str, int end ) {
        return slcEnd(str, end);
    }


    /**
     * Gets character at index
     * @param str string
     * @param index index
     * @return char at
     */
    @Universal
    public static char atIndex( String str, int index ) {
        return idx(str, index);
    }


    /**
     * Puts character at index
     * @param str string
     * @param index index
     * @param c char to put in
     * @return new string
     */
    @Universal
    public static String atIndex( String str, int index, char c ) {
            return idx (str, index, c);
    }


    /**
     * gets length
     * @param str string
     * @return length
     */
    @Universal
    public static int len( String str ) {
        return str.length();
    }



    /**
     * Gets slice of a string.
     * @param str string
     * @param start start index of slice
     * @return new string
     */
    @Universal
    public static String slc( String str, int start ) {

        return FastStringUtils.noCopyStringFromChars(Chr.slc(FastStringUtils.toCharArray(str), start));
    }


    /**
     * Get slice of string
     * @param str string
     * @param start start index
     * @param end end index
     * @return new string
     */
    @Universal
    public static String slc( String str, int start, int end ) {
        return FastStringUtils.noCopyStringFromChars(Chr.slc(FastStringUtils.toCharArray(str), start, end));
    }


    /**
     * Gets end slice of a string.
     * @param str string
     * @param end end index of slice
     * @return new string
     */
    @Universal
    public static String slcEnd( String str, int end ) {
        return FastStringUtils.noCopyStringFromChars( Chr.slcEnd( FastStringUtils.toCharArray(str), end ) );
    }



    /**
     * Gets character at index
     * @param str string
     * @param index index
     * @return char at
     */
    @Universal
    public static char idx( String str, int index ) {
        int i = calculateIndex( str.length(), index );

        char c = str.charAt( i );
        return c;
    }


    /**
     * Puts character at index
     * @param str string
     * @param index index
     * @param c char to put in
     * @return new string
     */
    @Universal
    public static String idx( String str, int index, char c ) {

        char[] chars = str.toCharArray();
        Chr.idx( chars, index, c );
        return new String( chars );
    }

    /**
     * See if chars is in another string
     * @param chars chars
     * @param str string
     * @return true or false
     */
    @Universal
    public static boolean in( char[] chars, String str ) {
        return Chr.in ( chars, FastStringUtils.toCharArray(str) );
    }


    /**
     * See if a char is in another string
     * @param c char
     * @param str string
     * @return true or false
     */
    @Universal
    public static boolean in( char c, String str ) {
        return Chr.in ( c, FastStringUtils.toCharArray(str) );
    }


    /**
     * See if a char is in a string
     * @param c char
     * @param offset offset
     * @param str string
     * @return true or false
     */
    @Universal
    public static boolean in( char c, int offset, String str ) {
        return Chr.in ( c, offset, FastStringUtils.toCharArray(str) );
    }


    /**
     * See if a char is in a string but in a certain bounds of string
     * @param c char
     * @param offset offset
     * @param end end of span to search
     * @param str string
     * @return true or false
     */
    @Universal
    public static boolean in( char c, int offset, int end, String str ) {
        return Chr.in ( c, offset, end, FastStringUtils.toCharArray(str) );
    }


    /**
     * Add a char to a string
     * @param str string
     * @param c char
     * @return new string
     */
    @Universal
    public static String add( String str, char c ) {
        return FastStringUtils.noCopyStringFromChars( Chr.add( FastStringUtils.toCharArray(str), c ) );
    }


    /**
     * Add one string to another
     * @param str string 1
     * @param str2 string 2
     * @return new string
     */
    @Universal
    public static String add( String str, String str2 ) {
        return FastStringUtils.noCopyStringFromChars(
                Chr.add(
                 FastStringUtils.toCharArray(str),
                 FastStringUtils.toCharArray(str2) )
        );
    }


    /**
     * Add many strings together to another
     * @param strings strings
     * @return new string
     */
    @Universal
    public static String add( String... strings ) {
        int length = 0;
        for ( String str : strings ) {
            if ( str == null ) {
                continue;
            }
            length += str.length();
        }
        CharBuf builder = CharBuf.createExact( length );
        for ( String str : strings ) {
            if ( str == null ) {
                continue;
            }
            builder.add( str );
        }
        return builder.toString();
    }



    /**
     * Add many objects converted to strings together.
     * Null are ignored so be careful.
     * @param objects objects to convert to strings
     * @return new string
     */
    public static String addObjects( Object... objects ) {
        int length = 0;
        for ( Object obj : objects ) {
            if ( obj == null ) {
                continue;
            }
            length += obj.toString().length();
        }
        CharBuf builder = CharBuf.createExact( length );
        for ( Object str : objects ) {
            if ( str == null ) {
                continue;
            }
            builder.add( str.toString() );
        }
        return builder.toString();
    }

    /**
     * Gets rid of null characters lurking in the string
     * @param str string
     * @return new string
     */
    public static String compact( String str ) {
        return FastStringUtils.noCopyStringFromChars( Chr.compact( FastStringUtils.toCharArray(str) ) );
    }


    /**
     * calculates an index for slicing.
     * @param length length of index
     * @param originalIndex original index might be negative
     * @return new index
     */
    private static int calculateIndex( final int length, int originalIndex ) {


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
     * Split a string
     * @param str string to split
     * @return string array
     */
    public static String[] split( String str ) {
        char[][] split = Chr.split( FastStringUtils.toCharArray(str) );
        return fromCharArrayOfArrayToStringArray( split );
    }


    /**
     * Split a string by lines
     * @param str string to split
     * @return string array
     */
    public static String[] splitLines( String str ) {
        char[][] split = Chr.splitLines( FastStringUtils.toCharArray(str) );
        return fromCharArrayOfArrayToStringArray( split );
    }


    /**
     * Split a string by commas
     * @param str string to split
     * @return string array
     */
    public static String[] splitComma( String str ) {
        char[][] split = Chr.splitComma( FastStringUtils.toCharArray(str) );
        return fromCharArrayOfArrayToStringArray( split );
    }


    /**
     * Split a string by space
     * @param str string to split
     * @return string array
     */
    public static String[] splitBySpace( String str ) {
        char[][] split = CharScanner.splitBySpace(FastStringUtils.toCharArray(str));
        return fromCharArrayOfArrayToStringArray( split );
    }


    /**
     * Split a string by pipe
     * @param str string to split
     * @return string array
     */
    public static String[] splitByPipe( String str ) {
        char[][] split = CharScanner.splitByPipe( FastStringUtils.toCharArray(str) );
        return fromCharArrayOfArrayToStringArray( split );
    }


    /**
     * Convert arrays of chars to arrays of strings
     * @param split array of chars
     * @return string array
     */
    public static String[] fromCharArrayOfArrayToStringArray( char[][] split ) {
        String[] results = new String[ split.length ];

        char[] array;

        for ( int index = 0; index < split.length; index++ ) {
            array = split[ index ];

            results[ index ] = array.length == 0 ?
                    EMPTY_STRING : FastStringUtils.noCopyStringFromChars( array );
        }
        return results;
    }


    /**
     * Convert to upper case
     * @param str string to convert
     * @return new string
     */
    public static String upper( String str ) {
        return str.toUpperCase();
    }


    /**
     * Convert to lower case
     * @param str string to convert
     * @return new string
     */
    public static String lower( String str ) {
        return str.toLowerCase();
    }


    /**
     * Convert to camel case upper (starts with upper case)
     * @param in string to convert
     * @return new string
     */
    public static String camelCaseUpper( String in ) {
        return camelCase( in, true );
    }


    /**
     * Convert to camel case lower (starts with lower case)
     * @param in string to convert
     * @return new string
     */
    public static String camelCaseLower( String in ) {
        return camelCase( in, false );
    }


    /**
     * Convert to camel case
     * @param in string to convert
     * @return new string
     */
    public static String camelCase( String in ) {
        return camelCase( in, false );
    }


    /**
     * Convert to camel case and pass upper or lower
     * @param inStr string to convert
     * @param upper upper flag
     * @return new string
     */
    public static String camelCase( String inStr, boolean upper ) {
        char[] in = FastStringUtils.toCharArray(inStr);
        char[] out = Chr.camelCase( in, upper );
        return FastStringUtils.noCopyStringFromChars( out );
    }


    /**
     * Checks to see if a string is inside of another
     * @param start start
     * @param inStr input string
     * @param end index at end
     * @return inside of  result
     */
    public static boolean insideOf(String start, String inStr, String end) {
        return Chr.insideOf(FastStringUtils.toCharArray(start), FastStringUtils.toCharArray(inStr), FastStringUtils.toCharArray(end));
    }

    /**
     * Convert to under bar case
     * @param inStr input string
     * @return new string
     */
    public static String underBarCase( String inStr ) {
        char[] in = FastStringUtils.toCharArray(inStr);
        char[] out = Chr.underBarCase( in );
        return FastStringUtils.noCopyStringFromChars( out );
    }


    /**
     * See if they are equal or die
     * @param a a
     * @param b b
     */
    public static void equalsOrDie(CharSequence a, CharSequence b) {
        char[] ac = FastStringUtils.toCharArray(a);
        char[] bc = FastStringUtils.toCharArray(b);
        Chr.equalsOrDie(ac, bc);
    }


    /**
     * See if they are equal or die
     * @param a a
     * @param b b
     */
    public static void equalsOrDie(String a, String b) {
        if (a == null && b == null) {
            return;
        }
        if (a == null || b == null) {
            Exceptions.die("Values not equal value a=", a, "value b=", b);
        }

        char[] ac = FastStringUtils.toCharArray(a);
        char[] bc = FastStringUtils.toCharArray(b);
        Chr.equalsOrDie(ac, bc);
    }


    public static String lpad( String inStr, int size, char fill ) {
        return new String( Chr.lpad( inStr.toCharArray(), size, fill ) );
    }


    public static String lpad( String inStr, int size ) {
        return new String( Chr.lpad( inStr.toCharArray(), size, ' ' ) );
    }

    public static String lpad( Object inStr, int size ) {
        return new String( Chr.lpad(inStr == null ? "".toCharArray() : inStr.toString().toCharArray(), size, ' ') );
    }


    public static String lpad( Object inStr) {
        return new String( Chr.lpad( inStr == null ? "".toCharArray() : inStr.toString().toCharArray(), 20, ' ' ) );
    }


    public static String zfill( int num, int size ) {
        return new String( Chr.lpad( Integer.toString( num ).toCharArray(),
                size, '0' ) );
    }


    public static String rpad( String inStr, int size, char fill ) {
        return new String( Chr.rpad( inStr.toCharArray(), size, fill ) );
    }


    public static String rpad( String inStr, int size) {
        return new String( Chr.rpad( inStr.toCharArray(), size, ' ' ) );
    }


    public static String rpad( Object obj, int size) {
        if (obj != null) {
            return new String( Chr.rpad( obj.toString().toCharArray(), size, ' ' ) );
        } else {
            return new String( Chr.rpad( "<NULL>".toCharArray(), size, ' ' ) );
        }
    }


    public static String rpad( Object obj) {
        if (obj != null) {
            return new String( Chr.rpad( obj.toString().toCharArray(), 20, ' ' ) );
        } else {
            return new String( Chr.rpad( "<NULL>".toCharArray(), 20, ' ' ) );
        }
    }

    public static String rpad( Object obj, int size, char fill ) {
        if (obj != null) {
            return new String( Chr.rpad( obj.toString().toCharArray(), size, fill ) );
        } else {
            return new String( Chr.rpad( "<NULL>".toCharArray(), size, fill ) );
        }
    }

    public static String[] split( final String input,
                                  final char split ) {
        return StringScanner.split( input, split );

    }

    @Universal
    public static boolean in( String value, String str ) {
        return str.contains( value );
    }


    public static String lines( String... lines ) {
        return join( '\n', lines );
    }


    public static String linesConvertQuotes( String... lines ) {

        for (int index=0; index < lines.length; index++) {
            lines[index]=lines[index].replace( '\'', '"' );
        }
        return join( '\n', lines );
    }


    public static String join( char delim, String... args ) {
        CharBuf builder = CharBuf.create( 10 * args.length );

        int index = 0;
        for ( String arg : args ) {
            builder.add( arg );
            if ( !( index == args.length - 1 ) ) {
                builder.add( delim );
            }
            index++;
        }
        return builder.toString();
    }

    public static String joinObjects( char delim, Object... args ) {
        CharBuf builder = CharBuf.create( 10 * args.length );

        int index = 0;
        for ( Object arg : args ) {
            builder.add( arg == null ? "null" : arg.toString() );
            if ( !( index == args.length - 1 ) ) {
                builder.add( delim );
            }
            index++;
        }
        return builder.toString();
    }



    public static String join( String... args ) {
        CharBuf builder = CharBuf.create( 10 * args.length );

        for ( String arg : args ) {
            builder.add( arg );
        }
        return builder.toString();
    }

    public static String joinCollection( char delim, List<?> args ) {
        CharBuf builder = CharBuf.create( 10 * args.size() );

        int index = 0;
        for ( Object arg : args ) {
            if ( arg == null ) {
                continue;
            }
            builder.add( arg.toString() );
            if ( !( index == args.size() - 1 ) ) {
                builder.add( delim );
            }
            index++;
        }
        return builder.toString();

    }


    public static String joinCollection( String delim, List<?> args ) {
        CharBuf builder = CharBuf.create( 10 * args.size() );

        int index = 0;
        for ( Object arg : args ) {
            if ( arg == null ) {
                continue;
            }
            builder.add( arg.toString() );
            if ( !( index == args.size() - 1 ) ) {
                builder.add( delim );
            }
            index++;
        }
        return builder.toString();

    }

    @Universal
    public static boolean isEmpty( String str ) {
        if ( str == null ) {
            return true;
        } else {
            return str.isEmpty();
        }

    }

    @Universal
    public static boolean isEmpty( Object str ) {
        if ( str == null ) {
            return true;
        } else {
            return str.toString().isEmpty();
        }

    }


    public static String uncapitalize( String string ) {
        StringBuilder rv = new StringBuilder();
        if ( string.length() > 0 ) {
            rv.append( Character.toLowerCase( string.charAt( 0 ) ) );
            if ( string.length() > 1 ) {
                rv.append( string.substring( 1 ) );
            }
        }
        return rv.toString();
    }

    public static String toString(Object object, String defaultString) {
        if (object == null) {
            return defaultString;
        } else {
            return object.toString();
        }
    }


    public static String toString(Object object) {
        if (object == null) {
            return "";
        } else {
            return object.toString();
        }
    }


    public static String str(Object str) {
        return str == null ? "<NULL>" : str.toString();
    }

    public static boolean startsWithItemInCollection(String name, Collection<String> startsWithList) {
        for (String startsWith : startsWithList) {
            if (name.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    public static String readable(String s) {
        return s.replace("\\n", "\n");
    }


    /**
     * Quote a string
     * @param s input string
     * @return new String
     */
    public static String quote(String s) {
        return add("\"", s, "\"");
    }


    /**
     * single Quote a string
     * @param s input string
     * @return new String
     */
    public static String singleQuote(String s) {
        return add("\'", s, "\'");
    }


    /**
     * double Quote a string
     * @param s input string
     * @return new String
     */
    public static String doubleQuote(String s) {
        return add("\"", s, "\"");
    }

    /** Create a string from bytes.
     * @param bytes bytes in
     * @return string out
     */
    public static String str(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);

    }

    /**
     * Do a nice pretty print of a number.
     * Add commas and such.
     * @param count number
     * @return string format of number
     */
    public static String num(Number count) {

        if (count == null) {
            return "";
        }
        if (count instanceof  Double || count instanceof BigDecimal) {
            String s = count.toString();
            if (idx(s, 1) == '.' && s.length() > 7) {
                s = slc(s, 0, 5);
                return s;
            } else {
                return s;
            }

        } else if (count instanceof Integer || count instanceof Long || count instanceof Short || count instanceof BigInteger){
            String s = count.toString();
            s = new StringBuilder(s).reverse().toString();

            CharBuf buf = CharBuf.create(s.length());

            int index = 0;
            for (char c : s.toCharArray()) {


                index++;

                buf.add(c);


                if (index % 3 == 0) {
                    buf.add(',');
                }


            }

            if (buf.lastChar() == ',') {
                buf.removeLastChar();
            }

            s = buf.toString();

            s = new StringBuilder(s).reverse().toString();

            return s;

        }

        return count.toString();


    }

}