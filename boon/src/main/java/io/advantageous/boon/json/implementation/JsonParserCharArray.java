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

package io.advantageous.boon.json.implementation;

import io.advantageous.boon.collections.LazyMap;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.JsonException;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.primitive.Chr;
import io.advantageous.boon.primitive.CharBuf;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Converts an input JSON String into Java objects works with String or char array
 * as input. Produces an Object which can be any of the basic JSON types mapped
 * to Java.
 */
public class JsonParserCharArray extends BaseJsonParser  {

    protected char[] charArray;
    protected int __index;
    protected char __currentChar;





    protected int lastIndex;

    @Override
    public  Object parse ( final char[] chars ) {
        __index = 0;
        charArray = chars;
        lastIndex = chars.length -1;
        Object value = decodeValue();
        return value;
    }



    public Object parse ( byte[] bytes, Charset charset ) {
        final char[] chars = FastStringUtils.toCharArrayFromBytes(bytes, charset);
        return parse( chars );
    }

    protected final boolean hasMore() {
        return __index  < lastIndex;
    }


    protected final boolean hasCurrent() {
        return __index  <= lastIndex;
    }



    protected final void skipWhiteSpaceIfNeeded() {
        int ix = __index;


        if (hasCurrent ()) {
            this.__currentChar = this.charArray[ix];
        }

        if (__currentChar <= 32) {
            ix = CharScanner.skipWhiteSpaceFast(this.charArray, ix);
            this.__currentChar = this.charArray[ix];
            __index = ix;
        }



    }

    protected final char nextChar() {

        try {
            if ( hasMore() ) {
                __index++;
                return __currentChar = charArray[ __index ];
            } else {
                return '\u0000';
            }
        } catch ( Exception ex ) {
            throw new JsonException( exceptionDetails( "unable to advance character" ), ex );
        }
    }


    protected String exceptionDetails( String message ) {
        return CharScanner.errorDetails ( message, charArray, __index, __currentChar );
    }





    protected final Object decodeJsonObject() {


        if ( __currentChar == '{' )  {
            __index++;
        }

        LazyMap map = new LazyMap ();

        for (; __index < this.charArray.length; __index++ ) {

            skipWhiteSpaceIfNeeded ();


            if ( __currentChar == '"' ) {

                String key =
                        decodeString();

                if ( internKeys ) {
                    String keyPrime = internedKeysCache.get( key );
                    if ( keyPrime == null ) {
                        key = key.intern();
                        internedKeysCache.put( key, key );
                    } else {
                        key = keyPrime;
                    }
                }

                skipWhiteSpaceIfNeeded ();

                if ( __currentChar != ':' ) {

                    complain( "expecting current character to be " + charDescription( __currentChar ) + "\n" );
                }
                __index++;

                skipWhiteSpaceIfNeeded ();

                Object value = decodeValueInternal();

                skipWhiteSpaceIfNeeded ();
                map.put( key, value );


            }
            if ( __currentChar == '}' ) {
                __index++;
                break;
            } else if ( __currentChar == ',' ) {
                continue;
            } else {
                complain(
                        "expecting '}' or ',' but got current char " + charDescription( __currentChar ) );

            }
        }


        return map;
    }


    protected final void complain( String complaint ) {
        throw new JsonException( exceptionDetails( complaint ) );
    }


    protected Object decodeValue() {
        return decodeValueInternal();
    }

    private final Object decodeValueInternal() {
        Object value = null;
        skipWhiteSpaceIfNeeded ();

        switch ( __currentChar ) {

            case '"':
                value = decodeString();
                break;


            case 't':
                value = decodeTrue();
                break;

            case 'f':
                value = decodeFalse();
                break;

            case 'n':
                value = decodeNull();
                break;

            case '[':
                value = decodeJsonArray();
                break;




            case '{':
                value = decodeJsonObject();
                break;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                value = decodeNumber();
                break;
            case '-':
                value = decodeNumber();
                break;

            default:
                throw new JsonException( exceptionDetails( "Unable to determine the " +
                        "current character, it is not a string, number, array, or object" ) );

        }

        return value;
    }



    int[] endIndex = new int[1];

    private final Object decodeNumber() {

        Number num =  CharScanner.parseJsonNumber( charArray, __index, charArray.length, endIndex );
        __index = endIndex[0];

        return num;
    }




    protected static final char[] NULL = Chr.chars("null");

    protected final Object decodeNull() {

        if ( __index + NULL.length <= charArray.length ) {
            if ( charArray[ __index ] == 'n' &&
                    charArray[ ++__index ] == 'u' &&
                    charArray[ ++__index ] == 'l' &&
                    charArray[ ++__index ] == 'l' ) {
                __index++;
                return null;
            }
        }
        throw new JsonException( exceptionDetails( "null not parse properly" ) );
    }


