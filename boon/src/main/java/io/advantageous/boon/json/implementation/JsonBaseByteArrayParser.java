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

import io.advantageous.boon.IO;
import io.advantageous.boon.collections.LazyMap;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.json.JsonException;
import io.advantageous.boon.primitive.InMemoryInputStream;
import io.advantageous.boon.primitive.Byt;
import io.advantageous.boon.primitive.ByteScanner;
import io.advantageous.boon.primitive.CharBuf;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static io.advantageous.boon.primitive.ByteScanner.skipWhiteSpaceFast;

/**
 * Created by rick on 12/15/13.
 */
public abstract class JsonBaseByteArrayParser extends BaseJsonParser {

    protected byte[] charArray;
    protected int __index;
    protected int __currentChar;


    protected static final int NEW_LINE = '\n';

    protected static final int RETURN = '\r';

    protected static final int SPACE = ' ';


    protected static final int TAB = '\t';
    protected static final int BELL = '\b';
    protected static final int FORM_FEED = '\f';

    protected static final int COLON = ':';

    protected static final int OPEN_CURLY = '{';


    protected static final int OPEN_BRACKET = '[';




    protected static final int LETTER_N = 'n';


    protected static final int LETTER_U = 'u';

    protected static final int LETTER_L = 'l';

    protected static final int LETTER_T = 't';

    protected static final int LETTER_R = 'r';


    protected static final int LETTER_F = 'f';

    protected static final int LETTER_A = 'a';


    protected static final int LETTER_S = 's';


