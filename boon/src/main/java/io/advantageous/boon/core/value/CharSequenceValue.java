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

package io.advantageous.boon.core.value;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.JsonException;
import io.advantageous.boon.json.implementation.JsonStringDecoder;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Dates;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.primitive.CharBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;

import static io.advantageous.boon.Exceptions.die;

public class CharSequenceValue implements Value, CharSequence {

    private final TypeType type;
    private final boolean checkDate;
    private final boolean decodeStrings;

    private char[] buffer;
    private boolean chopped;
    private int startIndex;
    private int endIndex;
    private Object value;

    public CharSequenceValue( boolean chop, TypeType type, int startIndex, int endIndex, char[] buffer,
                              boolean encoded, boolean checkDate ) {
        this.type = type;
        this.checkDate = checkDate;
        this.decodeStrings = encoded;
        
        if ( chop ) {
            try {
                this.buffer = Arrays.copyOfRange ( buffer, startIndex, endIndex );
            } catch ( Exception ex ) {
                Exceptions.handle(ex);
            }
            this.startIndex = 0;
            this.endIndex = this.buffer.length;
            this.chopped = true;

        } else {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.buffer = buffer;
        }
    }

    public String toString () {

        if (this.decodeStrings) {
            return stringValue();
        } else if ( startIndex == 0 && endIndex == buffer.length ) {
            return FastStringUtils.noCopyStringFromCharsNoCheck(buffer);
        } else {
            return new String ( buffer, startIndex, ( endIndex - startIndex ) );
        }
    }

    @Override
    public final Object toValue () {
        return value != null ? value : (value = doToValue ()) ;
    }

    @Override
    public <T extends Enum> T toEnum( Class<T> cls ) {

        switch ( type ) {
            case STRING:
                return Conversions.toEnum ( cls, stringValue () );
            case INT:
            case INTEGER_WRAPPER:
                return Conversions.toEnum( cls, intValue() );
            case NULL:
                return null;
        }
        Exceptions.die("toEnum " + cls + " value was " + stringValue());
        return null;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    private Object doToValue () {

        switch ( type ) {
            case DOUBLE:
                return doubleValue ();

            case INT:
            case INTEGER_WRAPPER:



                if ( CharScanner.isInteger(buffer, startIndex, endIndex - startIndex) ) {
                    return intValue () ;
                } else {
                    return longValue () ;
                }
            case STRING:
                if ( checkDate ) {
                    Date date = null;
                    if ( Dates.isISO8601QuickCheck ( buffer, startIndex, endIndex ) ) {
                        if (Dates.isJsonDate ( buffer,startIndex, endIndex )) {
                            date =  Dates.fromJsonDate ( buffer, startIndex, endIndex );
                        } else if (Dates.isISO8601( buffer, startIndex, endIndex )) {
                            date =  Dates.fromISO8601( buffer, startIndex, endIndex );
                        } else {
                            return stringValue();
                        }

                        if (date == null) {
                            return stringValue ();
                        } else {
                            return date;
                        }
                    }
                }
                return stringValue ();
        }
        Exceptions.die();
        return null;
    }

    @Override
    public boolean equals ( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof Value ) ) return false;

        CharSequenceValue value1 = ( CharSequenceValue ) o;

        if ( endIndex != value1.endIndex ) return false;
        if ( startIndex != value1.startIndex ) return false;
        if ( !Arrays.equals ( buffer, value1.buffer ) ) return false;
        if ( type != value1.type ) return false;
        if ( value != null ? !value.equals ( value1.value ) : value1.value != null ) return false;

