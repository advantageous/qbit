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

import io.advantageous.boon.Boon;
import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.Sys;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.requireNonNull;

public class Annotations {

    private final static Context _context;
    private static WeakReference<Context> weakContext = new WeakReference<>( null );


    private static class Context {

        private Map<
                Class<?>,
                Map <String, List<AnnotationData>
                        >> annotationDataCacheProperty = new ConcurrentHashMap<> (  );



        private Map<
                Class<?>,
                Map <String, List<AnnotationData>
                        >> annotationDataCacheField = new ConcurrentHashMap<> (  );


        private Map<Class<?>, List<AnnotationData>> annotationDataCacheClass
                = new ConcurrentHashMap<>(  );


        private Map<
                Class<?>,
                Map<String, AnnotationData>
                > annotationDataCacheClassAsMap
                = new ConcurrentHashMap<>(  );


    }

    static {

        boolean noStatics = Boolean.getBoolean( "io.advantageous.boon.noStatics" );
        if ( noStatics || Sys.inContainer () ) {

            _context = null;
            weakContext = new WeakReference<>( new Context() );

        } else {
            _context = new Context();
        }
    }



    /* Manages weak references. */
    private static Context context() {

        if ( _context != null ) {
            return _context;
        } else {
            Context context = weakContext.get();
            if ( context == null ) {
                context = new Context();
                weakContext = new WeakReference<>( context );
            }
            return context;
        }
    }

    public static List<AnnotationData> getAnnotationDataForProperty( Class<?> clazz, String propertyName, boolean useReadMethod, Set<String> allowedPackages ) {

        final Map<Class<?>, Map<String, List<AnnotationData>>> cacheProperty = context ().annotationDataCacheProperty;

        Map<String, List<AnnotationData>> classMap = cacheProperty.get(clazz);

        if (classMap == null) {
            classMap = new ConcurrentHashMap<> (  );
            cacheProperty.put ( clazz, classMap );
        }

        List<AnnotationData> annotationDataList = classMap.get ( propertyName );
        if (annotationDataList == null ) {

            annotationDataList = extractValidationAnnotationData ( extractAllAnnotationsForProperty ( clazz, propertyName, useReadMethod ), allowedPackages );
            if (annotationDataList == null) {
                annotationDataList = Collections.EMPTY_LIST;
            }
            classMap.put (propertyName,  annotationDataList );

        }

        return annotationDataList;

    }

    public static List<AnnotationData> getAnnotationDataForField( Class<?> clazz, String propertyName, Set<String> allowedPackages ) {

        final Map<Class<?>, Map<String, List<AnnotationData>>> cacheProperty = context ().annotationDataCacheField;

        Map<String, List<AnnotationData>> classMap = cacheProperty.get(clazz);

        if (classMap == null) {
            classMap = new ConcurrentHashMap<> (  );
            cacheProperty.put ( clazz, classMap );
        }

        List<AnnotationData> annotationDataList = classMap.get ( propertyName );
        if (annotationDataList == null ) {

            annotationDataList = extractValidationAnnotationData( findFieldAnnotations( clazz, propertyName ), allowedPackages );

            if (annotationDataList == null) {
                annotationDataList = Collections.EMPTY_LIST;
            }
            classMap.put (propertyName,  annotationDataList );

        }

        return annotationDataList;

    }

    public static List<AnnotationData> getAnnotationDataForClass( Class<?> clazz  ) {
        return getAnnotationDataForClass ( clazz, Collections.EMPTY_SET );

    }



    public static List<AnnotationData> getAnnotationDataForMethod( Method method ) {
        List<AnnotationData> list = extractValidationAnnotationData( method.getDeclaredAnnotations(), Collections.EMPTY_SET );
        return list;
    }


    public static List<List<AnnotationData>> getAnnotationDataForMethodParams( Method method ) {


        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        final List<List<AnnotationData>> parameterAnnotationsList = new ArrayList<>(parameterAnnotations.length);

        for (Annotation[] paramAnnotaions : parameterAnnotations) {
            List<AnnotationData> list = extractValidationAnnotationData( paramAnnotaions, Collections.EMPTY_SET );
            parameterAnnotationsList.add(list);

        }

        return parameterAnnotationsList;
    }

    public static List<AnnotationData> getAnnotationDataForMethod( Constructor method ) {
        List<AnnotationData> list = extractValidationAnnotationData( method.getDeclaredAnnotations(), Collections.EMPTY_SET );
        return list;
    }


