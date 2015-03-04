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

package io.advantageous.boon.core.reflection.fields;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Sets;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.Annotations;
import io.advantageous.boon.core.value.ValueContainer;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.core.Conversions.*;

public abstract class BaseField implements FieldAccess {

    private static final int PRIMITIVE = 0;
    private static final int FINAL = 1;
    private static final int STATIC = 2;
    private static final int VOLATILE =  3;
    private static final int QUALIFIED = 4;
    private static final int READ_ONLY = 5;
    private static final int INCLUDE = 6;
    private static final int IGNORE = 7;
    private static final int WRITE_ONLY = 8;
    private static final int HAS_INJECT = 9;
    private static final int NAMED = 10;

    private static final int REQUIRES_INJECTION = 11;

    protected final BitSet bits = new BitSet (  );
    protected final Class<?> type;
    protected final Class<?> parentType;
    protected final String name;
    protected final ParameterizedType parameterizedType;
    protected final Class<?> componentClass;
    protected final String typeName;
    public final TypeType typeEnum;
    private final TypeType componentType;
    private  Map<String,  Map<String, Object>> annotationMap = new ConcurrentHashMap<> (  );
    private  HashSet<String> includedViews;
    private  HashSet<String> ignoreWithViews;
    private final String alias;
    private static Set<String> annotationsThatHaveAliases = Sets.set("JsonProperty","SerializedName", "Named", "id", "In", "Qualifier" );



    private void initAnnotationData(Class clazz ) {

        final Collection<AnnotationData> annotationDataForFieldAndProperty =
                Annotations.getAnnotationDataForFieldAndProperty ( clazz, name, Collections.EMPTY_SET );

        for (AnnotationData data : annotationDataForFieldAndProperty) {
            annotationMap.put ( data.getSimpleClassName (), data.getValues () );
            annotationMap.put ( data.getFullClassName (), data.getValues () );
        }


        if (hasAnnotation ( "JsonViews" )) {
            final Map<String, Object> jsonViews = getAnnotationData ( "JsonViews" );
            final String[] includeWithViews = (String[]) jsonViews.get ( "includeWithViews" );
            final String[] ignoreWithViews = (String[]) jsonViews.get ( "ignoreWithViews" );


            if (includeWithViews!=null) {
                this.includedViews = new HashSet<>(  );
                for (String view : includeWithViews) {
                    this.includedViews.add( view );
                }
            }


            if (ignoreWithViews!=null) {
                this.ignoreWithViews = new HashSet<>(  );
                for (String view : ignoreWithViews) {
                    this.ignoreWithViews.add( view );
                }
            }
        }


        if (hasAnnotation ( "JsonIgnore" )) {
            final Map<String, Object> jsonIgnore = getAnnotationData ( "JsonIgnore" );
            boolean ignore = (Boolean) jsonIgnore.get ( "value" );
            bits.set( IGNORE, ignore);
        }

        if (hasAnnotation ( "JsonInclude" ))  {
            final Map<String, Object> jsonIgnore = getAnnotationData ( "JsonInclude" );
            String include = (String) jsonIgnore.get ( "value" );
            if ( include.equals ( "ALWAYS" ) ) {
                bits.set( INCLUDE );
            }
        }

        if (hasAnnotation ( "Expose" ))  {
            final Map<String, Object> jsonIgnore = getAnnotationData ( "Expose" );
            boolean serialize = (boolean) jsonIgnore.get ( "serialize" );
            bits.set( INCLUDE, serialize );
            bits.set( IGNORE, !serialize);
        }

        if (hasAnnotation ( "Inject" ) || hasAnnotation( "Autowired" ) || hasAnnotation( "In" ))  {
            bits.set( HAS_INJECT );
        }

        if (hasAnnotation( "Autowired" ))  {
            final Map<String, Object> props = getAnnotationData ( "Autowired" );

            boolean required = (boolean) props.get ( "required" );
            if (required)
            bits.set( REQUIRES_INJECTION );

        }

        if (hasAnnotation( "In" ))  {
            final Map<String, Object> props = getAnnotationData ( "In" );

            boolean required = (boolean) props.get ( "required" );
            if (required)
                bits.set( REQUIRES_INJECTION );

        }

        if (hasAnnotation( "Required" ))  {
            bits.set( REQUIRES_INJECTION );
        }





        if (parentType!=null) {
            final Map<String, AnnotationData> classAnnotations =
                Annotations.getAnnotationDataForClassAsMap( parentType );

            final AnnotationData jsonIgnoreProperties = classAnnotations.get( "JsonIgnoreProperties" );

            if (jsonIgnoreProperties!=null) {
                String[] props = (String [])jsonIgnoreProperties.getValues().get( "value" );
                if (props!=null) {
                   for (String prop : props) {
                       if (prop.equals ( name ) ) {
                           bits.set( IGNORE, true);
                           break;
                       }
                   }
                }
            }

        }


    }

