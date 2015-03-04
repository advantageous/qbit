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


import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.fields.FieldAccessMode;
import io.advantageous.boon.core.reflection.fields.FieldsAccessor;
import io.advantageous.boon.core.value.*;
import io.advantageous.boon.json.JsonException;
import io.advantageous.boon.json.JsonParserEvents;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.core.TypeType;

import java.util.*;

/**
 * Created by rick on 12/12/13.
 */
public class JsonParserLax extends JsonFastParser {

    private static ValueContainer EMPTY_LIST = new ValueContainer ( Collections.emptyList() );

    private final JsonParserEvents events;


    public JsonParserLax() {
        this( FieldAccessMode.FIELD );
    }

    public JsonParserLax( FieldAccessMode mode ) {
        this( mode, true );
    }

    public JsonParserLax( FieldAccessMode mode, boolean useAnnotations ) {
        this( FieldAccessMode.create(mode, useAnnotations) );
    }

    public JsonParserLax( FieldsAccessor fieldsAccessor ) {
        this( false );
    }

    public JsonParserLax(  boolean useValues ) {
        this(  useValues, false );
    }

    public JsonParserLax(  boolean useValues, boolean chop ) {
        this(  useValues, chop, !chop );
    }

    public JsonParserLax( boolean useValues, boolean chop, boolean lazyChop ) {
        this(  useValues, chop, lazyChop, true );
    }

    public JsonParserLax(  boolean useValues, boolean chop, boolean lazyChop, boolean defaultCheckDates ) {
        this( useValues, chop, lazyChop, defaultCheckDates, null );
    }

    public JsonParserLax(  boolean useValues, boolean chop, boolean lazyChop, boolean defaultCheckDates, JsonParserEvents events) {
        super(useValues, chop, lazyChop, defaultCheckDates);
        this.events = events;
    }

    private Value decodeJsonObjectLax(boolean isRoot) {

        if ( __currentChar == '{' )
            this.nextChar();


        ValueMap map =  useValues ? new ValueMapImpl() : new LazyValueMap( lazyChop );

        if (events!=null ) {
            if (isRoot) root = map;
            if (!events.objectStart(__index))stop();

        }
        Value value  = new ValueContainer ( map );

        skipWhiteSpaceIfNeeded ();
        int startIndexOfKey = __index;
        Value key;
        MapItemValue miv;
        Value item;

        done:
        for (; __index < this.charArray.length; __index++ ) {

            skipWhiteSpaceIfNeeded ();

            switch ( __currentChar ) {
                case '/': /* */ //
                    handleComment();
                    startIndexOfKey = __index;
                    break;

                case '#':
                    handleBashComment();
                    startIndexOfKey = __index;
                    break;

                case ':':
                    char startChar = charArray[ startIndexOfKey ];
                    if ( startChar == ',' ) {
                        startIndexOfKey++;
                    }

                    key = extractLaxString( startIndexOfKey, __index - 1, false, false );

                    if (events!=null) if(!events.objectFieldName( __index, map, (CharSequenceValue) key ))stop();
                    __index++; //skip :


                    item = decodeValueInternal();
                    if (events!=null) if(!events.objectField( __index,  map, (CharSequenceValue) key, item ))stop();
                    skipWhiteSpaceIfNeeded ();

                    miv = new MapItemValue( key, item );

                    map.add( miv );

                    startIndexOfKey = __index;
                    if ( __currentChar == '}' ) {
                        __index++;
                        break done;
                    }

                    break;

                case '\'':
                    key = decodeStringSingle(  );


                    if (events!=null) if(!events.objectFieldName( __index, map, (CharSequenceValue) key ))stop();

                    skipWhiteSpaceIfNeeded ();

                    if ( __currentChar != ':' ) {

                        complain( "expecting current character to be ':' but got " + charDescription( __currentChar ) + "\n" );
                    }
                    __index++;
                    item = decodeValueInternal();

                    if (events!=null) if (!events.objectField( __index, map, ( CharSequenceValue ) key, item ))stop();


                    skipWhiteSpaceIfNeeded ();

                    miv = new MapItemValue( key, item );

                    map.add( miv );
                    startIndexOfKey = __index;
                    if ( __currentChar == '}' ) {
                        __index++;
                        break done;
                    }

                    break;

                case '"':
                    key = decodeStringDouble(  );

                    if (events!=null) events.objectFieldName( __index, map, (CharSequenceValue) key );

                    skipWhiteSpaceIfNeeded ();

                    if ( __currentChar != ':' ) {

                        complain( "expecting current character to be ':' but got " + charDescription( __currentChar ) + "\n" );
                    }
                    __index++;
                    item = decodeValueInternal();

                    if (events!=null) if(!events.objectField( __index, map, (CharSequenceValue) key, item ))stop();

                    skipWhiteSpaceIfNeeded ();

                    miv = new MapItemValue( key, item );

                    map.add( miv );
                    startIndexOfKey = __index;
                    if ( __currentChar == '}' ) {
                        __index++;
                        break done;
                    }

                    break;

                case '}':
                    __index++;
                    break done;
            }
        }


        if (events!=null) if (!events.objectEnd( __index, map ))stop();
        return value;
    }