    protected final CharBuf builder = CharBuf.create( 20 );


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
            ix = skipWhiteSpaceFast ( this.charArray, ix );
            this.__currentChar = this.charArray[ix];
            __index = ix;
        }



    }


    protected final int nextChar() {

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
        return ByteScanner.errorDetails(message, charArray, __index, __currentChar);
    }



    private static int  skipWhiteSpaceFastBytes( byte [] array, int index ) {
        int c;
        for (; index< array.length; index++ ) {
            c = array [index];
            if ( c > 32 ) {

                return index;
            }
        }
        return index-1;
    }


    protected final void skipWhiteSpace() {
        __index = skipWhiteSpaceFastBytes ( this.charArray, __index );
        this.__currentChar = this.charArray[__index];
    }


    protected int lastIndex;

    private Object decode( byte[] cs ) {

        lastIndex = cs.length -1;
        charArray = cs;
        __index = 0;
        return decodeValue();
    }


    public <T> T parse( Class<T> type, String str ) {
        return this.parse( type, str.getBytes( charset ) );
    }

    public <T> T parse( Class<T> type, byte[] bytes ) {

        if ( type == Map.class || type == List.class || Typ.isBasicType(type) ) {
            return ( T ) this.decode( bytes );
        } else {
            Map<String, Object> objectMap = ( Map<String, Object> ) this.decode( bytes );
            return MapObjectConversion.fromMap(objectMap, type);
        }

    }

    @Override
    public Object parse ( byte[] bytes ) {
        return this.decode ( bytes );
    }



    public <T> T parse( Class<T> type, InputStream input ) {
        return parse( type, IO.input(input) );
    }

    public <T> T parse( Class<T> type, CharSequence charSequence ) {
        return parse( type, charSequence.toString() );
    }

    public <T> T parse( Class<T> type, char[] chars ) {

        return parse( type, new String( chars ) );
    }


    protected void complain( String complaint ) {
        throw new JsonException( exceptionDetails( complaint ) );
    }



    protected final Object decodeJsonObject() {


        if ( __currentChar == OPEN_CURLY )  {
            __index++;
        }

        LazyMap map = new LazyMap ();

        for (; __index < this.charArray.length; __index++ ) {

            skipWhiteSpaceIfNeeded ();


            if ( __currentChar == DOUBLE_QUOTE ) {

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

                if ( __currentChar != COLON ) {

                    complain( "expecting current character to be " + charDescription( __currentChar ) + "\n" );
                }
                __index++;

                skipWhiteSpaceIfNeeded ();

                Object value = decodeValue();

                skipWhiteSpaceIfNeeded ();
                map.put( key, value );


            }
            if ( __currentChar == CLOSED_CURLY ) {
                __index++;
                break;
            } else if ( __currentChar == COMMA) {
                continue;
            } else {
                complain(
                        "expecting '}' or ',' but got current char " + charDescription( __currentChar ) );

            }
        }


        return map;
    }



    protected final Object decodeValue() {
        Object value = null;

        done:
        for (; __index < this.charArray.length; __index++ ) {
            __currentChar = charArray[ __index ];


            switch ( __currentChar ) {
                case NEW_LINE:
                    break;

                case RETURN:
                case SPACE:
                case TAB:
                case BELL:
                case FORM_FEED:
                    break;

                case DOUBLE_QUOTE:
                    value = decodeString ();
                    break done;


                case LETTER_T:
                    value = decodeTrue();
                    break done;

                case LETTER_F:
                    value = decodeFalse();
                    break done;

                case LETTER_N:
                    value = decodeNull();
                    break done;

                case OPEN_BRACKET:
                    value = decodeJsonArray();
                    break done;

                case OPEN_CURLY:
                    value = decodeJsonObject();
                    break done;

                case ALPHA_1:
                case ALPHA_2:
                case ALPHA_3:
                case ALPHA_4:
                case ALPHA_5:
                case ALPHA_6:
                case ALPHA_7:
                case ALPHA_8:
                case ALPHA_9:
                case ALPHA_0:
                    value = decodeNumber();
                    break done;

                case MINUS:
                    value = decodeNumber();
                    break done;

                default:
                    throw new JsonException( exceptionDetails( "Unable to determine the " +
                            "current character, it is not a string, number, array, or object" ) );

            }
        }

        return value;
    }



    int[] endIndex = new int[1];

    private final Object decodeNumber() {

        Number num =  ByteScanner.parseJsonNumber( charArray, __index, charArray.length, endIndex );
        __index = endIndex[0];

        return num;
    }



    protected final static byte[] NULL = Byt.bytes( "null" );

    protected final Object decodeNull() {

        if ( __index + NULL.length <= charArray.length ) {
            if ( charArray[ __index ] == LETTER_N &&
                    charArray[ ++__index ] == LETTER_U &&
                    charArray[ ++__index ] == LETTER_L &&
                    charArray[ ++__index ] == LETTER_L ) {
                nextChar();
                return null;
            }
        }
        throw new JsonException( exceptionDetails( "null not parsed properly" ) );
    }

    protected final static byte[] TRUE = Byt.bytes( "true" );

    protected final boolean decodeTrue() {

        if ( __index + TRUE.length <= charArray.length ) {
            if ( charArray[ __index ] == LETTER_T &&
                    charArray[ ++__index ] == LETTER_R &&
                    charArray[ ++__index ] == LETTER_U &&
                    charArray[ ++__index ] == LETTER_E ) {

                nextChar();
                return true;

            }
        }

        throw new JsonException( exceptionDetails( "true not parsed properly" ) );
    }


    protected final static byte[] FALSE = Byt.bytes( "false" );

    protected final boolean decodeFalse() {

        if ( __index + FALSE.length <= charArray.length ) {
            if ( charArray[ __index ] == LETTER_F &&
                    charArray[ ++__index ] == LETTER_A &&
                    charArray[ ++__index ] == LETTER_L &&
                    charArray[ ++__index ] == LETTER_S &&
                    charArray[ ++__index ] == LETTER_E ) {
                nextChar();
                return false;
            }
        }
        throw new JsonException( exceptionDetails( "false not parsed properly" ) );
    }



    protected abstract String decodeString();



    protected final List decodeJsonArray() {


        ArrayList<Object> list = null;

        boolean foundEnd = false;
        byte [] charArray = this.charArray;

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


            int c;

            loop:
            while ( this.hasMore() ) {

                Object arrayItem = decodeValue();

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

    protected final List decodeJsonArrayOLD() {
        if ( __currentChar == OPEN_BRACKET ) {
            this.nextChar();
        }

        skipWhiteSpace();

                /* the list might be empty  */
        if ( __currentChar == CLOSED_BRACKET ) {
            this.nextChar();
            return Collections.EMPTY_LIST;
        }


        ArrayList<Object> list = new ArrayList();

        boolean foundEnd = false;



        int arrayIndex = 0;


        try {
            while ( this.hasMore() ) {
                skipWhiteSpace();

                Object arrayItem = decodeValue();

                list.add( arrayItem );

                arrayIndex++;

                __currentChar = this.charArray[__index];


                if ( __currentChar == COMMA ) {
                    this.nextChar();
                    continue;
                } else if ( __currentChar == CLOSED_BRACKET ) {
                    this.nextChar();
                    foundEnd = true;
                    break;
                }

                skipWhiteSpace();


                if ( __currentChar == COMMA ) {
                    this.nextChar();
                    continue;
                } else if ( __currentChar == CLOSED_BRACKET ) {
                    this.nextChar();
                    foundEnd = true;
                    break;
                } else {
                    String charString = charDescription( __currentChar );

                    complain(
                            String.format( "expecting a ',' or a ']', " +
                                    " but got \nthe current character of  %s " +
                                    " on array index of %s \n", charString, arrayIndex )
                    );

                }
            }
        }catch (Exception ex) {
            throw new JsonException( exceptionDetails( ex.getMessage() ), ex );
        }

        if (!foundEnd) {
            complain( "No end bracket found for JSON Array" );
        }
        return list;
    }


    public <T> T parseDirect( Class<T> type, byte[] value ) {
        return this.parse( type, value );
    }

    public <T> T parseAsStream( Class<T> type, byte[] value ) {
        return this.parse( type, new InMemoryInputStream( value ) );
    }



    public <T> T parse( Class<T> type, byte[] bytes, Charset charset ) {
        return parse ( type, bytes );
    }


    public <T> T parseFile( Class<T> type, String fileName ) {
        return parse(type, IO.input ( fileName ));
    }



    @Override
    public Object parse ( char[] chars ) {
        return parse ( new String (chars) );
    }

    @Override
    public Object parse ( String string ) {
        return parse ( string.getBytes ( charset ) );
    }


    @Override
    public Object parse ( byte[] bytes, Charset charset ) {

        return parse (  bytes );
    }




}
