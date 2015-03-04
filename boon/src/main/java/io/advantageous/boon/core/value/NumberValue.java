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

import io.advantageous.boon.Boon;
import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Dates;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.primitive.CharBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;

import static io.advantageous.boon.Exceptions.die;

public class NumberValue extends Number implements Value {


    private char[] buffer;
    private boolean chopped;
    private int startIndex;
    private int endIndex;
    private TypeType type;
    private Object value;


    public NumberValue( TypeType type ) {
        this.type = type;
    }

    public NumberValue() {

    }

    public NumberValue( boolean chop, TypeType type, int startIndex, int endIndex, char[] buffer ) {
        this.type = type;


        try {
            if ( chop ) {

                this.buffer = Arrays.copyOfRange ( buffer, startIndex, endIndex );
                this.startIndex = 0;
                this.endIndex = this.buffer.length;
                chopped = true;
            } else {
                this.startIndex = startIndex;
                this.endIndex = endIndex;
                this.buffer = buffer;
            }
        } catch ( Exception ex ) {
            Boon.puts("exception", ex, "start", startIndex, "end", endIndex);
            Exceptions.handle(ex);

        }
    }



    public String toString () {
        if ( startIndex == 0 && endIndex == buffer.length ) {
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

       return Conversions.toEnum( cls, intValue() );
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    private final Object doToValue () {

        switch ( type ) {
            case DOUBLE:
            case DOUBLE_WRAPPER:
                return doubleValue ();
            case INT:
            case INTEGER_WRAPPER:

                if ( CharScanner.isInteger(buffer, startIndex, endIndex - startIndex) ) {
                    return intValue ();
                } else {
                    return longValue ();
                }
        }
        Exceptions.die();
        return null;
    }

    @Override
    public boolean equals ( Object o ) {

        if (o instanceof Integer) {
            Integer i = (Integer) o;
            return toValue().equals(i);
        } else if  (o instanceof Float) {
            Float i = (Float) o;
            return floatValue() == i;
        } else if (o instanceof Double) {
            Double i = (Double) o;
            return toValue().equals(i);
        } else if (o instanceof Short) {
            Short i = (Short) o;
            return shortValue() == i;
        } else if (o instanceof Byte) {
            Byte i = (Byte) o;
            return byteValue() == i;
        } else if (o instanceof Date) {
            Date i = (Date) o;
            return dateValue().equals(i);
        } else if (o instanceof Long) {
            Long i = (Long) o;
            return longValue()==i;
        }
        if ( this == o ) return true;
        if ( !( o instanceof Value ) ) return false;

        NumberValue value1 = ( NumberValue ) o;

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


    public BigDecimal bigDecimalValue () {
        return new BigDecimal ( buffer, startIndex, endIndex - startIndex );
    }

    @Override
    public BigInteger bigIntegerValue() {
        return new BigInteger ( toString () );
    }

    public String stringValue () {
            return toString ();
    }

    @Override
    public String stringValue(CharBuf charBuf) {
        return toString();
    }

    @Override
    public String stringValueEncoded () {
           return toString ();
    }


    @Override
    public Date dateValue () {
           return new Date ( Dates.utc ( longValue () ) );
    }


    @Override
    public int intValue () {
        if ( CharScanner.isInteger(buffer, startIndex, endIndex - startIndex) ){
            return CharScanner.parseInt(buffer, startIndex, endIndex);
        } else {
            return 0;
        }
    }

    @Override
    public long longValue () {

        if ( CharScanner.isInteger(buffer, startIndex, endIndex - startIndex) ){
            return CharScanner.parseInt(buffer, startIndex, endIndex);
        } else if( CharScanner.isLong(buffer, startIndex, endIndex - startIndex)){
            return CharScanner.parseLong(buffer, startIndex, endIndex);
        } else {
            return 0L;
        }
    }


    public byte byteValue () {
        return ( byte ) intValue ();
    }

    public short shortValue () {
        return ( short ) intValue ();
    }


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
        return TypeType.NUMBER;
    }




}