    private Value extractLaxString( int startIndexOfKey, int end, boolean encoded, boolean checkDate ) {
        char startChar;
        startIndexLookup:
        for (; startIndexOfKey < __index && startIndexOfKey < charArray.length; startIndexOfKey++ ) {
            startChar = charArray[ startIndexOfKey ];
            switch ( startChar ) {
                case ' ':
                case '\n':
                case '\t':
                    continue;

                default:
                    break startIndexLookup;
            }
        }

        char endChar;
        int endIndex = end >= charArray.length ? charArray.length - 1 : end;
        endIndexLookup:
        for (; endIndex >= startIndexOfKey + 1 && endIndex >= 0; endIndex-- ) {
            endChar = charArray[ endIndex ];
            switch ( endChar ) {
                case ' ':
                case '\n':
                case '\t':
                case '}':
                    continue;
                case ',':
                case ';':
                    continue;

                case ']':
                     continue;
                default:
                    break endIndexLookup;
            }
        }
        CharSequenceValue value = new  CharSequenceValue ( chop, TypeType.STRING, startIndexOfKey, endIndex + 1, this.charArray, encoded, checkDate );
        if (events!=null) if (!events.string( startIndexOfKey, endIndex + 1, value ))stop();
        return value;
    }



    Object root;


    @Override
    public  Object parse( char[] chars ) {
        lastIndex = chars.length -1;

        try {


            __index = 0;
            charArray = chars;

            Value value = decodeValueInternal( true );
            if (value.isContainer ()) {
                return value.toValue ();
            } else {
                return value;
            }

        } catch (StopException stop) {
            return root;
        }

    }





    protected final Value decodeValueInternal() {
        return decodeValueInternal( false );
    }
    protected final Value decodeValueInternal(boolean isRoot) {
        Value value = null;

        for (; __index < charArray.length; __index++ ) {
            skipWhiteSpaceIfNeeded ();

            switch ( __currentChar ) {
                case '\n':
                    break;

                case '\r':
                    break;

                case ' ':
                    break;

                case '\t':
                    break;

                case '\b':
                    break;

                case '\f':
                    break;

                case '/': /* */ //
                    handleComment();
                    break;

                case '#':
                    handleBashComment();
                    break;

                case '"':
                    value = decodeStringDouble(  );
                    break;

                case '\'':
                    value = decodeStringSingle( );
                    break;


                case 't':
                    if ( isTrue() ) {
                        return decodeTrueWithEvents() == true ? ValueContainer.TRUE : ValueContainer.FALSE;
                    } else {
                        value = decodeStringLax();
                    }
                    break;

                case 'f':
                    if ( isFalse() ) {
                        return decodeFalseWithEvents() == false ? ValueContainer.FALSE : ValueContainer.TRUE;
                    } else {
                        value = decodeStringLax();
                    }
                    break;

                case 'n':
                    if ( isNull() ) {
                        return decodeNullWithEvents() == null ? ValueContainer.NULL : ValueContainer.NULL;
                    } else {
                        value = decodeStringLax();
                    }

                    break;

                case '[':
                    value = decodeJsonArrayLax(isRoot);
                    break;

                case '{':
                    value = decodeJsonObjectLax(isRoot);
                    break;

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '0':
                    return decodeNumberLax (false);

                case '-':
                    return decodeNumberLax (true);

                default:
                    value = decodeStringLax();
            }

            if ( value != null ) {
                return value;
            }
        }

        return null;
    }


    private void handleBashComment() {
        for (; __index < charArray.length; __index++ ) {
            __currentChar = charArray[ __index ];

            if ( __currentChar == '\n' ) {
                __index++;
                return;
            }
        }
    }