    private String findAlias() {
        String alias = null;
        for (String aliasAnnotation : annotationsThatHaveAliases) {
            alias = getAlias(aliasAnnotation);
            if (! Str.isEmpty(alias)) {
                bits.set( NAMED );
                break;
            }
        }

        return Str.isEmpty( alias ) ? name : alias;
    }


    private String getAlias(String annotationName) {

        String alias = null;
        if ( hasAnnotation ( annotationName )) {

            Map<String, Object> aliasD = getAnnotationData ( annotationName );
            alias = (String) aliasD.get ( "value" );
        }

        return alias;
    }

    protected BaseField ( String name, Method getter, Method setter ) {

        try {
            if (setter==null) {
                bits.set(READ_ONLY);

            } else if (getter == null) {
                bits.set( WRITE_ONLY );
                bits.set( IGNORE );
            }

            this.name = name.intern();

            bits.set( VOLATILE,  false);
            bits.set( QUALIFIED, false);

            String alias;

            if ( getter != null ) {
                bits.set(STATIC,  Modifier.isStatic ( getter.getModifiers () ));
                bits.set(FINAL, Modifier.isFinal ( getter.getModifiers () ));

                type = getter.getReturnType ();
                parentType = getter.getDeclaringClass();
                bits.set( PRIMITIVE, type.isPrimitive ());
                typeName = type.getName().intern ();
                Object obj = getter.getGenericReturnType ();

                if ( obj instanceof ParameterizedType ) {

                    parameterizedType = ( ParameterizedType ) obj;
                } else {
                    parameterizedType = null;
                }


                if (name.startsWith ( "$" )) {
                    this.typeEnum = TypeType.SYSTEM;
                } else {
                    this.typeEnum = TypeType.getType(type);
                }

                if ( this.typeEnum.isArray()) {
                    componentClass = this.type.getComponentType();
                    componentType = TypeType.getType(componentClass);
                } else if ( parameterizedType == null ) {
                    componentClass = Object.class;
                    componentType = TypeType.OBJECT;
                }  else  {
                    Object obj2 = parameterizedType.getActualTypeArguments ()[ 0 ];
                    if (obj2 instanceof Class) {
                        componentClass = ( Class<?> ) parameterizedType.getActualTypeArguments ()[ 0 ];
                    }else {
                        componentClass=Object.class;
                    }

                    componentType = TypeType.getType(componentClass);

                }

                getter.setAccessible(true);

                initAnnotationData ( getter.getDeclaringClass () );

            } else {
                bits.set(STATIC,  Modifier.isStatic ( setter.getModifiers () ));
                bits.set(FINAL, Modifier.isFinal ( setter.getModifiers () ));
                type = setter.getParameterTypes ()[ 0 ];

                if (name.startsWith ( "$" )) {
                    this.typeEnum = TypeType.SYSTEM;
                } else {
                    this.typeEnum = TypeType.getType(type);
                }
                bits.set( PRIMITIVE, type.isPrimitive ());
                typeName = type.getName ().intern ();
                parameterizedType = null;
                componentClass = Object.class;
                componentType = TypeType.getType(componentClass);

                parentType = setter.getDeclaringClass();

                initAnnotationData ( setter.getDeclaringClass () );


            }


            alias = findAlias();



            this.alias = alias != null ? alias : name;

        } catch ( Exception ex ) {
            Exceptions.handle ( "name " + name + " setter " + setter + " getter " + getter, ex );
            throw new RuntimeException ( "die" );
        }



    }


