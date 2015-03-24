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

package io.advantageous.boon.json.serializers.impl;

import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.json.serializers.FieldSerializer;
import io.advantageous.boon.primitive.CharBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by rick on 1/1/14.
 */
public class FieldSerializerImpl implements FieldSerializer {


    private void serializeFieldName ( String name, CharBuf builder ) {
        builder.addJsonFieldName ( FastStringUtils.toCharArray(name) );
    }

    @Override
    public final boolean serializeField ( JsonSerializerInternal serializer, Object parent, FieldAccess fieldAccess, CharBuf builder ) {

        final String fieldName = fieldAccess.name();
        final TypeType typeEnum = fieldAccess.typeEnum ();
        switch ( typeEnum ) {
            case INT:
                int value = fieldAccess.getInt ( parent );
                if (value !=0) {
                    serializeFieldName ( fieldName, builder );
                    builder.addInt ( value  );
                    return true;
                }
                return false;
            case BOOLEAN:
                boolean bvalue = fieldAccess.getBoolean ( parent );
                if ( bvalue ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addBoolean ( bvalue  );
                    return true;
                }
                return false;

            case BYTE:
                byte byvalue = fieldAccess.getByte ( parent );
                if ( byvalue != 0 ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addByte ( byvalue  );
                    return true;
                }
                return false;
            case LONG:
                long lvalue = fieldAccess.getLong ( parent );
                if ( lvalue != 0 ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addLong ( lvalue  );
                    return true;
                }
                return false;
            case DOUBLE:
                double dvalue = fieldAccess.getDouble ( parent );
                if ( dvalue != 0 ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addDouble ( dvalue  );
                    return true;
                }
                return false;
            case FLOAT:
                float fvalue = fieldAccess.getFloat ( parent );
                if ( fvalue != 0 ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addFloat ( fvalue  );
                    return true;
                }
                return false;
            case SHORT:
                short svalue = fieldAccess.getShort( parent );
                if ( svalue != 0 ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addShort ( svalue  );
                    return true;
                }
                return false;
            case CHAR:
                char cvalue = fieldAccess.getChar( parent );
                if ( cvalue != 0 ) {
                    serializeFieldName ( fieldName, builder );
                    builder.addChar( cvalue  );
                    return true;
                }
                return false;

        }

        Object value = fieldAccess.getObject ( parent );

        if ( value == null ) {
            return false;
        }

        /* Avoid back reference and infinite loops. */
        if ( value == parent ) {
            return false;
        }

        switch ( typeEnum )  {
            case BIG_DECIMAL:
                serializeFieldName ( fieldName, builder );
                builder.addBigDecimal ( (BigDecimal ) value );
                return true;
            case BIG_INT:
                serializeFieldName ( fieldName, builder );
                builder.addBigInteger ( ( BigInteger ) value );
                return true;
            case DATE:
                serializeFieldName ( fieldName, builder );
                serializer.serializeDate ( ( Date ) value, builder );
                return true;
            case STRING:
                serializeFieldName ( fieldName, builder );
                serializer.serializeString ( ( String ) value, builder );
                return true;
            case CLASS:
                serializeFieldName ( fieldName, builder );
                builder.addQuoted ( (( Class ) value).getName());
                return true;

            case TIME_ZONE:

                serializeFieldName ( fieldName, builder );
                TimeZone zone = (TimeZone) value;

                builder.addQuoted ( zone.getID() );
                return true;
            case CHAR_SEQUENCE:
                serializeFieldName ( fieldName, builder );
                serializer.serializeString ( value.toString (), builder );
                return true;
            case NUMBER:
                serializeFieldName ( fieldName, builder );
                builder.addString (  value.toString() );
                return true;



            case BOOLEAN_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addBoolean ( ( Boolean ) value );
                return true;
            case INTEGER_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addInt ( ( Integer ) value );
                return true;
            case LONG_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addLong ( ( Long ) value );
                return true;
            case FLOAT_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addFloat ( ( Float ) value );
                return true;
            case DOUBLE_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addDouble ( ( Double ) value );
                return true;
            case SHORT_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addShort ( ( Short ) value );
                return true;
            case BYTE_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addByte ( ( Byte ) value );
                return true;
            case CHAR_WRAPPER:
                serializeFieldName ( fieldName, builder );
                builder.addChar ( ( Character ) value );
                return true;
            case ENUM:
                serializeFieldName ( fieldName, builder );
                builder.addQuoted ( value.toString () );
                return true;
            case COLLECTION:
            case LIST:
            case SET:
                Collection collection = (Collection) value;
                if ( collection.size () > 0) {
                    serializeFieldName ( fieldName, builder );
                    serializer.serializeCollection ( collection, builder );
                    return true;
                }
                return false;
            case MAP:
                Map map = (Map) value;
                if ( map.size () > 0) {
                    serializeFieldName ( fieldName, builder );
                    serializer.serializeMap ( map, builder );
                    return true;
                }
                return false;


            case ARRAY:
            case ARRAY_INT:
            case ARRAY_BYTE:
            case ARRAY_SHORT:
            case ARRAY_FLOAT:
            case ARRAY_DOUBLE:
            case ARRAY_LONG:
            case ARRAY_STRING:
            case ARRAY_OBJECT:
                Object []  array  = (Object []) value;
                if ( array.length > 0) {
                    serializeFieldName ( fieldName, builder );
                    serializer.serializeArray ( value, builder );
                    return true;
                }
                return false;



            case INTERFACE:
            case ABSTRACT:
                serializeFieldName ( fieldName, builder );
                serializer.serializeSubtypeInstance ( value, builder );
                return true;

            case INSTANCE:
                serializeFieldName ( fieldName, builder );
                if ( fieldAccess.type() == value.getClass () ) {
                    serializer.serializeInstance ( value, builder );
                } else {
                    serializer.serializeSubtypeInstance ( value, builder );
                }
                return true;

            case CURRENCY:
                serializeFieldName ( fieldName, builder );
                builder.addCurrency ( (Currency ) value );
                return true;

            default:
                serializeFieldName ( fieldName, builder );
                serializer.serializeUnknown ( value, builder );
                return true;
        }

    }
}