    private void handleComment() {

        if ( hasMore() ) {
            __index++;
            __currentChar = charArray[ __index ];

            switch ( __currentChar ) {
                case '*':
                    for (; __index < charArray.length; __index++ ) {
                        __currentChar = charArray[ __index ];

                        if ( __currentChar == '*' ) {
                            if ( hasMore() ) {
                                __index++;
                                __currentChar = charArray[ __index ];
                                if ( __currentChar == '/' ) {
                                    if ( hasMore() ) {
                                        __index++;
                                        return;
                                    }
                                }
                            } else {
                                complain( "missing close of comment" );
                            }
                        }
                    }

                case '/':
                    for (; __index < charArray.length; __index++ ) {
                        __currentChar = charArray[ __index ];

                        if ( __currentChar == '\n' ) {
                            if ( hasMore() ) {
                                __index++;
                                return;
                            } else {
                                return;
                            }
                        }
                    }
            }
        }
    }

    protected final Value decodeNumberLax(boolean minus) {

        char[] array = charArray;

        final int startIndex = __index;
        int index =  __index;
        char currentChar;
        boolean doubleFloat = false;

        if (minus && index + 1 < array.length) {
            index++;
        }

        while (true) {
            currentChar = array[index];
            if ( CharScanner.isNumberDigit(currentChar)) {
                //noop
            } else if ( currentChar <= 32 ) { //white
                break;
            } else if ( CharScanner.isDelimiter(currentChar) ) {
                break;
            } else if ( CharScanner.isDecimalChar(currentChar) ) {
                doubleFloat = true;
            }
            index++;
            if (index   >= array.length) break;
        }

        __index = index;
        __currentChar = currentChar;

        TypeType type = doubleFloat ? TypeType.DOUBLE : TypeType.INT;

        NumberValue value = new NumberValue ( chop, type, startIndex, __index, this.charArray );
        if (events!=null) if (!events.number( startIndex, __index, value ))stop();

        return value;
    }

    private boolean isNull() {
        if ( __index + NULL.length <= charArray.length ) {
            if ( charArray[ __index ] == 'n' &&
                    charArray[ __index + 1 ] == 'u' &&
                    charArray[ __index + 2 ] == 'l' &&
                    charArray[ __index + 3 ] == 'l' ) {
                return true;
            }
        }
        return false;
    }

    private boolean isTrue() {
        if ( __index + TRUE.length <= charArray.length ) {
            if ( charArray[ __index ] == 't' &&
                    charArray[ __index + 1 ] == 'r' &&
                    charArray[ __index + 2 ] == 'u' &&
                    charArray[ __index + 3 ] == 'e' ) {
                return true;

            }
        }
        return false;
    }

    private boolean isFalse() {
        if ( __index + FALSE.length <= charArray.length ) {
            if ( charArray[ __index ] == 'f' &&
                    charArray[ __index + 1 ] == 'a' &&
                    charArray[ __index + 2 ] == 'l' &&
                    charArray[ __index + 3 ] == 's' &&
                    charArray[ __index + 4 ] == 'e' ) {
                return true;
            }
        }
        return false;
    }

    private Value decodeStringLax() {
        int index = __index;
        char currentChar;
        final int startIndex = __index;
        boolean encoded = false;
        char [] charArray = this.charArray;

        for (; index < charArray.length; index++ ) {
            currentChar = charArray[ index ];

            if (CharScanner.isDelimiter(currentChar)) break;
            else if (currentChar == '\\') break;
        }

        Value value = this.extractLaxString( startIndex, index, encoded, checkDates);
        __index = index;
        return value;
    }

    private Value decodeStringDouble(  ) {

        __currentChar = charArray[ __index ];

        if ( __index < charArray.length && __currentChar == '"' ) {
            __index++;
        }

        final int startIndex = __index;

        boolean escape = false;
        boolean encoded = false;

        done:
        for (; __index < this.charArray.length; __index++ ) {
            __currentChar = charArray[ __index ];
            switch ( __currentChar ) {

                case '"':
                    if ( !escape ) {
                        break done;
                    } else {
                        escape = false;
                        continue;
                    }

                case '\\':
                    if ( !escape ) {
                        escape = true;
                    } else {
                        escape = false;
                    }
                    encoded = true;
                    continue;
            }
            escape = false;
        }

        CharSequenceValue value = new CharSequenceValue ( chop, TypeType.STRING, startIndex, __index, this.charArray, encoded, checkDates );

        if ( __index < charArray.length ) {
            __index++;
        }
        if (events!=null) if (!events.string( startIndex, __index, value )) stop();

        return value;
    }

