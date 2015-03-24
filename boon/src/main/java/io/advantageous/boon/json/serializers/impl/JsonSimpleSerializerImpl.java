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


import io.advantageous.boon.Boon;
import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.core.reflection.Invoker;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.Maps;
import io.advantageous.boon.cache.SimpleCache;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.Reflection;
import io.advantageous.boon.primitive.CharBuf;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;

/**
 *
 * This is a simple fast serializer.
 * It excludes default values, i.e., int v by default is 0.
 * It excludes nulls and empties as well.
 */
public class JsonSimpleSerializerImpl implements JsonSerializerInternal {
    private final Map <Class<?>,  Map<String, FieldAccess>> fieldMap = new ConcurrentHashMap<>( );
    private final String view;
    private final boolean encodeStrings;

    private final boolean serializeAsSupport;


    private final CharBuf builder;

    private int level;

    private boolean asciiOnly;

    public JsonSimpleSerializerImpl() {

        this.view = null;
        this.encodeStrings = true;
        serializeAsSupport = true;
        builder = CharBuf.create( 4000 );


    }


    public JsonSimpleSerializerImpl(boolean encodeStrings) {

        this.view = null;
        this.encodeStrings = encodeStrings;

        serializeAsSupport = true;
        builder = CharBuf.create( 4000 );


    }




    public JsonSimpleSerializerImpl(String view, boolean encodeStrings,
                                    boolean serializeAsSupport, boolean asciiOnly) {

        this.encodeStrings = encodeStrings;
        this.serializeAsSupport = serializeAsSupport;
        this.view = view;
        builder = CharBuf.create( 4000 );
        this.asciiOnly = asciiOnly;

    }



    SimpleCache<String, String> stringCache;
    CharBuf encodedJsonChars;

    public final void serializeString( String str, CharBuf builder ) {



          if (encodeStrings) {

              if (stringCache == null) {
                  stringCache = new SimpleCache<>(1000);
                  encodedJsonChars = CharBuf.create(str.length());

              }

              String encodedString = stringCache.get(str);
              if (encodedString == null) {



                  encodedJsonChars.asJsonString(str, asciiOnly);
                  encodedString = encodedJsonChars.toStringAndRecycle();
                  stringCache.put(str, encodedString);
              }

              builder.add(encodedString);

          } else {
              builder.addQuoted(str);
          }

    }


    public CharBuf serialize( Object obj ) {
        level=0;

        builder.readForRecycle ();
        try {
            serializeObject( obj, builder );
        } catch ( Exception ex ) {
            return Exceptions.handle(CharBuf.class, "unable to serializeObject", ex);
        }
        return builder;
    }

    @Override
    public void serialize(CharBuf builder, Object obj) {
        level = 0;

        try {
            serializeObject( obj, builder );
        } catch ( Exception ex ) {
             Exceptions.handle("unable to serializeObject", ex);
        }
    }