    public static Map<String, AnnotationData> getAnnotationDataForClassAsMap( Class<?> clazz  ) {

        final Map<Class<?>, Map<String, AnnotationData>> cache = context ().annotationDataCacheClassAsMap;

         Map<String, AnnotationData> map = cache.get ( clazz );

        if (map ==  null) {
            final List<AnnotationData> list = getAnnotationDataForClass ( clazz );

            if (list.size () == 0) {
                map = Collections.EMPTY_MAP;
            } else {
                map = new ConcurrentHashMap<> ( list.size () );

                for (AnnotationData data : list) {
                    map.put ( data.getFullClassName (), data );
                    map.put ( data.getSimpleClassName (), data);
                    map.put ( data.getName (), data);
                }
            }
            cache.put ( clazz, map );
        }
        return map;
    }

    public static List<AnnotationData> getAnnotationDataForClass( Class<?> clazz, Set<String> allowedPackages ) {

        final Map<Class<?>,  List<AnnotationData>> cache = context ().annotationDataCacheClass;

        List<AnnotationData> annotationDataList = cache.get ( clazz );
        if (annotationDataList ==  null) {
            annotationDataList =  extractValidationAnnotationData( findClassAnnotations( clazz ), allowedPackages );
            cache.put ( clazz, annotationDataList );
        }
        return annotationDataList;
    }

    private static Annotation[] findClassAnnotations( Class<?> clazz ) {
        return clazz.getAnnotations();
    }

    public static Collection<AnnotationData> getAnnotationDataForFieldAndProperty( Class<?> clazz, String propertyName, Set<String> allowedPackages ) {
        /* Extract the AnnotationData from the Java annotation. */
        List<AnnotationData> propertyAnnotationDataList =
                getAnnotationDataForProperty( clazz, propertyName, false, allowedPackages );

        /* Read the field annotation.  */
        List<AnnotationData> fieldAnnotationDataList =
                getAnnotationDataForField( clazz, propertyName, allowedPackages );

        /* Combine the annotation from field and properties. Field validations take precedence over property validations. */
        Map<String, AnnotationData> map = new HashMap<String, AnnotationData>( propertyAnnotationDataList.size() + fieldAnnotationDataList.size() );

        /* Add the property annotation to the map. */
        for ( AnnotationData annotationData : propertyAnnotationDataList ) {
            map.put( annotationData.getName(), annotationData );
        }

        /* Add the field annotation to the map allowing them to override the property annotation. */
        for ( AnnotationData annotationData : fieldAnnotationDataList ) {
            map.put( annotationData.getName(), annotationData );
        }
        return map.values();
    }


    /**
     * Create an annotation data list.
     *
     * @param annotations list of annotation.
     * @param allowedPackages allowed packages
     * @return list of annotation data
     */
    public static List<AnnotationData> extractValidationAnnotationData(
            Annotation[] annotations, Set<String> allowedPackages ) {
        List<AnnotationData> annotationsList = new ArrayList<>();
        for ( Annotation annotation : annotations ) {
            AnnotationData annotationData = new AnnotationData( annotation, allowedPackages );
            if ( annotationData.isAllowed() ) {
                annotationsList.add( annotationData );
            }
        }
        return annotationsList;
    }

    /**
     * Extract all annotation for a given property.
     * Searches current class and if none found searches
     * super class for annotation. We do this because the class
     * could be proxied with AOP.
     *
     * @param clazz        Class containing the property.
     * @param propertyName The name of the property.
     * @return array of annotation data.
     */
    private static Annotation[] extractAllAnnotationsForProperty( Class<?> clazz, String propertyName, boolean useRead ) {
        try {

            Annotation[] annotations = findPropertyAnnotations( clazz, propertyName, useRead );

            /* In the land of dynamic proxied AOP classes,
             * this class could be a proxy. This seems like a bug
             * waiting to happen. So far it has worked... */
            if ( annotations.length == 0 ) {
                annotations = findPropertyAnnotations( clazz.getSuperclass(), propertyName, useRead );
            }
            return annotations;
        } catch ( Exception ex ) {
            return Exceptions.handle (Annotation[].class,
                    Boon.sputs(
                            "Unable to extract annotation for property",
                            propertyName, " of class ", clazz,
                            "  useRead ", useRead), ex);
        }

    }