    private Value decodeStringSingle(  ) {

        __currentChar = charArray[ __index ];

        if ( __index < charArray.length && __currentChar == '\'' ) {
            __index++;
        }

        final int startIndex = __index;

        boolean escape = false;
        boolean encoded = false;
        int minusCount = 0;
        int colonCount = 0;

        done:
        for (; __index < this.charArray.length; __index++ ) {
            __currentChar = charArray[ __index ];
            switch ( __currentChar ) {

                case '\'':
                        if ( !escape ) {
                            break done;
                        } else {
                            escape = false;
                            continue;
                        }

                case '\\':
                    encoded = true;
                    escape = true;
                    continue;

                case '-':
                    minusCount++;
                    break;
                case ':':
                    colonCount++;
                    break;
            }
            escape = false;
        }

        boolean checkDates = this.checkDates && !encoded && minusCount >= 2 && colonCount >= 2;

        CharSequenceValue value = new CharSequenceValue ( chop, TypeType.STRING, startIndex, __index, this.charArray, encoded, checkDates );

        if ( __index < charArray.length ) {
            __index++;
        }


        if (events!=null) if (!events.string( startIndex, __index, value )) stop();
        return value;
    }

    private Value decodeJsonArrayLax(boolean isRoot) {

        if (events!=null) if (!events.arrayStart( __index )) stop();

        if ( __currentChar == '[' ) {
            __index++;
        }


        skipWhiteSpaceIfNeeded ();

        if ( __currentChar == ']' ) {
            __index++;
            return EMPTY_LIST;
        }

        List<Object> list;

        if ( useValues ) {
            list = new ArrayList<>();
        } else {
            list = new ValueList ( lazyChop );
        }

        if (events!=null && isRoot) {
            root = list;
        }

        Value value = new ValueContainer ( list );

        do {

            skipWhiteSpaceIfNeeded ();

            Object arrayItem = decodeValueInternal();


            list.add( arrayItem );

            if (events!=null) if (!events.arrayItem( __index, list, arrayItem )) stop();

            skipWhiteSpaceIfNeeded ();

            char c = __currentChar;

            if ( c == ',' ) {
                __index++;
                continue;
            } else if ( c == ']' ) {
                __index++;
                break;
            } else {
                String charString = charDescription( c );

                complain(
                        String.format( "expecting a ',' or a ']', " +
                                " but got \nthe current character of  %s " +
                                " on array index of %s \n", charString, list.size() )
                );
            }
        } while ( this.hasMore() );


        if (events!=null) if (!events.arrayEnd( __index, list )) stop();
        return value;
    }


    public static class StopException extends JsonException {
        public StopException( String message ) {
            super( message );
        }

    }
    private void stop () {
        throw new StopException( "STOPPED BY EVENT HANDLER at index " + __index );
    }


    protected final boolean decodeTrueWithEvents() {

        if ( __index + TRUE.length <= charArray.length ) {
            if ( charArray[ __index ] == 't' &&
                    charArray[ ++__index ] == 'r' &&
                    charArray[ ++__index ] == 'u' &&
                    charArray[ ++__index ] == 'e' ) {

                if (events!=null) if (!events.bool( __index, true )) stop();

                __index++;
                return true;

            }
        }

        throw new JsonException( exceptionDetails( "true not parsed properly" ) );
    }



    protected final boolean decodeFalseWithEvents() {

        if ( __index + FALSE.length <= charArray.length ) {
            if ( charArray[ __index ] == 'f' &&
                    charArray[ ++__index ] == 'a' &&
                    charArray[ ++__index ] == 'l' &&
                    charArray[ ++__index ] == 's' &&
                    charArray[ ++__index ] == 'e' ) {
                if (events!=null) if (!events.bool( __index, false )) stop();

                __index++;

                return false;
            }
        }
        throw new JsonException( exceptionDetails( "false not parsed properly" ) );
    }




    protected final Object decodeNullWithEvents() {

        if ( __index + NULL.length <= charArray.length ) {
            if ( charArray[ __index ] == 'n' &&
                    charArray[ ++__index ] == 'u' &&
                    charArray[ ++__index ] == 'l' &&
                    charArray[ ++__index ] == 'l' ) {
                __index++;
                if (events!=null) if (!events.nullValue( __index )) stop();

                return null;
            }
        }
        throw new JsonException( exceptionDetails( "null not parse properly" ) );
    }


}
