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

import io.advantageous.boon.Sets;
import io.advantageous.boon.core.reflection.fields.FieldAccessMode;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.fields.FieldsAccessor;

import java.util.*;



/**
 * Created by rick on 12/26/13.
 * @author Richard Hightower
 * <p>
 * This class creates Java objects from java.util.Lists and java.util.Maps.
 * It is used by the JSON parser lib.
 * There are map like objects that are index overlays of the parsed JSON.
 * This set of utilties makes Java a bit more dynamic.
 * This is the core of the serialization for JSON and works in conjunction with TypeType.
 * </p>
 */
public class MapObjectConversion {

    private static final Mapper mapper = new MapperSimple();

    private static final Mapper mapperWithType = new MapperComplex();



    private static final Mapper prettyMapper = new MapperComplex(
            false, //outputType
            FieldAccessMode.PROPERTY_THEN_FIELD, //fieldAccessType
            true, //useAnnotations
            false, //caseInsensitiveFields
            null, //ignoreSet
            null, //view
            true, true);




    /** Convert an item from a list into a class using the classes constructor.
     *
     * REFACTOR: Can't this just be from collection?
     * REFACTOR
     *
     * @param argList list if arguments
     * @param clazz  the type of the object we are creating
     * @param <T> generics
     * @return the new object that we just created.
     */
    public static <T> T fromList( List<?> argList, Class<T> clazz ) {
        return mapper.fromList(argList, clazz);
    }


    /** Convert an item from a list into a class using the classes constructor.
     *
     * REFACTOR: Can't this just be from collection?
     * REFACTOR
     *
     * @param respectIgnore  honor @JsonIgnore, transients, etc. of the field
     * @param view honor views for fields
     * @param fieldsAccessor how we are going to access the fields (by field, by property, combination)
     * @param argList list if arguments
     * @param clazz  the type of the object we are creating
     * @param ignoreSet a set of properties to ignore
     * @param <T> generics
     * @return the new object that we just created.
     */
    public static <T> T fromList( boolean respectIgnore, String view, FieldsAccessor fieldsAccessor,
                                  List<?> argList, Class<T> clazz, Set<String> ignoreSet ) {

        return new MapperComplex(fieldsAccessor, ignoreSet, view, respectIgnore).fromList(argList, clazz);

    }


    /** Convert an item from a list into a class using the classes constructor.
     *
     * REFACTOR: Can't this just be from collection?
     * REFACTOR
     *
     * @param fieldsAccessor how we are going to access the fields (by field, by property, combination)
     * @param argList list if arguments
     * @param clazz  the type of the object we are creating
     * @param <T> generics
     * @return the new object that we just created.
     */
    public static <T> T fromList( FieldsAccessor fieldsAccessor, List<?> argList, Class<T> clazz ) {
        return new MapperComplex(fieldsAccessor, null, null, false).fromList(argList, clazz);
    }



    /** Convert an object to a list.
     *
     * @param object the object we want to convert to a list
     * @return new list from an object
     */
    public static List<?> toList( Object object) {

        return mapper.toList(object);
    }


    /**
     * From map.
     * @param map map to create the object from.
     * @param clazz the new instance type
     * @param <T> generic type capture
     * @return new object
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T fromMap( Map<String, Object> map, Class<T> clazz ) {
        return mapper.fromMap(map, clazz);
    }




    /**
     * fromMap converts a map into a java object.
     * @param map map to create the object from.
     * @param clazz  the new instance type
     * @param excludeProperties the properties to exclude
     * @param <T> generic type capture
     * @return the new object
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T fromMap( Map<String, Object> map, Class<T> clazz, String... excludeProperties ) {
        Set<String> ignoreProps = excludeProperties.length > 0 ? Sets.set(excludeProperties) :  null;
        return new MapperComplex(FieldAccessMode.FIELD_THEN_PROPERTY.create( false ), ignoreProps, null, true).fromMap(map, clazz);


    }


    /**
     * fromMap converts a map into a Java object.
     * This version will see if there is a class parameter in the map, and dies if there is not.
     * @param map map to create the object from.
     * @return new object
     */
    public static Object fromMap( Map<String, Object> map ) {
        return mapper.fromMap(map);
    }