    public final boolean serializeField ( Object parent, FieldAccess fieldAccess, CharBuf builder ) {



        final String fieldName = fieldAccess.name();
        final TypeType typeEnum = fieldAccess.typeEnum ();


        //try {


            if ( view!=null ){
                if (!fieldAccess.isViewActive( view ) ) {
                    return false;
                }
            }

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
                        builder.addQuoted( "" + cvalue  );
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
                    builder.addBigDecimal ( (BigDecimal) value );
                    return true;
                case NUMBER:
                    Number nvalue = (Number) fieldAccess.getObject(parent);
                    if ( nvalue.intValue() != 0 ) {
                        serializeFieldName ( fieldName, builder );
                        builder.addString(nvalue.toString());
                        return true;
                    }
                    return false;
                case BIG_INT:
                    serializeFieldName ( fieldName, builder );
                    builder.addBigInteger((BigInteger) value);
                    return true;
                case DATE:
                    serializeFieldName ( fieldName, builder );
                    serializeDate((Date) value, builder);
                    return true;
                case STRING:
                    serializeFieldName ( fieldName, builder );
                    serializeString((String) value, builder);
                    return true;
                case CLASS:
                    serializeFieldName ( fieldName, builder );
                    builder.addQuoted ( (( Class ) value).getName());
                    return true;

                case CHAR_SEQUENCE:
                    serializeFieldName ( fieldName, builder );
                    serializeString ( value.toString (), builder );
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
                    builder.addQuoted ( (( Character ) value).toString() );
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
                        this.serializeCollection ( collection, builder );
                        return true;
                    }
                    return false;
                case MAP:
                    Map map = (Map) value;
                    if ( map.size () > 0) {
                        serializeFieldName ( fieldName, builder );
                        this.serializeMap ( map, builder );
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
                    if (value.getClass().isArray()) {
                        if ( Array.getLength (value) > 0) {
                            serializeFieldName ( fieldName, builder );
                            this.serializeArray ( fieldAccess.componentType(), value, builder );
                            return true;
                        }
                    }
                    return false;

                case INTERFACE:
                case ABSTRACT:
                    serializeFieldName ( fieldName, builder );
                    serializeSubtypeInstance ( value, builder );
                    return true;

                case OBJECT:
                    serializeFieldName ( fieldName, builder );
                    final TypeType instanceType = TypeType.getInstanceType(value);
                    if (instanceType== TypeType.INSTANCE) {
                        serializeSubtypeInstance(value, builder);
                    } else {
                        serializeObject(value, builder);
                    }
                    return true;

                case INSTANCE:
                    serializeFieldName ( fieldName, builder );
                    serializeInstance ( value, builder );
                    return true;

                case SYSTEM:
                    return false;


                case TIME_ZONE:

                    serializeFieldName ( fieldName, builder );
                    TimeZone zone = (TimeZone) value;

                    builder.addQuoted ( zone.getID() );
                    return true;

                case CURRENCY:
                    serializeFieldName ( fieldName, builder );
                    serializeCurrency ( (Currency) value, builder );
                    return true;

                default:
                    serializeFieldName ( fieldName, builder );
                    serializeUnknown(value, builder);
                    return true;
            }

    }




    public final void serializeDate ( Date date, CharBuf builder ) {
        builder.addLong(date.getTime ());
    }


    public final void serializeCurrency ( Currency currency, CharBuf builder ) {
        builder.addChar ( '"' );
        builder.addString(currency.getCurrencyCode());
        builder.addChar ( '"' );
    }

    public final void serializeObject( Object obj, CharBuf builder )  {


        TypeType type = TypeType.getInstanceType(obj);

        switch ( type ) {

            case NULL:
                builder.addNull();
                return;
            case INT:
                builder.addInt ( int.class.cast ( obj ) );
                return;
            case BOOLEAN:
                builder.addBoolean ( boolean.class.cast ( obj ) );
                return;
            case BYTE:
                builder.addByte ( byte.class.cast ( obj ) );
                return;
            case LONG:
                builder.addLong ( long.class.cast ( obj ) );
                return;
            case DOUBLE:
                builder.addDouble ( double.class.cast ( obj ) );
                return;
            case FLOAT:
                builder.addFloat ( float.class.cast ( obj ) );
                return;
            case SHORT:
                builder.addShort ( short.class.cast ( obj ) );
                return;
            case CHAR:
                builder.addChar ( char.class.cast ( obj ) );
                return;
            case BIG_DECIMAL:
                builder.addBigDecimal ( ( BigDecimal ) obj );
                return;
            case BIG_INT:
                builder.addBigInteger ( ( BigInteger ) obj );
                return;
            case DATE:
                serializeDate ( ( Date ) obj, builder );
                return;
            case CLASS:
                builder.addQuoted ( (( Class ) obj).getName() );
                return;

            case STRING:
                serializeString ( ( String ) obj, builder );
                return;
            case CHAR_SEQUENCE:
                serializeString ( obj.toString(), builder );
                return;
            case BOOLEAN_WRAPPER:
                builder.addBoolean ( ( Boolean ) obj );
                return;
            case INTEGER_WRAPPER:
                builder.addInt ( (Integer) obj);
                return;
            case LONG_WRAPPER:
                builder.addLong ( (Long) obj);
                return;
            case FLOAT_WRAPPER:
                builder.addFloat ( (Float) obj);
                return;
            case DOUBLE_WRAPPER:
                builder.addDouble ( (Double) obj);
                return;
            case SHORT_WRAPPER:
                builder.addShort ( (Short) obj);
                return;
            case BYTE_WRAPPER:
                builder.addByte ( (Byte) obj);
                return;
            case CHAR_WRAPPER:
                builder.addChar ( (Character) obj);
                return;
            case ENUM:
                builder.addQuoted ( obj.toString () );
                return;

            case TIME_ZONE:
                TimeZone zone = (TimeZone) obj;

                builder.addQuoted ( zone.getID() );
                return;


            case COLLECTION:
            case LIST:
            case SET:
                this.serializeCollection ( (Collection) obj, builder );
                return;
            case MAP:
                this.serializeMap ( ( Map ) obj, builder );
                return;


            case ARRAY:
            case ARRAY_INT:
            case ARRAY_BYTE:
            case ARRAY_SHORT:
            case ARRAY_FLOAT:
            case ARRAY_DOUBLE:
            case ARRAY_LONG:
            case ARRAY_STRING:
            case ARRAY_OBJECT:
                this.serializeArray ( obj, builder );
                return;
            case ABSTRACT:
            case INTERFACE:
                serializeSubtypeInstance ( obj, builder );
                return;

            case VALUE:
                Value value = (Value) obj;
                serializeObject( value.toValue(), builder );
                return;

            case INSTANCE:
                serializeInstance ( obj, builder );
                return;
            case CURRENCY:
                serializeCurrency ( ( Currency ) obj, builder );
                return;
            default:

                    this.serializeUnknown(obj, builder);


        }


    }


    public void serializeUnknown ( Object obj, CharBuf builder ) {
        //try {
            builder.addQuoted ( obj.toString () );
        //} catch (Exception ex) {
           //unknown so... TODO log
           //
        //}
    }



    public final void serializeInstance ( Object instance, CharBuf builder )  {

        try {

            level++;

            if (level > 100) {
                die("Detected circular dependency", builder.toString());
            }

            if (serializeAsSupport && Reflection.respondsTo(instance, "serializeAs")) {
                serializeObject(Invoker.invoke(instance, "serializeAs"), builder);
                return;
            }

            final Collection<FieldAccess> fields = getFields(instance.getClass()).values();


            builder.addChar( '{' );

            int index = 0;
            for ( FieldAccess fieldAccess : fields ) {
                 if (serializeField ( instance, fieldAccess, builder ) ) {
                     builder.add(',');
                     index++;
                 }
            }
            if ( index > 0 ) {
                builder.removeLastChar();
            }
            builder.addChar( '}' );


        } catch (Exception ex) {
            Exceptions.handle(ex, "serialize instance", instance,
                    "class name of instance", Boon.className(instance),
                    "obj", instance);
        } finally {
            level--;
        }

    }

    @Override
    public void serializeInstance(Object obj, CharBuf builder, boolean includeTypeInfo) {

        this.serializeInstance(obj, builder);

    }


    public Map<String, FieldAccess> getFields( Class<? extends Object> aClass ) {
            Map<String, FieldAccess> map = fieldMap.get( aClass );
            if (map == null) {
                map = doGetFields ( aClass );
                fieldMap.put ( aClass, map );
            }
            return map;

   }

    private final Map<String, FieldAccess> doGetFields ( Class<? extends Object> aClass ) {
        Map<String, FieldAccess> fields =  Maps.copy ( Reflection.getPropertyFieldAccessMapFieldFirstForSerializer ( aClass ) );

        List<FieldAccess> removeFields = new ArrayList<>();

        for (FieldAccess field : fields.values()) {
            if (field.isWriteOnly ())  {
                removeFields.add(field);
            }
        }

        for (FieldAccess fieldAccess : removeFields) {
            fields.remove(fieldAccess.name());
        }
        return fields;
    }


    private static final char [] EMPTY_MAP_CHARS = {'{', '}'};

    public final void serializeMap( Map<Object, Object> smap, CharBuf builder )  {

        Map map = smap;
        if ( map.size () == 0 ) {
            builder.addChars ( EMPTY_MAP_CHARS );
            return;
        }


        builder.addChar( '{' );

        int index=0;
        final Set<Map.Entry> entrySet = map.entrySet();
        for ( Map.Entry entry : entrySet ) {
            if (entry.getValue ()!=null ) {
                serializeFieldName ( Str.toString(entry.getKey()), builder );
                serializeObject( entry.getValue(), builder );
                builder.addChar ( ',' );
                index++;
            }
        }
        if (index>0)
        builder.removeLastChar ();
        builder.addChar( '}' );

    }


    public final void serializeArray ( TypeType componentType, Object objectArray, CharBuf builder ) {


        switch (componentType) {
            case STRING: //optimization
                String[] array = (String[]) objectArray;
                final int length = array.length;

                builder.addChar( '[' );
                for ( int index = 0; index < length; index++ ) {
                        serializeString( array[index], builder );
                        builder.addChar ( ',' );
                }
                builder.removeLastChar ();
                builder.addChar( ']' );
                break;
            default:
                serializeArray(objectArray, builder);
        }
    }
    @Override
    public final void serializeArray ( Object array, CharBuf builder ) {

        if ( Array.getLength (array) == 0 ) {
            builder.addChars ( EMPTY_LIST_CHARS );
            return;
        }

        builder.addChar( '[' );
        final int length = Array.getLength( array );
        for ( int index = 0; index < length; index++ ) {
            serializeObject( Array.get( array, index ), builder );
            builder.addChar ( ',' );
        }
        builder.removeLastChar ();
        builder.addChar( ']' );

    }

    private void serializeFieldName ( String name, CharBuf builder ) {
            builder.addJsonFieldName ( FastStringUtils.toCharArray(name) );
    }


    private static final char [] EMPTY_LIST_CHARS = {'[', ']'};

    public final void serializeCollection( Collection<?> collection, CharBuf builder )  {

        if ( collection.size () == 0 ) {
             builder.addChars ( EMPTY_LIST_CHARS );
             return;
        }

        builder.addChar( '[' );
        for ( Object o : collection ) {
            if (o == null) {
                builder.addNull();
            } else {
                serializeObject(o, builder);
            }
            builder.addChar ( ',' );

        }
        builder.removeLastChar ();
        builder.addChar( ']' );

    }





    @Override
    public void serializeSubtypeInstance( Object instance, CharBuf builder ) {

        level++;

        if (level > 100) {
            die("Detected circular dependency", builder.toString());
        }

        final Map<String, FieldAccess> fieldAccessors = getFields(instance.getClass ());
        final Collection<FieldAccess> values = fieldAccessors.values ();

        builder.addString( "{\"class\":" );
        builder.addQuoted ( instance.getClass ().getName () );

        int index = 0;
        int length = values.size();

        if ( length > 0 ) {
            builder.addChar( ',' );

            for ( FieldAccess fieldAccess : values ) {
                if (serializeField ( instance, fieldAccess, builder ) ) {
                    builder.addChar ( ',' );
                    index++;
                }
            }
            if ( index > 0 ) {
                builder.removeLastChar();
            }
            builder.addChar( '}' );

        }

        level--;
    }



}