    /**
     * Find annotation given a particular property name and clazz. This figures
     * out the writeMethod for the property, and uses the write method
     * to look up the annotation.
     *
     * @param clazz        The class that holds the property.
     * @param propertyName The name of the property.
     * @return array of annotation data
     * @throws java.beans.IntrospectionException
     *
     */
    private static Annotation[] findPropertyAnnotations( Class<?> clazz, String propertyName, boolean useRead )
            throws IntrospectionException {

        PropertyDescriptor propertyDescriptor = getPropertyDescriptor( clazz, propertyName );
        if ( propertyDescriptor == null ) {
            return new Annotation[]{ };
        }
        Method accessMethod = null;

        if ( useRead ) {
            accessMethod = propertyDescriptor.getReadMethod();
        } else {
            accessMethod = propertyDescriptor.getWriteMethod();
        }

        if ( accessMethod != null ) {
            Annotation[] annotations = accessMethod.getAnnotations();
            return annotations;
        } else {
            return new Annotation[]{ };
        }
    }


    /**
     * This needs refactor and put into Refleciton.
     */
    private static PropertyDescriptor getPropertyDescriptor( final Class<?> type, final String propertyName ) {

        Exceptions.requireNonNull(type);
        Exceptions.requireNonNull(propertyName);

        if ( !propertyName.contains( "." ) ) {
            return doGetPropertyDescriptor( type, propertyName );
        } else {
            String[] propertyNames = propertyName.split( "[.]" );
            Class<?> clazz = type;
            PropertyDescriptor propertyDescriptor = null;
            for ( String pName : propertyNames ) {
                propertyDescriptor = doGetPropertyDescriptor( clazz, pName );
                if ( propertyDescriptor == null ) {
                    return null;
                }
                clazz = propertyDescriptor.getPropertyType();
            }
            return propertyDescriptor;
        }
    }


    private static Annotation[] findFieldAnnotations( Class<?> clazz, String propertyName ) {
        Field field = getField( clazz, propertyName );

        if ( field == null ) {
            return new Annotation[]{ };
        }
        Annotation[] annotations = field.getAnnotations();
        return annotations;
    }


    /**
     * This needs to be refactored and put into Reflection or something.
     */
    private static PropertyDescriptor doGetPropertyDescriptor( final Class<?> type, final String propertyName ) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo( type );
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for ( PropertyDescriptor pd : propertyDescriptors ) {
                if ( pd.getName().equals( propertyName ) ) {
                    return pd;
                }
            }
            Class<?> superclass = type.getSuperclass();
            if ( superclass != null ) {
                return doGetPropertyDescriptor( superclass, propertyName );
            }
            return null;

        } catch ( Exception ex ) {
            throw new RuntimeException( "Unable to get property " + propertyName + " for class " + type,
                    ex );
        }
    }


    private static Field getField( final Class<?> type, final String fieldName ) {
        if ( !fieldName.contains( "." ) ) {
            return doFindFieldInHeirarchy( type, fieldName );
        } else {
            String[] fieldNames = fieldName.split( "[.]" );
            Class<?> clazz = type;
            Field field = null;
            for ( String fName : fieldNames ) {
                field = doFindFieldInHeirarchy( clazz, fName );
                if ( field == null ) {
                    return null;
                }
                clazz = field.getType();
            }

            field.setAccessible( true );
            return field;
        }
    }


    private static Field doFindFieldInHeirarchy( Class<?> clazz, String propertyName ) {
        Field field = doGetField( clazz, propertyName );

        Class<?> sclazz = clazz.getSuperclass();
        if ( field == null ) {
            while ( true ) {
                if ( sclazz != null ) {
                    field = doGetField( sclazz, propertyName );
                    sclazz = sclazz.getSuperclass();
                }
                if ( field != null ) {
                    break;
                }
                if ( sclazz == null ) {
                    break;
                }
            }
        }

        if (field!=null)field.setAccessible( true );
        return field;
    }

    private static Field doGetField( Class<?> clazz, String fieldName ) {
        Field field = null;
        try {
            field = clazz.getDeclaredField( fieldName );
        } catch ( SecurityException se ) {
            field = null;
        } catch ( NoSuchFieldException nsfe ) {
            field = null;
        }
        if ( field == null ) {
            Field[] fields = clazz.getDeclaredFields();
            for ( Field f : fields ) {
                if ( f.getName().equals( fieldName ) ) {
                    field = f;
                }
            }
        }
        if ( field != null ) {
            field.setAccessible( true );
        }
        return field;
    }



    public static Object contextToHold() {
        return context();
    }

}
