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

package io.advantageous.boon.core.reflection;

import io.advantageous.boon.*;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.primitive.CharBuf;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.className;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.*;
import static io.advantageous.boon.Str.lines;
import static io.advantageous.boon.StringScanner.isDigits;

/**
 * Created by rick on 12/20/13.
 */
public class BeanUtils {

    /**
     * This method handles walking lists of lists.
     *
     * @param item item
     * @param path path
     * @return value at this path
     */
    public static Object getPropByPath( Object item, String... path ) {

        Object o = item;
        for ( int index = 0; index < path.length; index++ ) {
            String propName = path[ index ];
            if ( o == null ) {
                return null;
            } else if ( o.getClass().isArray() || o instanceof Collection ) {


                o = getCollectionProp(o, propName, index, path);
                break;
            } else {
                o = getProp( o, propName );
            }
        }

        return Conversions.unifyListOrArray(o);
    }


    /**
     * This returns getPropertyFieldFieldAccessMap(clazz, true, true);
     *
     * @param clazz gets the properties or fields of this class.
     * @return name/field mapping
     */
    private static Map<String, FieldAccess> getPropertyFieldAccessMap( Class<?> clazz ) {
        return Reflection.getPropertyFieldAccessMapFieldFirst( clazz );
    }


    public static  FieldAccess getField( Class clazz, String name ) {

        Map<String, FieldAccess> fields = getPropertyFieldAccessMap( clazz );
        if ( fields != null) {
            return fields.get(name);
        } else {
            return null;
        }
    }


    public static  FieldAccess getField( Object object, String name ) {

        Map<String, FieldAccess> fields = getFieldsFromObject( object );
        if ( fields != null) {
            return fields.get(name);
        } else {
            return null;
        }
    }

    public static Map<String, FieldAccess> getFieldsFromObject( Class<?> cls ) {
        return getPropertyFieldAccessMap( cls );
    }

    /**
     * Get fields from object or Map.
     * Allows maps to act like they have fields.
     *
     * @param object object
     * @return names/fields mapping
     */
    public static Map<String, FieldAccess> getFieldsFromObject( Object object ) {

        try {
           Map<String, FieldAccess> fields;

            if ( object instanceof Map ) {

                fields = getFieldsFromMap( ( Map<String, Object> ) object );
            } else {
                fields = getPropertyFieldAccessMap( object.getClass() );

            }


           return fields;
        } catch (Exception ex) {
           Exceptions.requireNonNull(object, "Item cannot be null");
           return handle(Map.class, ex, "Unable to get fields from object", className(object));
        }

    }


