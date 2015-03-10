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

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Sets;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.reflection.fields.FieldAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 2/17/14.
 */
public class Fields {
    private final static Set<String> fieldSortNames = Sets.safeSet("name", "orderBy", "title", "key");
    private final static Set<String> fieldSortNamesSuffixes = Sets.safeSet( "Name", "Title", "Key" );

    private static void setSortableField( Class<?> clazz, String fieldName ) {
        Reflection.context()._sortableFields.put( clazz.getName(), fieldName );
    }

    private static String getSortableField( Class<?> clazz ) {
        return Reflection.context()._sortableFields.get( clazz.getName() );
    }

    /**
     * Checks to see if we have a string field.
     *
     * @param value1 value1
     * @param name name
     * @return result
     */
    public static boolean hasStringField( final Object value1, final String name ) {

        Class<?> clz = value1.getClass();
        return classHasStringField( clz, name );
    }

    /**
     * Checks to see if this class has a string field.
     *
     * @param clz class
     * @param name name
     * @return result
     */
    public static boolean classHasStringField( Class<?> clz, String name ) {

        List<Field> fields = Reflection.getAllFields( clz );
        for ( Field field : fields ) {
            if (
                    field.getType().equals( Typ.string ) &&
                            field.getName().equals( name ) &&
                            !Modifier.isStatic( field.getModifiers() ) &&
                            field.getDeclaringClass() == clz
                    ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to if an instance has a field
     *
     * @param value1 value1
     * @param name name
     * @return result
     */
    public static boolean hasField( Object value1, String name ) {
        return classHasField( value1.getClass(), name );
    }

    /**
     * Checks to see if a class has a field.
     *
     * @param clz class we are analyzing
     * @param name name
     * @return result
     */
    public static boolean classHasField( Class<?> clz, String name ) {
        List<Field> fields = Reflection.getAllFields( clz );
        for ( Field field : fields ) {
            if ( field.getName().equals( name )
                    && !Modifier.isStatic( field.getModifiers() )
                    && field.getDeclaringClass() == clz ) {
                return true;
            }
        }

        return false;
    }

    /**
     * This can be used for default sort.
     *
     * @param value1 value we are analyzing
     * @return first field that is comparable or primitive.
     */
    public static String getFirstComparableOrPrimitive( Object value1 ) {
        return getFirstComparableOrPrimitiveFromClass( value1.getClass() );
    }

    /**
     * This can be used for default sort.
     *
     * @param clz class we are analyzing
     * @return first field that is comparable or primitive or null if not found.
     */
    public static String getFirstComparableOrPrimitiveFromClass( Class<?> clz ) {
        List<Field> fields = Reflection.getAllFields( clz );
        for ( Field field : fields ) {

            if ( ( field.getType().isPrimitive() || Typ.isComparable( field.getType() )
                    && !Modifier.isStatic( field.getModifiers() )
                    && field.getDeclaringClass() == clz )
                    ) {
                return field.getName();
            }
        }

        return null;
    }

    /**
     * getFirstStringFieldNameEndsWith
     *
     * @param value object we are looking at
     * @param name  name
     * @return field name or null
     */
    public static String getFirstStringFieldNameEndsWith( Object value, String name ) {
        return getFirstStringFieldNameEndsWithFromClass( value.getClass(), name );
    }

    /**
     * getFirstStringFieldNameEndsWithFromClass
     *
     * @param clz  class we are looking at
     * @param name name
     * @return field name or null
     */
    public static String getFirstStringFieldNameEndsWithFromClass( Class<?> clz, String name ) {
        List<Field> fields = Reflection.getAllFields( clz );
        for ( Field field : fields ) {
            if (
                    field.getName().endsWith( name )
                            && field.getType().equals( Typ.string )
                            && !Modifier.isStatic( field.getModifiers() )
                            && field.getDeclaringClass() == clz ) {

                return field.getName();
            }
        }

        return null;
    }

    /**
     * Gets the first sortable fields found.
     *
     * @param value1 value
     * @return sortable field
     */
    public static String getSortableField( Object value1 ) {

        if (value1 instanceof Map) {
            return getSortableFieldFromMap( (Map<String, ?>) value1);
        } else {
            return getSortableFieldFromClass( value1.getClass() );
        }
    }

    private static String getSortableFieldFromMap(Map<String, ?> map) {



            /* See if we have this sortable field and look for string first. */
        for (String name : fieldSortNames) {
            if (map.containsKey(name)) {
                return name;
            }
        }

            /*
             Now see if we can find one of our predefined suffixes.
             */
        for (String suffix : fieldSortNamesSuffixes) {
            for (String key : map.keySet()) {
                if (key.endsWith(suffix)) {
                    return key;
                }
            }
        }


        for (Object object : map.entrySet()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) object;
            if (Typ.isBasicType(entry.getValue())) {
                return entry.getKey();
            }
        }


        for (Object object : map.entrySet()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) object;
            if (entry.getValue() instanceof Comparable) {
                return entry.getKey();
            }
        }

        return Exceptions.die(String.class, "No suitable sort key was found");
    }

    /**
     * Gets the first sortable field.
     *
     * @param clazz the class we are getting the sortable field from.
     * @return sortable field
     */
    public static String getSortableFieldFromClass( Class<?> clazz ) {

        /** See if the fieldName is in the field listStream already.
         * We keep a hash-map cache.
         * */
        String fieldName = getSortableField( clazz );

        /**
         * Not found in cache.
         */
        if ( fieldName == null ) {

            /* See if we have this sortable field and look for string first. */
            for ( String name : fieldSortNames ) {
                if ( classHasStringField( clazz, name ) ) {
                    fieldName = name;
                    break;
                }
            }

            /*
             Now see if we can find one of our predefined suffixes.
             */
            if ( fieldName == null ) {
                for ( String name : fieldSortNamesSuffixes ) {
                    fieldName = getFirstStringFieldNameEndsWithFromClass( clazz, name );
                    if ( fieldName != null ) {
                        break;
                    }
                }
            }

            /**
             * Ok. We still did not find it so give us the first comparable or
             * primitive that we can find.
             */
            if ( fieldName == null ) {
                fieldName = getFirstComparableOrPrimitiveFromClass( clazz );
            }

            /* We could not find a sortable field. */
            if ( fieldName == null ) {
                setSortableField( clazz, "NOT FOUND" );
                Exceptions.die("Could not find a sortable field for type " + clazz);

            }

            /* We found a sortable field. */
            setSortableField( clazz, fieldName );
        }
        return fieldName;

    }

    public static boolean hasField( Class<?> aClass, String name ) {
        Map<String, FieldAccess> fields = Reflection.getAllAccessorFields( aClass );
        return fields.containsKey( name );
    }
}