        return true;
    }

    @Override
    public int hashCode () {
        int result = type != null ? type.hashCode () : 0;
        result = 31 * result + ( buffer != null ? Arrays.hashCode ( buffer ) : 0 );
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + ( value != null ? value.hashCode () : 0 );
        return result;
    }


    @Override
    public final int length () {
        return buffer.length;
    }

    @Override
    public final char charAt ( int index ) {
        return buffer[ index ];
    }

    @Override
    public final CharSequence subSequence ( int start, int end ) {
        return new CharSequenceValue (false, type, start, end, buffer, decodeStrings, checkDate);
    }

    public BigDecimal bigDecimalValue () {
        return new BigDecimal ( buffer, startIndex, endIndex - startIndex );
    }

    @Override
    public BigInteger bigIntegerValue() {
        return new BigInteger ( toString () );
    }

    public String stringValue () {
        if ( this.decodeStrings ) {
            return JsonStringDecoder.decodeForSure(buffer, startIndex, endIndex);
        } else {
            return toString ();
        }
    }

    @Override
    public String stringValue(CharBuf charBuf) {
        if ( this.decodeStrings ) {
            return JsonStringDecoder.decodeForSure ( charBuf, buffer, startIndex, endIndex );
        } else {
            return toString ();
        }
    }

    @Override
    public String stringValueEncoded () {
        return JsonStringDecoder.decode ( buffer, startIndex, endIndex );
    }

    @Override
    public Date dateValue () {


        if ( type == TypeType.STRING ) {

            if ( Dates.isISO8601QuickCheck ( buffer, startIndex, endIndex ) ) {

                if ( Dates.isJsonDate ( buffer, startIndex, endIndex ) ) {
                    return Dates.fromJsonDate ( buffer, startIndex, endIndex );

                } else if ( Dates.isISO8601Jackson(buffer, startIndex, endIndex) ) {
                    return Dates.fromISO8601Jackson(buffer, startIndex, endIndex);
                } else if ( Dates.isISO8601 ( buffer, startIndex, endIndex ) ) {
                    return Dates.fromISO8601 ( buffer, startIndex, endIndex );
                } else {
                    throw new JsonException( "Unable to convert " + stringValue () + " to date " );
                }
            } else {

                throw new JsonException ( "Unable to convert " + stringValue () + " to date " );
            }
        } else {

            return new Date ( Dates.utc ( longValue () ) );
        }
    }

    @Override
    public int intValue () {
        if ( CharScanner.isInteger(buffer, startIndex, endIndex - startIndex) ){

            return CharScanner.parseInt(buffer, startIndex, endIndex);
        }  else {
            return Exceptions.die(int.class, "not an int");
        }
    }

    @Override
    public long longValue () {
        if ( CharScanner.isInteger(buffer, startIndex, endIndex - startIndex) ){
            return CharScanner.parseInt(buffer, startIndex, endIndex);
        } else {
           return CharScanner.parseLong(buffer, startIndex, endIndex);
        }
    }

    public byte byteValue () {
        return ( byte ) intValue ();
    }

    public short shortValue () {
        return ( short ) intValue ();
    }

    private static float fpowersOf10[] = {
            1.0f,
            10.0f,
            100.0f,
            1_000.0f,
            10_000.0f,
            100_000.0f,
            1_000_000.0f,
            10_000_000.0f,
            100_000_000.0f,
            1_000_000_000.0f,
    };

    @Override
    public double doubleValue () {
        return CharScanner.parseDouble( this.buffer, startIndex, endIndex );
    }

    @Override
    public boolean booleanValue() {
        return Boolean.parseBoolean ( toString () );
    }

    @Override
    public float floatValue () {
        return CharScanner.parseFloat( this.buffer, startIndex, endIndex );
    }

    @Override
    public Currency currencyValue () {
        return Currency.getInstance( toString() );
    }

    public final void chop () {
        if ( !chopped ) {
            this.chopped = true;
            this.buffer = Arrays.copyOfRange ( buffer, startIndex, endIndex );
            this.startIndex = 0;
            this.endIndex = this.buffer.length;
        }
    }

    @Override
    public char charValue () {
        return buffer[startIndex];
    }

    @Override
    public TypeType type() {
        return TypeType.CHAR_SEQUENCE;
    }
}
