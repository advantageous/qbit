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
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.primitive.CharBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;

public class ValueContainer implements CharSequence, Value {

    public static final Value TRUE = new ValueContainer ( TypeType.TRUE );
    public static final Value FALSE = new ValueContainer ( TypeType.FALSE );
    public static final Value NULL = new ValueContainer ( TypeType.NULL );

    public Object value;

    public final TypeType type;
    private boolean container;

    public boolean decodeStrings;

    public static Object toObject(Object o) {
        if ( o instanceof ValueContainer ) {
            o = ( ( ValueContainer ) o ).toValue();
        }
        return o;

    }

    public ValueContainer(  Object value, TypeType type, boolean decodeStrings ) {
        this.value = value;
        this.type = type;
        this.decodeStrings = decodeStrings;
    }

    public ValueContainer( TypeType type ) {
        this.type = type;
    }

    public ValueContainer( Map<String, Object> map ) {
        this.value = map;
        this.type = TypeType.MAP;
        this.container = true;
    }

    public ValueContainer( List<Object> list ) {
        this.value = list;
        this.type = TypeType.LIST;
        this.container = true;
    }

    @Override
    public int intValue() {
        return die(int.class, sputs("intValue not supported for type ", type) );
    }

    @Override
    public long longValue() {
        return die(int.class, sputs("intValue not supported for type ", type) );
    }


    @Override
    public boolean booleanValue() {

        switch ( type ) {
            case FALSE:
                return false;
            case TRUE:
                return true;
        }
        Exceptions.die();
        return false;

    }


    @Override
    public String stringValue() {
        if (type == TypeType.NULL)  {
            return null;
        } else {
            return type.toString();
        }
    }

    @Override
    public String stringValue(CharBuf charBuf) {
        if (type == TypeType.NULL)  {
            return null;
        } else {
            return type.toString();
        }
    }

    @Override
    public String stringValueEncoded() {
        return toString();
    }


    public String toString() {
        return type.toString();
    }

    @Override
    public  Object toValue() {
        if ( value != null ) {
            return value;
        }
        switch ( type ) {
            case FALSE:
                return (value = false);

            case TRUE:
                return (value = true);
            case NULL:
                return null;
        }
        Exceptions.die();
        return null;

    }

    @Override
    public  <T extends Enum> T toEnum( Class<T> cls ) {
        return (T) value;
    }

    @Override
    public boolean isContainer() {
        return container;
    }

    @Override
    public void chop() {
    }

    @Override
    public char charValue () {
        return 0;
    }

    @Override
    public TypeType type() {
        return type;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt( int index ) {
        return '0';
    }

    @Override
    public CharSequence subSequence( int start, int end ) {
        return "";
    }


    @Override
    public Date dateValue() {
        return null;
    }


    public byte byteValue() {
           return 0;
    }

    public short shortValue() {
        return 0;
    }


    public BigDecimal bigDecimalValue() {
        return null;
    }

    public BigInteger bigIntegerValue() {
        return null;
    }


    @Override
    public double doubleValue() {
        return 0;
    }


    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public Currency currencyValue () {
        return null;
    }
}