    protected BaseField ( Field field ) {
        name = field.getName().intern ();

        bits.set(STATIC,  Modifier.isStatic ( field.getModifiers () ));
        bits.set(FINAL, Modifier.isFinal ( field.getModifiers () ));
        bits.set(VOLATILE, Modifier.isVolatile ( field.getModifiers () ));
        bits.set(QUALIFIED, bits.get(FINAL) || bits.get(VOLATILE));
        bits.set(READ_ONLY, bits.get(FINAL) );
        bits.set(IGNORE, Modifier.isTransient ( field.getModifiers () ) || Modifier.isStatic ( field.getModifiers () ));
//        bits.set(IGNORE, Modifier.isStatic ( field.getModifiers () ));

        parentType = field.getDeclaringClass();


        type = field.getType ();
        typeName = type.getName().intern ();

        bits.set(PRIMITIVE, type.isPrimitive ());

        if ( field != null ) {
            Object obj = field.getGenericType ();

            if ( obj instanceof ParameterizedType ) {

                parameterizedType = ( ParameterizedType ) obj;
            } else {
                parameterizedType = null;
            }

        } else {
            parameterizedType = null;
        }


        if (name.startsWith ( "$" )) {
            this.typeEnum = TypeType.SYSTEM;
        } else {
            this.typeEnum = TypeType.getType(type);
        }

        if ( this.typeEnum.isArray()) {

            componentClass = this.type.getComponentType();

        }
        else if ( parameterizedType == null ) {
            componentClass = Object.class;
        } else {


            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length>0) {

                Object obj = parameterizedType.getActualTypeArguments()[0];
                if (obj instanceof Class) {
                    componentClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                } else {
                    componentClass = Object.class;
                }
            } else {
                componentClass = Object.class;
            }

        }

        componentType = TypeType.getType(componentClass);




        initAnnotationData ( field.getDeclaringClass () );

