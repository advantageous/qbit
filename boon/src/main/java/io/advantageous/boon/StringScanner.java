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


public class StringScanner {


    private static final char[] WHITE_SPACE = new char[] {'\n', '\t', ' ', '\r'};

    /**
     * Checks the input string to see if it is only digits
     * @param input digits
     * @return true or false
     */
    public static boolean isDigits( String input ) {
        return CharScanner.isDigits(FastStringUtils.toCharArray(input));
    }


    /**
     * Splits a string into many parts
     * @param string input string
     * @param split char to split by
     * @param limit the limit of the times you want it split up
     * @return the split string
     */
    public static String[]  split( final String string,
                                  final char split, final int limit ) {


        char[][] comps = CharScanner.split( FastStringUtils.toCharArray( string ), split, limit );

        return Str.fromCharArrayOfArrayToStringArray( comps );


    }

    /**
     * Splits a string
     * @param string string to split
     * @param split what you want to split it by
     * @return the split up string
     */
    public static String[] split( final String string,
                                  final char split ) {

        char[][] comps = CharScanner.split( FastStringUtils.toCharArray( string ), split );

        return Str.fromCharArrayOfArrayToStringArray( comps );

    }

    /**
     * Split string by a list of delimiters
     * @param string string to split
     * @param delimiters delimeters to split it by
     * @return the split up string
     */
    public static String[] splitByChars( final String string,
                                         final char... delimiters ) {

        char[][] comps = CharScanner.splitByChars( FastStringUtils.toCharArray( string ), delimiters );

        return Str.fromCharArrayOfArrayToStringArray( comps );

    }

    public static String[] splitByCharsFromToDelims( final String string, int from, int to,
                                         final char... delimiters ) {

        char[][] comps = CharScanner.splitByCharsFromToDelims( FastStringUtils.toCharArray( string ), from, to, delimiters );

        return Str.fromCharArrayOfArrayToStringArray( comps );

    }

    public static String[] splitByCharsFrom( final String string, int from,
                                                     final char... delimiters ) {

        char[][] comps = CharScanner.splitByCharsFromToDelims( FastStringUtils.toCharArray( string ), from, string.length(), delimiters );

        return Str.fromCharArrayOfArrayToStringArray( comps );

    }

    /**
     * Split string by white space
     * @param string string to split
     * @return the split up string
     */
    public static String[] splitByWhiteSpace( final String string
                                         ) {

        char[][] comps = CharScanner.splitByChars( FastStringUtils.toCharArray( string ), WHITE_SPACE );

        return Str.fromCharArrayOfArrayToStringArray( comps );

    }

    /**
     * Split string by a list of delimiters
     * @param string string to split
     * @param delimiters delimeters to split it by
     * @return the split up string
     */
    public static String[] splitByDelimiters( final String string,
                                              final String delimiters ) {

        char[][] comps = CharScanner.splitByChars( FastStringUtils.toCharArray( string ), delimiters.toCharArray() );

        return Str.fromCharArrayOfArrayToStringArray( comps );

    }



    /**
     * Split string by a list of delimiters but none are empty
     * @param string string to split
     * @param delimiters delimeters to split it by
     * @return the split up string
     */
    public static String[] splitByCharsNoneEmpty( final String string, final char... delimiters ) {

        char[][] comps = CharScanner.splitByCharsNoneEmpty( FastStringUtils.toCharArray( string ), delimiters );
        return Str.fromCharArrayOfArrayToStringArray( comps );
    }


    /**
     * remove chars from a string
     * @param string string to split
     * @param delimiters delimeters to remove
     * @return the split up string
     */
    public static String removeChars( final String string, final char... delimiters ) {
        char[][] comps = CharScanner.splitByCharsNoneEmpty( FastStringUtils.toCharArray( string ), delimiters );
        return new String(Chr.add(comps));
    }



    /**
     * Split string by a list of delimiters but none are empty within a range
     * @param string string to split
     * @param delimiters delimeters to split it by
     * @return the split up string
     */
    public static String[] splitByCharsNoneEmpty( final String string, int start, int end, final char... delimiters ) {
        Exceptions.requireNonNull( string );

        char[][] comps = CharScanner.splitByCharsNoneEmpty( FastStringUtils.toCharArray( string ), start, end, delimiters );
        return Str.fromCharArrayOfArrayToStringArray( comps );
    }

    /**
     * Parse float
     * @param buffer input buffer
     * @param from from
     * @param to to
     * @return value
     */
    public static float parseFloat( String buffer, int from, int to ) {
        return CharScanner.parseFloat( FastStringUtils.toCharArray(buffer), from , to );
    }

    /**
     * parse a float
     * @param buffer input string
     * @return value
     */
    public static float parseFloat( String buffer ) {
        return CharScanner.parseFloat( FastStringUtils.toCharArray(buffer) );
    }


    /**
     * parse a double
     * @param buffer input string
     * @return value
     */
    public static double parseDouble( String buffer, int from, int to ) {
        return CharScanner.parseDouble( FastStringUtils.toCharArray(buffer), from , to );
    }


    /**
     * parse a double
     * @param buffer input string
     * @return value
     */
    public static double parseDouble( String buffer ) {
        return CharScanner.parseDouble( FastStringUtils.toCharArray(buffer) );
    }



    /**
     * parse an int within a range
     * @param buffer input string
     * @return value
     */
    public static int parseInt( String buffer, int from, int to ) {
        return CharScanner.parseInt( FastStringUtils.toCharArray(buffer), from , to );
    }

    /**
     * parse an int within a range
     * @param buffer input string
     * @return value
     */
    public static int parseInt( String buffer ) {
        return CharScanner.parseInt( FastStringUtils.toCharArray(buffer) );
    }


    /**
     * parse an long within a range
     * @param buffer input string
     * @return value
     */
    public static long parseLong( String buffer, int from, int to ) {
        return CharScanner.parseLong( FastStringUtils.toCharArray(buffer), from , to );
    }


    /**
     * parse an long within a range
     * @param buffer input string
     * @return value
     */
    public static long parseLong( String buffer ) {
        return CharScanner.parseLong( FastStringUtils.toCharArray(buffer) );
    }



    public static short parseShort( String buffer, int from, int to ) {
        return (short) CharScanner.parseInt( FastStringUtils.toCharArray(buffer), from , to );
    }

    public static short parseShort( String buffer ) {
        return (short) CharScanner.parseInt( FastStringUtils.toCharArray(buffer) );
    }


    public static short parseByte( String buffer, int from, int to ) {
        return (byte) CharScanner.parseInt( FastStringUtils.toCharArray(buffer), from , to );
    }

    public static short parseByte( String buffer ) {
        return (byte) CharScanner.parseInt( FastStringUtils.toCharArray(buffer) );
    }


    public static int findWhiteSpace(String buffer) {
        return CharScanner.findWhiteSpace(FastStringUtils.toCharArray(buffer));
    }

    public static String substringAfter(String string, String after) {

        int index = StringScanner.findString(string, after);
        if (index==-1) {
            return "";
        } else {
            return Str.slc(string, index+after.length());
        }

    }

    private static int findString(String string, String after) {
        return CharScanner.findChars(FastStringUtils.toCharArray(after),
                FastStringUtils.toCharArray(string));
    }

    public static String substringBefore(String string, String before) {

        int index = StringScanner.findString(string, before);
        if (index==-1) {
            return "";
        } else {
            return Str.slcEnd(string, index);
        }
    }
}