    /**
     * Get fields from map.
     *
     * @param map map
     * @return fields
     */
    private static Map<String, FieldAccess> getFieldsFromMap(final Map<String, Object> map ) {


        return new Map<String, FieldAccess>() {
            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return map.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return true;
            }

            @Override
            public FieldAccess get(final Object key) {
                return new FieldAccess() {
                    @Override
                    public boolean injectable() {
                        return false;
                    }

                    @Override
                    public boolean requiresInjection() {
                        return false;
                    }

                    @Override
                    public boolean isNamed() {
                        return false;
                    }

                    @Override
                    public boolean hasAlias() {
                        return false;
                    }

                    @Override
                    public String alias() {
                        return null;
                    }

                    @Override
                    public String named() {
                        return key.toString();
                    }

                    @Override
                    public String name() {
                        return key.toString();
                    }

                    @Override
                    public Object getValue(Object obj) {
                        return map.get(key);
                    }

                    @Override
                    public void setValue(Object obj, Object value) {
                        map.put(key.toString(), value);
                    }

                    @Override
                    public void setFromValue(Object obj, Value value) {
                        map.put(key.toString(), value.toValue());
                    }

                    @Override
                    public boolean getBoolean(Object obj) {
                        return Conversions.toBoolean(getValue(key));
                    }

                    @Override
                    public void setBoolean(Object obj, boolean value) {

                        setValue(map, value);

                    }

                    @Override
                    public int getInt(Object obj) {

                        return Conversions.toInt(getValue(key));
                    }

                    @Override
                    public void setInt(Object obj, int value) {

                        setValue(map, value);
                    }

                    @Override
                    public short getShort(Object obj) {

                        return Conversions.toShort(getValue(key));
                    }

                    @Override
                    public void setShort(Object obj, short value) {

                        setValue(map, value);

                    }

                    @Override
                    public char getChar(Object obj) {

                        return Conversions.toChar(getValue(key));
                    }

                    @Override
                    public void setChar(Object obj, char value) {

                        setValue(map, value);

                    }

                    @Override
                    public long getLong(Object obj) {

                        return Conversions.toChar(getValue(key));
                    }

                    @Override
                    public void setLong(Object obj, long value) {

                        setValue(map, value);
                    }

                    @Override
                    public double getDouble(Object obj) {

                        return Conversions.toDouble(getValue(key));
                    }

                    @Override
                    public void setDouble(Object obj, double value) {


                        setValue(map, value);

                    }

                    @Override
                    public float getFloat(Object obj) {

                        return Conversions.toFloat(getValue(key));
                    }

                    @Override
                    public void setFloat(Object obj, float value) {

                        setValue(map, value);

                    }

                    @Override
                    public byte getByte(Object obj) {

                        return Conversions.toByte(getValue(key));
                    }

                    @Override
                    public void setByte(Object obj, byte value) {

                        setValue(map, value);

                    }

                    @Override
                    public Object getObject(Object obj) {

                        return getValue(obj);
                    }

                    @Override
                    public void setObject(Object obj, Object value) {

                        this.setValue(obj, value);
                    }

                    @Override
                    public TypeType typeEnum() {
                        return TypeType.OBJECT;
                    }

                    @Override
                    public boolean isPrimitive() {
                        return false;
                    }

                    @Override
                    public boolean isFinal() {
                        return false;
                    }

                    @Override
                    public boolean isStatic() {
                        return false;
                    }

                    @Override
                    public boolean isVolatile() {
                        return false;
                    }

                    @Override
                    public boolean isQualified() {
                        return false;
                    }

                    @Override
                    public boolean isReadOnly() {
                        return false;
                    }

                    @Override
                    public boolean isWriteOnly() {
                        return false;
                    }

                    @Override
                    public Class<?> type() {
                        return Object.class;
                    }

                    @Override
                    public Class<?> declaringParent() {
                        return null;
                    }

                    @Override
                    public Object parent() {
                        return map;
                    }

                    @Override
                    public Field getField() {
                        return null;
                    }

                    @Override
                    public boolean include() {
                        return true;
                    }

                    @Override
                    public boolean ignore() {
                        return false;
                    }

                    @Override
                    public ParameterizedType getParameterizedType() {
                        return null;
                    }

                    @Override
                    public Class<?> getComponentClass() {
                        return null;
                    }

                    @Override
                    public boolean hasAnnotation(String annotationName) {
                        return false;
                    }

                    @Override
                    public Map<String, Object> getAnnotationData(String annotationName) {
                        return null;
                    }

                    @Override
                    public boolean isViewActive(String activeView) {
                        return false;
                    }

                    @Override
                    public void setStaticValue(Object newValue) {

                    }

                    @Override
                    public TypeType componentType() {
                        return null;
                    }
                };
            }

            @Override
            public FieldAccess put(String key, FieldAccess value) {
                return null;
            }

            @Override
            public FieldAccess remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ? extends FieldAccess> m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set<String> keySet() {
                return null;
            }

            @Override
            public Collection<FieldAccess> values() {
                return null;
            }

            @Override
            public Set<Entry<String, FieldAccess>> entrySet() {
                return null;
            }
        };


    }


    /**
     * Get property value, loads nested properties
     *
     * @param root root
     * @param properties properties forming a path
     * @param newValue  new value
     */
    public static void setPropertyValue( final Object root,
                                         final Object newValue,
                                         final String... properties ) {

        Object object = root;

        int index = 0;


        try {

            for ( String property : properties ) {


                Map<String, FieldAccess> fields = getFieldsFromObject( object );
                FieldAccess field = fields.get( property );


                if ( isDigits( property ) ) {
                    /* We can index numbers and names. */
                    object = idx ( object, StringScanner.parseInt(property) );

                } else {

                    if ( field == null ) {
                        die( sputs(
                                "We were unable to access property=", property,
                                "\nThe properties passed were=", properties,
                                "\nThe root object is =", root.getClass().getName(),
                                "\nThe current object is =", object.getClass().getName()
                        )
                        );
                    }


                    if ( index == properties.length - 1 ) {
                        field.setValue( object, newValue );
                    } else {
                        object = field.getObject( object );
                    }
                }

                index++;
            }
        } catch (Exception ex) {
            Exceptions.requireNonNull(root, "Root cannot be null");
            handle(ex, "Unable to set property for root object", className(root),
                    "for property path", properties, "with new value", newValue,
                    "last object in the tree was",
                    className(object), "current property index", index);
        }

    }



    /**
     * Get property value, loads nested properties
     *
     * @param root root class
     * @param newValue new value
     * @param properties properties forming a path
     */
    public static void setPropertyValue( final Class<?> root, final Object newValue, final String... properties ) {

        Object object = root;

        int index = 0;


        Map<String, FieldAccess> fields = getFieldsFromObject( root );


        try {

            for ( String property : properties ) {

                FieldAccess field = fields.get( property );


                if ( isDigits( property ) ) {
                    /* We can index numbers and names. */
                    object = idx ( object, StringScanner.parseInt ( property ) );

                } else {

                    if ( field == null ) {
                        die( sputs(
                                "We were unable to access property=", property,
                                "\nThe properties passed were=", properties,
                                "\nThe root object is =", root.getClass().getName(),
                                "\nThe current object is =", object.getClass().getName()
                        )
                        );
                    }


                    if ( index == properties.length - 1 ) {
                        if (object instanceof Class) {
                            field.setStaticValue( newValue );
                        } else {
                            field.setValue( object, newValue );

                        }
                    } else {
                        object = field.getObject( object );
                        if (object != null) {
                            fields = getFieldsFromObject( root );
                        }
                    }
                }

                index++;
            }
        } catch (Exception ex) {
            Exceptions.requireNonNull(root, "Root cannot be null");
            handle(ex, "Unable to set property for root object", className(root),
                    "for property path", properties, "with new value", newValue,
                    "last object in the tree was",
                    className(object), "current property index", index);
        }

    }



        /**
         * Get property value, loads nested properties
         *
         * @param root root
         * @param properties properties forming a path
         * @return value at path
         */
    public static Object getPropertyValue( final Object root, final String... properties ) {



        Object object = root;



        for ( String property : properties ) {

            if (object == null) {
                return null;
            }

            if (property.equals("this")) {
                if (!(object instanceof Map)) {
                    continue;
                } else {
                    Object aThis = ((Map) object).get("this");
                    if (aThis!=null) {
                        object = aThis;
                        continue;
                    } else {
                        continue;
                    }
                }
            }

            if (object instanceof Map) {
                object = ((Map) object).get(property);
                continue;
            }


            char c = property.charAt(0);
            if ( CharScanner.isDigit(c) ) {
                /* We can index numbers and names. */
                object = idx ( object, StringScanner.parseInt ( property ) );

            } else {


                if (object instanceof Collection) {

                    object = _getFieldValuesFromCollectionOrArray( object, property);
                    continue;
                } else if (Typ.isArray(object)) {


                    Iterator iter = Conversions.iterator(object);
                    List list = Lists.list(iter);
                    object = _getFieldValuesFromCollectionOrArray(list, property);

                    continue;
                }


                Map<String, FieldAccess> fields =
                         getPropertyFieldAccessMap( object.getClass() );

                FieldAccess field = fields.get( property );

                if ( field == null ) {
                    return null;
                }

                object = field.getValue( object );
            }
        }
        return object;
    }

    /**
     * Get property value, loads nested properties
     *
     * @param root root
     * @param property property
     * @return type at this prop path
     */
    public static Class<?> getPropertyType( final Object root, final String property ) {

        Map<String, FieldAccess> fields = getPropertyFieldAccessMap( root.getClass() );

        FieldAccess field = fields.get( property );
        return field.type();
    }


    @SuppressWarnings ( "unchecked" )
    public static <T> T idxGeneric( Class<T> t, Object object, final String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return ( T ) getPropertyValue( object, properties );


    }

    public static <T> List<T> idxList( Class<T> cls, Object items, String path ) {

        String[] properties = propertyPathAsStringArray(path);
        return ( List<T> ) getPropByPath( items, properties );
    }

    public static List idxList( Object items, String path ) {

        String[] properties = propertyPathAsStringArray(path);
        return ( List ) getPropByPath( items, properties );
    }

    public static <T> List<T> idxRecurse( Class<T> cls, Object items, String path ) {

        String[] properties = propertyPathAsStringArray(path);
        return ( List<T> ) getPropByPath( items, properties );
    }

    public static List idxRecurse( Object items, String path ) {

        String[] properties = propertyPathAsStringArray(path);
        return ( List ) getPropByPath( items, properties );
    }




    /**
     * Get property value
     *
     * @param object object
     * @param path   in dotted notation
     * @return value
     */
    public static Object idx( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyValue( object, properties );
    }


    /**
     * Get property value
     *
     * @deprecated use atIndex or idx.
     * @param object object
     * @param path   in dotted notation
     * @return value at index
     */
    public static Object indexOf(Object object, String path) {
        return atIndex(object, path);
    }




    /**
     * Get property value
     *
     * @param object object
     * @param path   in dotted notation
     * @return value at index
     */
    public static Object atIndex(Object object, String path) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyValue( object, properties );
    }

    static Map<String, String[]> splitsPathsCache = new ConcurrentHashMap<>();

    private static String[] propertyPathAsStringArray(String path) {
        String[] split = splitsPathsCache.get(path);
        if (split!=null) {
            return split;
        }


        split =  StringScanner.splitByCharsNoneEmpty(path, '.', '[', ']', '/');
        splitsPathsCache.put(path, split);
        return split;
    }


    public static Object findProperty(Object context, String propertyPath) {


        int index = propertyPath.indexOf('|');

        Object defaultValue;

        if (index!=-1) {

            String[] splitByPipe = Str.splitByPipe(propertyPath);
            defaultValue = splitByPipe[1];
            propertyPath = splitByPipe[0];

        } else {
            defaultValue = null;
        }

        Object object;
        Iterator iterator = Conversions.iterator(context);
        while (iterator.hasNext()) {
                Object ctx = iterator.next();
                object = idx(ctx, propertyPath);
                if (object != null) {

                    if (object instanceof List) {
                        List list = (List) object;
                        int nulls = 0;
                        for (Object o : list) {
                            if (o == null) {
                                nulls++;
                            }
                        }
                        if (nulls == list.size()) {
                            break;
                        }
                    }
                    return object;
                }
        }

        return defaultValue;

    }

    /**
     * Set property value to simulate dependency injection.
     *
     * @param object object
     * @param path   in dotted notation
     * @param value  value
     */
    public static void injectIntoProperty( Object object, String path, Object value ) {


        String[] properties = propertyPathAsStringArray(path);

        setPropertyValue( object, value, properties );
    }

    /**
     * Set property value
     *
     * @param object object
     * @param path   in dotted notation
     * @param value value
     */
    public static void idx( Object object, String path, Object value ) {


        String[] properties = propertyPathAsStringArray(path);

        setPropertyValue( object, value, properties );
    }

    /**
     * Set a static value
     *
     * @param cls class
     * @param path   in dotted notation
     * @param value value
     */
    public static void idx( Class<?> cls, String path, Object value ) {


        String[] properties = propertyPathAsStringArray(path);

        setPropertyValue( cls, value, properties );
    }


    /**
     * This is an amazing little recursive method. It walks a fanout of
     * nested collection to pull out the leaf nodes
     *
     * @param o o
     * @param propName property name
     * @param index index
     * @param path path
     * @return value which could be a collection
     */
    private static Object getCollectionProp(Object o, String propName, int index, String[] path
                                           ) {
        o = _getFieldValuesFromCollectionOrArray(o, propName);

        if ( index + 1 == path.length ) {
            return o;
        } else {
            index++;
            return getCollectionProp(o, path[index], index, path);
        }
    }


    /**
     * This is one is forgiving of null paths.
     * This works with getters first, i.e., properties.
     *
     * @param object object
     * @param property property
     * @return value
     */
    public static Object getProp( Object object, final String property ) {
        if ( object == null ) {
            return null;
        }

        if ( isDigits( property ) ) {
                /* We can index numbers and names. */
            object = idx(object, StringScanner.parseInt(property));

        }

        Class<?> cls = object.getClass();

        /** Tries the getters first. */
        Map<String, FieldAccess> fields = Reflection.getPropertyFieldAccessors( cls );

        if ( !fields.containsKey( property ) ) {
            fields = Reflection.getAllAccessorFields( cls );
        }

        if ( !fields.containsKey( property ) ) {
            return null;
        } else {
            return fields.get( property ).getValue( object );
        }

    }


    public static int getPropertyInt( final Object root, final String... properties ) {


        final String lastProperty = properties[ properties.length - 1 ];




        if ( isDigits( lastProperty ) ) {

            return Conversions.toInt(getPropertyValue(root, properties));

        }


        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );


        if ( field.type() == Typ.intgr ) {
            return field.getInt( object );
        } else {
            return Conversions.toInt( field.getValue( object ) );
        }

    }


    /**
     * @param root root
     * @param properties properties
     * @return value
     */
    private static Object baseForGetProperty( Object root, String[] properties ) {
        Object object = root;

        Map<String, FieldAccess> fields = null;

        for ( int index = 0; index < properties.length - 1; index++ ) {
            if (object == null) {
                return null;
            }


            String property = properties[ index ];


            if (property.equals("this")) {
                continue;
            }

            if ( isDigits( property ) ) {
                /* We can index numbers and names. */
                object = idx ( object, StringScanner.parseInt ( property ) );

            } else {

                if (object instanceof Collection) {
                    object = _getFieldValuesFromCollectionOrArray( object, property);

                    continue;
                } else if (Typ.isArray(object)) {

                    Iterator iter = Conversions.iterator(object);
                    List list = Lists.list(iter);
                    object = _getFieldValuesFromCollectionOrArray(list, property);


                    continue;
                }


                fields = getPropertyFieldAccessMap( object.getClass() );

                FieldAccess field = fields.get( property );


                if ( field == null ) {
                        return null;
                }

                object = field.getObject( object );
            }
        }
        return object;
    }


    /**
     * @param root root
     * @param properties properties
     * @return class
     */
    private static Class<?> baseForGetProperty( Class<?> root, String[] properties ) {
        Class cls = root;

        Map<String, FieldAccess> fields = null;

        for ( int index = 0; index < properties.length - 1; index++ ) {
            fields = getPropertyFieldAccessMap( cls );

            String property = properties[ index ];
            FieldAccess field = fields.get( property );
            cls = field.type();
        }
        return cls;
    }
    /**
     * Get property value
     *
     * @param object object
     * @param path   in dotted notation
     * @return int
     */
    public static int idxInt( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyInt( object, properties );
    }

    /**
     * Get property value
     *
     * @param object object
     * @param path   in dotted notation
     * @return string
     */
    public static String idxStr( Object object, String path ) {


        final Object val = idx(object, path);
        return Conversions.toString(val);
    }

    private static String getPropertyString(Object root, String[] properties) {



        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        final String lastProperty = properties[ properties.length - 1 ];
        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.string ) {
            return (String) field.getObject( object );
        } else {
            return Conversions.toString(field.getValue(object));
        }


    }


    /**
     * @param root root
     * @param properties properties forming a path
     * @return byte
     */
    public static byte getPropertyByte( final Object root, final String... properties ) {



        final String lastProperty = properties[ properties.length - 1 ];

        if ( isDigits( lastProperty ) ) {

            return Conversions.toByte(getPropertyValue(root, properties));

        }

        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.bt ) {
            return field.getByte( object );
        } else {
            return Conversions.toByte( field.getValue( object ) );
        }
    }

    public static byte idxByte( Object object, String path ) {

        String[] properties = propertyPathAsStringArray(path);

        return getPropertyByte( object, properties );
    }

    public static float getPropertyFloat( final Object root, final String... properties ) {

        final String lastProperty = properties[ properties.length - 1 ];

        if ( isDigits( lastProperty ) ) {

            return Conversions.toFloat(getPropertyValue(root, properties));

        }

        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.flt ) {
            return field.getFloat( object );
        } else {
            return Conversions.toFloat( field.getValue( object ) );
        }
    }


    public static float idxFloat( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyFloat( object, properties );
    }


    public static short getPropertyShort( final Object root,
                                          final String... properties ) {

        final String lastProperty = properties[ properties.length - 1 ];




        if ( isDigits( lastProperty ) ) {

            return Conversions.toShort(getPropertyValue(root, properties));

        }



        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );
        FieldAccess field = fields.get( lastProperty );


        if ( field.type() == Typ.shrt ) {
            return field.getShort( object );
        } else {
            return Conversions.toShort( field.getValue( object ) );
        }
    }


    /**
     * Get Property Path TypeType
     * @param root root
     * @param properties properties forming a path
     * @return class
     */
    public static Class<?> getPropertyPathType( final Object root,
                                          final String... properties ) {


        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );
        final String lastProperty = properties[ properties.length - 1 ];
        FieldAccess field = fields.get( lastProperty );

        return field.type();
    }


    /**
     * Get Property Path TypeType
     * @param root root
     * @param properties properties forming a path
     * @return FieldAccess
     */
    public static FieldAccess getPropertyPathField( final Object root,
                                                final String... properties ) {


        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );
        final String lastProperty = properties[ properties.length - 1 ];
        FieldAccess field = fields.get( lastProperty );

        return field;
    }


    public static FieldAccess getPropertyPathField( final Class root,
                                                    final String... properties ) {


        Class cls = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( cls );
        final String lastProperty = properties[ properties.length - 1 ];
        FieldAccess field = fields.get( lastProperty );

        return field;
    }

    public static short idxShort( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyShort( object, properties );
    }

    public static Class idxType( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyPathType( object, properties );
    }


    public static FieldAccess idxField( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyPathField( object, properties );
    }


    public static FieldAccess idxField( Class<?> cls, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyPathField( cls, properties );
    }

    public static char getPropertyChar( final Object root,
                                        final String... properties ) {


        final String lastProperty = properties[ properties.length - 1 ];


        if ( isDigits( lastProperty ) ) {

            return Conversions.toChar(getPropertyValue(root, properties));

        }

        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.chr ) {
            return field.getChar( object );
        } else {
            return Conversions.toChar( field.getValue( object ) );
        }
    }


    public static char idxChar( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyChar( object, properties );
    }


    public static double getPropertyDouble( final Object root,
                                            final String... properties ) {



        final String lastProperty = properties[ properties.length - 1 ];


        if ( isDigits( lastProperty ) ) {

            return Conversions.toDouble(getPropertyValue(root, properties));

        }


        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.dbl ) {
            return field.getDouble( object );
        } else {
            return Conversions.toDouble( field.getValue( object ) );
        }
    }


    public static double idxDouble( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyDouble( object, properties );
    }


    public static long getPropertyLong( final Object root,
                                        final String... properties ) {

        final String lastProperty = properties[ properties.length - 1 ];


        if ( isDigits( lastProperty ) ) {

            return Conversions.toLong(getPropertyValue(root, properties));

        }


        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.lng ) {
            return field.getLong( object );
        } else {
            return Conversions.toLong( field.getValue( object ) );
        }
    }


    public static long idxLong( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyLong( object, properties );
    }


    public static boolean getPropertyBoolean( final Object root,
                                              final String... properties ) {


        final String lastProperty = properties[ properties.length - 1 ];


        if ( isDigits( lastProperty ) ) {

            return Conversions.toBoolean(getPropertyValue(root, properties));

        }


        Object object = baseForGetProperty( root, properties );

        Map<String, FieldAccess> fields = getFieldsFromObject( object );

        FieldAccess field = fields.get( lastProperty );

        if ( field.type() == Typ.bln ) {
            return field.getBoolean( object );
        } else {
            return Conversions.toBoolean( field.getValue( object ) );
        }
    }


    public static boolean idxBoolean( Object object, String path ) {


        String[] properties = propertyPathAsStringArray(path);

        return getPropertyBoolean( object, properties );
    }


    public static <V> Map<String, V> collectionToMap( String propertyKey, Collection<V> values ) {
        LinkedHashMap<String, V> map = new LinkedHashMap<>( values.size() );
        for ( V v : values ) {
            String key = idxGeneric( Typ.string, v, propertyKey );
            map.put( key, v );
        }
        return map;
    }


    public static void copyProperties( Object object, Map<String, Object> properties ) {

        Set<Map.Entry<String, Object>> props = properties.entrySet();
        for ( Map.Entry<String, Object> entry : props ) {
            setPropertyValue( object, entry.getValue(), entry.getKey() );
        }
    }



    private static Object _getFieldValuesFromCollectionOrArray(Object object,
                                                               final String key) {
        if ( object == null ) {
            return null;
        }

        if (object instanceof Collection ) {
            Collection collection = (Collection) object;


            if (collection.size() == 0) {
                return Collections.EMPTY_LIST;
            }
            List list = new ArrayList( collection.size() );


            Class lastClass=null;
            Map<String, FieldAccess> fields=null;
            FieldAccess field=null;


            for (Object item : collection) {

                if (item instanceof Map) {
                    Map map = (Map) item;
                    list.add(map.get(key));
                }else {

                    Class currentClass = Boon.cls(item);

                    if (lastClass==null || currentClass != lastClass) {

                        fields = getPropertyFieldAccessMap(currentClass);
                        field = fields.get(key);
                        lastClass = currentClass;

                    }

                    if (field == null) {
                        list.add(idx(item, key));
                    } else {

                        list.add(field.getValue(item));
                    }
                }
            }
            return list;

        } else if ( object.getClass().isArray() ) {
            int len = Array.getLength(object);
            List list = new ArrayList( len );


            Map<String, FieldAccess> fields =
                    getPropertyFieldAccessMap( object.getClass().getComponentType());

            for (int index = 0; index < len; index++) {
                list.add(  fields.get(key).getValue(Array.get(object, index)));
            }
            return list;
        }
        else {
            return atIndex(object, key);
        }

    }


    @SuppressWarnings("all")
    public static <T> T copy( T item ) {
        if ( item instanceof Cloneable ) {
             return (T) ClassMeta.classMeta(item.getClass()).invokeUntyped(item, "clone", ( Class[] ) null);
        } else {
            return fieldByFieldCopy( item );
        }
    }


    public static <T> T fieldByFieldCopy( T item ) {

        final Class<T> aClass = (Class<T>) item.getClass();
        Map<String, FieldAccess> fields = Reflection.getAllAccessorFields( aClass );

        T clone = Reflection.newInstance( aClass );

        for ( FieldAccess field : fields.values() ) {


            try {

                /* If the field is static or write only continue. */
                if (field.isStatic() || field.isWriteOnly()) {
                    continue;
                }


                   /* if the field is primitive then just inject it
                    and allow for conversion if needed. */
                if (field.isPrimitive()) {
                    field.setValue(clone, field.getValue(item));
                    continue;
                }


                Object value = field.getObject(item);

                if (value == null ) {

                    field.setObject(clone, null);
                    continue;
                }

               /* If the field is not a basic type and not a primitive then
                   then recursively copy a version into the field field.
                 */
                if (!field.isPrimitive() && !Typ.isBasicType(field.type())) {


                     field.setObject(clone, copy(value));

                    continue;
                }



                    /* It was a basic type so just copy a reference to it. */
                /* It is a basic type. */
                if (Typ.isBasicType(field.type())) {

                     field.setObject(clone, value);

                    continue;
                }

                if (Typ.isCollection(field.type())) {

                    Collection<Object> src = ( Collection<Object> ) value;
                    Class<?> collectionType = field.type();
                    Collection<Object> dst = Conversions.createCollection(collectionType, src.size());

                    for (Object o : src) {
                        dst.add( copy ( o ) );
                    }

                    field.setObject(clone, dst);

                    continue;
                }


                /** We don't handle maps yet. */
                if (Typ.isMap(field.type())) {


                    continue;
                }

                if (field.type().isArray()) {

                    int length = Array.getLength(value);
                    Object dst = Array.newInstance(field.getComponentClass(), length);

                    for (int index =0; index < length; index++) {
                        Object o = Array.get(value, index);
                        Array.set(dst, index, copy (o));
                    }

                    field.setObject(clone, dst);

                    continue;
                }


            } catch (Exception ex) {

                return (T) Exceptions.handle(Object.class, "" + field,  ex );
            }
        }
        return clone;
    }




    public static void copyProperties( Object src, Object dest ) {
         fieldByFieldCopy( src, dest );
    }


    public static <T> T createFromSrc( Object src, Class<T> dest ) {
        T instance = Reflection.newInstance(dest);
        fieldByFieldCopy( src, instance );
        return instance;
    }


    public static void copyProperties( Object src, Object dest, String... ignore) {
        fieldByFieldCopy( src, dest, Sets.set(ignore) );
    }

    public static void copyProperties( Object src, Object dest, Set<String> ignore) {
        fieldByFieldCopy( src, dest, ignore );
    }

    private static void fieldByFieldCopy( Object src, Object dst, Set<String> ignore ) {

        final Class<?> srcClass = src.getClass();
        Map<String, FieldAccess> srcFields = Reflection.getAllAccessorFields( srcClass );


        final Class<?> dstClass =  dst.getClass();
        Map<String, FieldAccess> dstFields = Reflection.getAllAccessorFields ( dstClass );

        for ( FieldAccess srcField : srcFields.values() ) {

            if (ignore.contains ( srcField.name() )) {
                continue;
            }

            FieldAccess dstField = dstFields.get ( srcField.name() );
            try {

                copySrcFieldToDestField ( src, dst, dstField, srcField, ignore );

            }catch (Exception ex) {
                Exceptions.handle( sputs("copying field", srcField.name(), srcClass, " to ", dstField.name(), dstClass), ex );
            }
        }
    }

    private static void fieldByFieldCopy( Object src, Object dst ) {

        final Class<?> srcClass = src.getClass();
        Map<String, FieldAccess> srcFields = Reflection.getAllAccessorFields( srcClass );


        final Class<?> dstClass =  dst.getClass();
        Map<String, FieldAccess> dstFields = Reflection.getAllAccessorFields ( dstClass );

        for ( FieldAccess srcField : srcFields.values() ) {

            FieldAccess dstField = dstFields.get ( srcField.name() );
            try {

                copySrcFieldToDestField ( src, dst, dstField, srcField, null );

            }catch (Exception ex) {
                 Exceptions.handle( sputs("copying field", srcField.name(), srcClass, " to ", dstField.name(), dstClass), ex );
            }
        }
    }

    private static void copySrcFieldToDestField( Object src, Object dst, FieldAccess dstField, FieldAccess srcField, Set<String> ignore ) {
        if ( srcField.isStatic() ) {
            return ;
        }

        if (dstField == null ) {
            return ;
        }

                /* If its primitive handle it. */
        if ( srcField.isPrimitive() ) {
            dstField.setValue( dst, srcField.getValue( src ) );
            return ;
        }

        Object srcValue = srcField.getObject( src );

                /* if value is null then handle it unless it is primitive.*/
        if (srcValue == null) {
            if ( !dstField.isPrimitive () ) {
                dstField.setObject(dst, null);
            }
            return ;
        }





                /* Basic type. */
        if ( Typ.isBasicType( srcField.type() ) ) {
            /* Handle non primitive copy. */
            Object value = srcField.getObject( src );
            dstField.setValue( dst,  value  );
            return ;
        }

                /* Types match and not a collection so just copy. */
        if (    !(srcValue instanceof Collection ) && dstField.type() == srcValue.getClass() ||
                Typ.isSuperType ( dstField.type(), srcValue.getClass () ) ) {

            dstField.setObject(dst, copy( srcField.getObject ( src ) ));
            return ;
        }



                /* Collection field copy. */
        if ( srcValue instanceof Collection && dstField.getComponentClass() != null
                            && Typ.isCollection ( dstField.type() )
                            ) {

            handleCollectionFieldCopy ( dst, dstField, ( Collection ) srcValue );
            return ;

        }


                      /* Non identical object copy. */
        if (dstField.typeEnum () == TypeType.ABSTRACT || dstField.typeEnum () == TypeType.INTERFACE) {
                            //no op
        } else {
                Object newInstance = Reflection.newInstance ( dstField.type() );
                if (ignore == null) {
                    fieldByFieldCopy( srcField.getObject( src ), newInstance );
                } else {
                    fieldByFieldCopy( srcField.getObject( src ), newInstance, ignore );
                }
                dstField.setObject ( dst, newInstance );
        }
    }

    private static void handleCollectionFieldCopy( Object dst, FieldAccess dstField, Collection srcValue ) {
        if ( dstField.getComponentClass () != Typ.string )  {

            Collection dstCollection = Conversions.createCollection( dstField.type(), srcValue.size() );
            for ( Object srcComponentValue : srcValue ) {

                Object newInstance = Reflection.newInstance( dstField.getComponentClass() );
                fieldByFieldCopy( srcComponentValue, newInstance );
                dstCollection.add ( newInstance );
            }

            dstField.setObject ( dst, dstCollection );
        } else {

            Collection dstCollection = Conversions.createCollection( dstField.type(), srcValue.size() );
            for ( Object srcComponentValue : srcValue ) {

                if (srcComponentValue!=null) {
                    dstCollection.add ( srcComponentValue.toString () );
                }
            }

            dstField.setObject ( dst, dstCollection );

        }
    }

    public static Object idx( Object object, int index ) {
        if ( Boon.isArray( object ) ) {
            object = Array.get ( object, index );
        } else if ( object instanceof List ) {
            object = Lists.idx ( ( List ) object, index );
        }
        return object;
    }

    public static void idx( Object object, int index, Object value ) {
        try {
            if ( Boon.isArray( object ) ) {
                Array.set( object, index, value );
            } else if ( object instanceof List ) {
                Lists.idx( ( List ) object, index, value );
            }
        } catch ( Exception notExpected ) {
            String msg = lines( "An unexpected error has occurred",
                    "This is likely a programming error!",
                    String.format( "Object is %s, index is %s, and set is %s", object, index, value ),
                    String.format( "The object is an array? %s", object == null ? "null" : object.getClass().isArray() ),
                    String.format( "The object is of type %s", object == null ? "null" : object.getClass().getName() ),
                    String.format( "The set is of type %s", value == null ? "null" : value.getClass().getName() ),

                    ""

            );
            Exceptions.handle( msg, notExpected );
        }
    }

    public static <T> T idx( Class<T> type, Object object, String property ) {
        return (T) idx(object, property);
    }


    public static <T> T atIndex( Class<T> type, Object object, String property ) {
        return (T) idx(object, property);
    }



    /**
     * Is this a property path?
     * @param prop property
     * @return true or false
     */
    public static boolean isPropPath(String prop) {
        if (prop.contains(".")) return true;
        if (prop.equals("this")) return true;
        if (prop.contains("[")) return true;

        return false;
    }



    public static void setCollectionProperty(Collection<?> list, String propertyName, Object value) {
        for (Object object : list) {
            BeanUtils.idx(object, propertyName, value);
        }
    }


    public static void setIterableProperty(Iterable<?> list, String propertyName, Object value) {
        for (Object object : list) {
            BeanUtils.idx(object, propertyName, value);
        }
    }


    public static String asPrettyJsonString(Object bean) {
        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintBean(bean).toString();
    }

    public static String asPrettyJsonString(Mapper mapper, Object bean) {
        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintBean(mapper, bean).toString();
    }


}
