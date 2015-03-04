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

import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.value.*;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.primitive.CharScanner.*;

/**
 * Created by rick on 12/21/13.
 */
public class PlistParser extends JsonFastParser {


    protected static final int CLOSED_PAREN = ')';
    protected static final int SEMI_COLON = ';';


    public PlistParser() {
    }

    public PlistParser( boolean useValues ) {
        super ( useValues );
    }

    public PlistParser( boolean useValues, boolean chop ) {
        super ( useValues, chop );
    }

    public PlistParser( boolean useValues, boolean chop, boolean lazyChop ) {
        super ( useValues, chop, lazyChop );
    }

    public PlistParser( boolean useValues, boolean chop, boolean lazyChop, boolean checkDates ) {
        super ( useValues, chop, lazyChop, checkDates );
    }

    private Value decodeJsonObjectLax() {

        if ( __currentChar == '{' )
            this.nextChar();

        ValueMap map = useValues ? new ValueMapImpl() : new LazyValueMap( lazyChop );
        Value value = new ValueContainer( map );

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

                case '=':

                    char startChar = charArray[ startIndexOfKey ];
                    if ( startChar == ';' ) {
                        startIndexOfKey++;
                    }


                    key = extractLaxString( startIndexOfKey, __index - 1, false, false );
                    __index++; //skip :


                    item = decodeValuePlist();
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
                    key = decodeStringPlist(  );


                    skipWhiteSpaceIfNeeded ();

                    if ( __currentChar != '=' ) {

                        complain( "expecting current character to be '='  but got " + charDescription( __currentChar ) + "\n" );
                    }
                    __index++;
                    item = decodeValuePlist();


                    skipWhiteSpaceIfNeeded ();
                    miv = new MapItemValue( key, item );

                    map.add( miv );

                    startIndexOfKey = __index;
                    if ( __currentChar == '}' ) {
                        __index++;
                        if ( hasMore() ) {
                            if ( charArray[ __index ] == ';' ) {
                                __index++;
                            }
                        }
                        break done;
                    }

                    break;


                case '}':
                    __index++;
                    if ( hasMore() ) {
                        if ( charArray[ __index ] == ';' ) {
                            __index++;
                        }
                    }
                    break done;
            }
        }

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

                case ')':
                    continue;
                default:
                    break endIndexLookup;
            }
        }
        return new CharSequenceValue( chop, TypeType.STRING, startIndexOfKey, endIndex + 1, this.charArray, encoded, checkDate );
    }

    private Value decodeValuePlist() {
        Value value = null;

        for (; __index < charArray.length; __index++ ) {
            skipWhiteSpaceIfNeeded ();

            switch ( __currentChar ) {

                case '/': /* */ //
                    handleComment();
                    break;

                case '#':
                    handleBashComment();
                    break;

                case '"':
                    value = decodeStringPlist(  );
                    break;

                case 't':
                    if ( isTrue() ) {
                        return decodeTrue() == true ? ValueContainer.TRUE : ValueContainer.FALSE;
                    } else {
                        value = decodeStringLax();
                    }
                    break;

                case 'f':
                    if ( isFalse() ) {
                        return decodeFalse() == false ? ValueContainer.FALSE : ValueContainer.TRUE;
                    } else {
                        value = decodeStringLax();
                    }
                    break;

                case 'n':
                    if ( isNull() ) {
                        return decodeNull() == null ? ValueContainer.NULL : ValueContainer.NULL;
                    } else {
                        value = decodeStringLax();
                    }

                    break;

                case '(':
                    value = decodeJsonArrayLax();
                    break;

                case '{':
                    value = decodeJsonObjectLax();
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
                    value = decodeNumberPLIST( false );
                    break;
                case '-':
                    value = decodeNumberPLIST( true );
                    break;

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




    protected static boolean isPLISTDelimiter( int c ) {

        return c == SEMI_COLON || c == CLOSED_CURLY || c == CLOSED_PAREN || c == COMMA;
    }

    protected Value decodeNumberPLIST( boolean minus ) {
        char[] array = charArray;

        final int startIndex = __index;
        int index = __index;
        char currentChar;
        boolean doubleFloat = false;

        if ( minus && index + 1 < array.length ) {
            index++;
        }


        while ( true ) {
            currentChar = array[ index ];
            if ( isNumberDigit( currentChar ) ) {
                //noop
            } else if ( currentChar <= 32 ) { //white
                break;
            } else if ( isPLISTDelimiter( currentChar ) ) {
                break;
            } else if ( isDecimalChar( currentChar ) ) {
                doubleFloat = true;
            }
            index++;
            if ( index >= array.length ) break;
        }

        __index = index;
        __currentChar = currentChar;

        TypeType type = doubleFloat ? TypeType.DOUBLE : TypeType.INT;

        NumberValue value = new NumberValue ( chop, type, startIndex, __index, this.charArray );

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
        char currentChar = charArray[ __index ];
        final int startIndex = __index;
        boolean encoded = false;
        char [] charArray = this.charArray;

        for (; index < charArray.length; index++ ) {
            currentChar = charArray[ index ];

            if (isPLISTDelimiter( currentChar )) break;
            else if (currentChar == '\\') break;
        }

        Value value = this.extractLaxString( startIndex, index, encoded, checkDates );

        __index = index;
        return value;
    }

    private Value decodeStringPlist(  ) {

        char[] array = charArray;
        int index = __index;
        char currentChar = charArray[index];

        if ( index < array.length && currentChar == '"' ) {
            index++;
        }

        final int startIndex = index;

        boolean encoded = hasEscapeChar ( array, index, indexHolder );
        index = indexHolder[0];

        if (encoded)  {
            index = findEndQuote ( array, index );
        }

        Value value = new CharSequenceValue ( chop, TypeType.STRING, startIndex, index, array, encoded, checkDates );

        if ( index < array.length ) {
            index++;
        }

        __index = index;
        return value;
    }

    private Value decodeJsonArrayLax() {

        if ( __currentChar == '(' ) {
            __index++;
        }

        skipWhiteSpaceIfNeeded ();

                /* the list might be empty  */
        if ( __currentChar == ')' ) {
            __index++;
            return EMPTY_LIST;
        }

        List<Object> list;

        if ( useValues ) {
            list = new ArrayList<>();
        } else {
            list = new ValueList( lazyChop );
        }

        Value value = new ValueContainer ( list );

        skipWhiteSpaceIfNeeded ();

        do {
            skipWhiteSpaceIfNeeded ();

            Object arrayItem = decodeValuePlist();

            list.add( arrayItem );

            skipWhiteSpaceIfNeeded ();

            char c = __currentChar;

            if ( c == ',' ) {
                __index++;
                continue;
            } else if ( c == ')' ) {
                __index++;
                break;
            } else {

                String charString = charDescription( c );

                complain(
                        String.format( "expecting a ',' or a ')', " +
                                " but got \nthe current character of  %s " +
                                " on array index of %s \n", charString, list.size() )
                );

            }
        } while ( this.hasMore() );

        return value;
    }


    public  Object parse( char[] chars ) {
        lastIndex = chars.length -1;
        __index = 0;
        charArray = chars;

        Value value = decodeValuePlist();
        if (value.isContainer ()) {
            return value.toValue ();
        } else {
            return value;
        }
    }

}