    /**
     * fromMap converts a map into a java object
     * @param respectIgnore honor @JsonIgnore, transients, etc. of the field
     * @param view the view of the object which can ignore certain fields given certain views
     * @param fieldsAccessor how we are going to access the fields (by field, by property, combination)
     * @param map map to create the object from.
     * @param cls class type of new object
     * @param ignoreSet a set of properties to ignore
     * @param <T> map to create teh object from.
     * @return new object of type cls
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromMap( boolean respectIgnore, String view, FieldsAccessor fieldsAccessor, Map<String, Object> map, Class<T> cls, Set<String> ignoreSet ) {

        Mapper mapper = new MapperComplex(ignoreSet, view, respectIgnore);

        return mapper.fromMap(map, cls);
    }


    /**
     * Creates an object from a value map.
     *
     * This does some special handling to take advantage of us using the value map so it avoids creating
     * a bunch of array objects and collections. Things you have to worry about when writing a
     * high-speed JSON serializer.
     * @param respectIgnore  honor @JsonIgnore, transients, etc. of the field
     * @param view the view of the object which can ignore certain fields given certain views
     * @param fieldsAccessor how we are going to access the fields (by field, by property, combination)
     * @param cls the new type
     * @param ignoreSet a set of properties to ignore
     * @param valueMap  value map
     * @param <T> type
     * @return new object from value map
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromValueMap( boolean respectIgnore, String view, final FieldsAccessor fieldsAccessor,
                                      final Map<String, Value> valueMap,
                                      final Class<T> cls, Set<String> ignoreSet ) {


        Mapper mapper = new MapperComplex(fieldsAccessor, ignoreSet, view, respectIgnore);
        return mapper.fromValueMap(valueMap, cls);

     }


    /**
     * Basic toMap to create an object into a map.
     * @param object the object we want to convert to a list
     * @param ignore do we honor ignore properties
     * @return new map
     */
    public static Map<String, Object> toMap( final Object object, final String... ignore ) {
        return toMap( object, Sets.set( ignore ) );
    }



    /**
     * This could be refactored to use core.TypeType class and it would run faster.
     * Converts an object into a map
     * @param object the object that we want to convert
     * @param ignore the map
     * @return map map representation of the object
     */
    public static Map<String, Object> toMap( final Object object, Set<String> ignore ) {

        return new MapperComplex(ignore).toMap(object);

    }




    /**
     * This could be refactored to use core.TypeType class and it would run faster.
     *
     * Converts an object into a map
     * @param object the object that we want to convert
     * @return map map representation of the object
     */
    public static Map<String, Object> toMap( final Object object ) {

        return mapper.toMap(object);
    }


    /**
     * This could be refactored to use core.TypeType class and it would run faster.
     *
     * Converts an object into a map
     * @param object the object that we want to convert
     * @return map map representation of the object
     */
    public static Map<String, Object> toMapWithType( final Object object ) {

        return mapperWithType.toMap(object);
    }

    /**
     * This converts a list of maps to objects.
     * I always forget that this exists. I need to remember.
     *
     * @param respectIgnore   honor @JsonIgnore, transients, etc. of the field
     * @param view the view of the object which can ignore certain fields given certain views
     * @param fieldsAccessor how we are going to access the fields (by field, by property, combination)
     * @param componentType The component type of the created list
     * @param list the input list
     * @param ignoreProperties properties to ignore
     * @param <T> generics
     * @return a new list
     */
    public  static <T> List<T> convertListOfMapsToObjects(   boolean respectIgnore, String view,
                                                            FieldsAccessor fieldsAccessor,
                                                            Class<T> componentType, List<Map> list, Set<String> ignoreProperties) {
        return new MapperComplex(fieldsAccessor, ignoreProperties,view, respectIgnore).convertListOfMapsToObjects(list, componentType);
    }

    /**
     *
     * @param componentType The component type of the created list
     * @param list the input list
     * @param <T> T
     * @return T
     */
    public static  <T> List<T> convertListOfMapsToObjects(Class<T> componentType, List<Map> list) {
        return mapper.convertListOfMapsToObjects(list, componentType);
    }

    /**
     * Creates a list of maps from a list of class instances.
     * @param collection  the collection we are coercing into a field value
     * @return the return value.
     */
    public static List<Map<String, Object>> toListOfMaps( Collection<?> collection ) {

        return mapper.toListOfMaps(collection);
    }

    public static Map toPrettyMap(Object object) {
        return prettyMapper.toMap(object);
    }
}