        alias = findAlias();



    }


    @Override
    public final Object getValue ( Object obj ) {

        switch ( typeEnum ) {
            case INT:
                return this.getInt ( obj );
            case LONG:
                return this.getLong ( obj );
            case BOOLEAN:
                return this.getBoolean ( obj );
            case BYTE:
                return this.getByte ( obj );
            case SHORT:
                return this.getShort ( obj );
            case CHAR:
                return this.getChar ( obj );
            case DOUBLE:
                return this.getDouble ( obj );
            case FLOAT:
                return this.getFloat ( obj );
            default:
                return this.getObject ( obj );

        }
    }


    @Override
    public final void setValue ( Object obj, Object value ) {

        if (this.isPrimitive() && value == null) {
            return;
        }

        switch ( typeEnum ) {
            case INT:
                 this.setInt ( obj, toInt ( value ) );
                 return;
            case LONG:
                 this.setLong ( obj, toLong ( value ) );
                 return;
            case BOOLEAN:
                 this.setBoolean ( obj, toBoolean ( value ) );
                 return;
            case BYTE:
                 this.setByte ( obj, toByte ( value ) );
                 return;
            case SHORT:
                 this.setShort ( obj, toShort ( value ) );
                 return;
            case CHAR:
                 this.setChar ( obj, toChar ( value ) );
                 return;
            case DOUBLE:
                 this.setDouble ( obj, toDouble ( value ) );
                 return;
            case FLOAT:
                 this.setFloat ( obj, toFloat ( value ) );
                 return;
            case DATE:
                this.setObject ( obj, toDate ( value ) );
                return;

            case STRING:
                if (value instanceof String)  {
                    this.setObject ( obj, value );
                } else {
                    this.setObject ( obj, Conversions.toString (value) );
                }
                return;
            case ENUM:
                if ( value.getClass () == this.type ) {
                    this.setObject ( obj, value );
                } else {
                    this.setObject ( obj, toEnum ( (Class<? extends Enum>) type, value ) );
                }
                return;
            case BIG_DECIMAL:
                if ( value instanceof BigDecimal )  {
                    this.setObject ( obj, value );
                } else {
                    this.setObject ( obj, toBigDecimal ( value ) );
                }
                return;
            case NUMBER:
                if ( value instanceof Number )  {
                    this.setObject ( obj, value );
                } else {
                    this.setObject ( obj, toDouble ( value ) );
                }
                return;

            case BIG_INT:
                if ( value instanceof BigInteger )  {
                    this.setObject ( obj, value );
                } else {
                    this.setObject ( obj, toBigInteger ( value ) );
                }
                return;
            case COLLECTION:
            case LIST:
            case SET:
                 this.setObject ( obj, Conversions.toCollection ( type, value ) );
                 return;
            case CURRENCY:
                if ( value instanceof Currency )  {
                    this.setObject ( obj, value );
                } else {
                    this.setObject ( obj, toCurrency ( value ) );
                }
                return;

            default:
                if ( value == null ) {
                    this.setObject ( obj, null );
                    return;
                }

                if ( value.getClass () == this.type ) {
                        this.setObject ( obj, value );
                        return;
                } else if ( type.isInstance( value)) {
                        this.setObject ( obj, value );
                        return;
                } else {

                    Object object = Conversions.coerce( typeEnum, type, value );

                    if (object == null) {
                        die("Unable to convert", value, "to", typeEnum, type);
                        return;
                    }

                    if ( object.getClass () == this.type ) {
                        this.setObject ( obj, object );
                        return;
                    } else if ( type.isInstance( object)) {
                        this.setObject ( obj, object );
                        return;
                    }  else if (object != null) {
                                die(sputs("Unable to set value into field after conversion was called",
                                    this, "converted value", object, "original value", value, "field", this,
                                    "converted object type", object.getClass()
                                        ));


                    }
                }

        }

    }


    public final void setFromValue ( Object obj, Value value ) {


        if (value == ValueContainer.NULL) {
            this.setObject ( obj, null );
            return;
        }


        switch ( typeEnum ) {
            case INT:
                this.setInt ( obj, value.intValue()  );
                return;
            case LONG:
                this.setLong ( obj, value.longValue () );
                return;
            case BOOLEAN:
                this.setBoolean ( obj, value.booleanValue () );
                return;
            case BYTE:
                this.setByte ( obj, value.byteValue () );
                return;
            case SHORT:
                this.setShort ( obj, value.shortValue () );
                return;
            case CHAR:
                this.setChar ( obj, value.charValue () );
                return;
            case DOUBLE:
                this.setDouble ( obj, value.doubleValue () );
                return;
            case FLOAT:
                this.setFloat ( obj, value.floatValue () );
                return;
            case INTEGER_WRAPPER:
                this.setObject ( obj, value.intValue () );
                return;
            case LONG_WRAPPER:
                this.setObject ( obj, value.longValue () );
                return;
            case BOOLEAN_WRAPPER:
                this.setObject ( obj, value.booleanValue () );
                return;
            case BYTE_WRAPPER:
                this.setObject ( obj, value.byteValue () );
                return;
            case SHORT_WRAPPER:
                this.setObject ( obj, value.shortValue () );
                return;
            case CHAR_WRAPPER:
                this.setObject ( obj, value.charValue () );
                return;
            case DOUBLE_WRAPPER:
                this.setObject ( obj, value.doubleValue () );
                return;
            case FLOAT_WRAPPER:
                this.setObject ( obj, value.floatValue () );
                return;
            case STRING:
            case CHAR_SEQUENCE:
                this.setObject ( obj, value.stringValue() );
                return;
            case BIG_DECIMAL:
                this.setObject ( obj, value.bigDecimalValue () );
                return;
            case BIG_INT:
                this.setObject ( obj, value.bigIntegerValue () );
                return;
            case DATE:
                this.setObject ( obj, value.dateValue () );
                return;




            case ENUM:
                this.setObject ( obj, value.toEnum (  ( Class<? extends Enum> )type ) );
                return;
            case CURRENCY:
                this.setObject ( obj, value.currencyValue () );
                return;
            default:
                setValue(obj, value.toValue());
        }

    }


    public final ParameterizedType getParameterizedType () {

        return parameterizedType;

    }


    public final Class<?> getComponentClass () {
        return componentClass;
    }



    protected void analyzeError( Exception e, Object obj ) {
        Exceptions.handle( Str.lines (
                e.getClass ().getName (),
                String.format ( "cause %s", e.getCause () ),
                String.format ( "Field info name %s, type %s, class that declared field %s", this.name(), this.type(), this.getField ().getDeclaringClass () ),
                String.format ( "TypeType of object passed %s", obj.getClass ().getName () )
        ), e );

    }



    @Override
    public final boolean hasAnnotation ( String annotationName ) {
        return this.annotationMap.containsKey ( annotationName );
    }

    @Override
    public final Map<String, Object> getAnnotationData ( String annotationName ) {

        return this.annotationMap.get ( annotationName );
    }


    public final boolean isPrimitive() {
        return  bits.get ( PRIMITIVE );
    }






    @Override
    public final TypeType typeEnum () {
        return this.typeEnum;
    }



    @Override
    public final boolean isFinal() {
        return bits.get(FINAL);
    }


    @Override
    public final boolean isStatic() {


        return bits.get(STATIC);
    }

    @Override
    public final boolean isVolatile() {
        return bits.get(VOLATILE);
    }


    @Override
    public final boolean isQualified() {
        return bits.get( QUALIFIED );
    }

    @Override
    public final boolean isReadOnly() {
        return bits.get(READ_ONLY);
    }


    @Override
    public boolean isWriteOnly() {
        return bits.get(WRITE_ONLY);
    }


    @Override
    public final Class<?> type() {
        return type;
    }

    @Override
    public final String name() {
        return name;
    }



    @Override
    public final String alias() {
        return alias;
    }

    @Override
    public String toString() {
        return "FieldInfo [name=" + name
               + ", type=" + type
                + ", parentType=" + parentType

                + "]";
    }




    public final boolean isViewActive (String activeView) {

        if (this.includedViews == null && this.ignoreWithViews == null) {
            return true;
        }
        if (this.includedViews!=null) {
            if (this.includedViews.contains( activeView )) {
                return true;
            } else {
                return false;
            }
        }
        if (this.ignoreWithViews!=null) {
            if (this.ignoreWithViews.contains( activeView )) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public final  boolean include () {
        return bits.get( INCLUDE );
    }

    @Override
    public final boolean ignore () {
        return  bits.get( IGNORE );
    }


    @Override
    public boolean injectable() {
        return bits.get( HAS_INJECT );
    }

    @Override
    public boolean requiresInjection() {

        return bits.get( REQUIRES_INJECTION );
    }

    @Override
    public boolean isNamed() {

        return bits.get( NAMED );
    }

    @Override
    public boolean hasAlias() {
        return bits.get( NAMED );
    }



    @Override
    public String named() {
        return this.alias;
    }



    @Override
    public Object parent() {
        return this.parentType;
    }


    @Override
    public Class<?> declaringParent() {
        return this.parentType;
    }


    @Override
    public TypeType componentType() {
        return this.componentType;
    }


}