    protected static final char[] TRUE = Chr.chars( "true" );

    protected final boolean decodeTrue() {

        if ( __index + TRUE.length <= charArray.length ) {
            if ( charArray[ __index ] == 't' &&
                    charArray[ ++__index ] == 'r' &&
                    charArray[ ++__index ] == 'u' &&
                    charArray[ ++__index ] == 'e' ) {

                __index++;
                return true;

            }
        }

        throw new JsonException( exceptionDetails( "true not parsed properly" ) );
    }


    protected static char[] FALSE = Chr.chars( "false" );

    protected final boolean decodeFalse() {

        if ( __index + FALSE.length <= charArray.length ) {
            if ( charArray[ __index ] == 'f' &&
                    charArray[ ++__index ] == 'a' &&
                    charArray[ ++__index ] == 'l' &&
                    charArray[ ++__index ] == 's' &&
                    charArray[ ++__index ] == 'e' ) {
                __index++;
                return false;
            }
        }
        throw new JsonException( exceptionDetails( "false not parsed properly" ) );
    }



    private CharBuf builder = CharBuf.create( 20 );
//
//    private String decodeString() {
//
//
//        char[] array = charArray;
//        int index = __index;
//        char currentChar = charArray[index];
//
//        if (index < array.length && currentChar == '"') {
//            index++;
//        }
//
//        final int startIndex = index;
//
//
//        boolean escape = false;
//
//        boolean hasEscaped = false;
//
//
//        while (true) {
//            currentChar = array[index];
//            if (isDoubleQuote(currentChar)) {
//                if (!escape) {
//                    break;
//                }
//            }
//            if (isEscape(currentChar)) {
//                hasEscaped = true;
//                escape = true;
//            } else {
//                escape = false;
//            }
//            index++;
//            if (index >= array.length) break;
//        }
//
//
//        String value = null;
//        if (hasEscaped) {
//            value = JsonStringDecoder.decodeForSure(array, startIndex, index);
//        } else {
//            value = new String(array, startIndex, (index - startIndex));
//        }
//
//        if (index < charArray.length) {
//            index++;
//        }
//        __index = index;
//        return value;
//
//    }


    private String decodeString() {

        char[] array = charArray;
        int index = __index;
        char currentChar = array[index];

        if ( index < array.length && currentChar == '"' ) {
            index++;
        }

        final int startIndex = index;


        boolean encoded = CharScanner.hasEscapeChar ( array, index, indexHolder );
        index = indexHolder[0];



        String value = null;
        if ( encoded ) {
            index = CharScanner.findEndQuote ( array,  index);
            value = builder.decodeJsonString ( array, startIndex, index ).toString ();
            builder.recycle ();
        } else {
            value = new String( array, startIndex, ( index - startIndex ) );
        }

        if ( index < charArray.length ) {
            index++;
        }
        __index = index;
        return value;
    }

    protected final List decodeJsonArray() {


        ArrayList<Object> list = null;

        boolean foundEnd = false;
        char [] charArray = this.charArray;

        try {
        if ( __currentChar == '[' ) {
            __index++;
        }


        skipWhiteSpaceIfNeeded ();


        /* the list might be empty  */
        if ( __currentChar == ']' ) {
            __index++;
            return Collections.EMPTY_LIST;
        }

        list = new ArrayList();


        char c;

        loop:
        while ( this.hasMore() ) {
 
            Object arrayItem = decodeValueInternal();

            list.add( arrayItem );


            while (true) {
                c  =  charArray[__index];
                if ( c == ',' ) {
                    __index++;
                    continue loop;
                } else if ( c == ']' ) {
                    foundEnd = true;
                    __index++;
                    break loop;
                } else if (c <= 32) {
                     __index++;
                    continue;
                } else {
                    break;
                }

            }



            c  =  charArray[__index];

            if ( c == ',' ) {
                __index++;
                continue;
            } else if ( c == ']' ) {
                __index++;
                foundEnd = true;
                break;
            } else {

                String charString = charDescription( c );

                complain(
                        String.format( "expecting a ',' or a ']', " +
                                " but got \nthe current character of  %s " +
                                " on array index of %s \n", charString, list.size() )
                );

            }
        }

        }catch ( Exception ex ) {
            if (ex instanceof JsonException) {
                JsonException jsonException = (JsonException) ex;
                throw jsonException;
            }
            throw new JsonException ( exceptionDetails("issue parsing JSON array"), ex );
        }
        if (!foundEnd ) {
            complain ( "Did not find end of Json Array" );
        }
        return list;

    }

    protected final char currentChar() {
        if (__index > lastIndex) {
            return 0;
        }
        else {
            return charArray[__index];
        }
    }

}