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
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.core.reflection.fields.PropertyField;
import io.advantageous.boon.core.reflection.fields.ReflectField;
import io.advantageous.boon.core.reflection.fields.UnsafeField;
import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Lists;
import io.advantageous.boon.Pair;
import sun.misc.Unsafe;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;



public class Reflection {



    public static List<Field> getFields( Class<? extends Object> theClass ) {


        List<Field> fields = context().__fields.get(theClass);
        if (fields!=null) {
            return fields;
        }

        fields = Lists.list(theClass.getDeclaredFields());
        boolean foundCrap = false;
        for ( Field field : fields ) {
            if (field.getName().indexOf('$')!=-1) {
                foundCrap = true;
                continue;
            }
            field.setAccessible( true );
        }


        if (foundCrap) {
            List<Field> copy = Lists.copy(fields);

            for (Field field : copy) {
                if (field.getName().indexOf('$')!=-1) {
                   fields.remove(field);
                }


            }
        }

        return fields;
    }


    private static final Logger log = Logger.getLogger( Reflection.class.getName() );

    private static boolean _useUnsafe;

    static {
        try {
            Class.forName( "sun.misc.Unsafe" );
            _useUnsafe = true;
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            _useUnsafe = false;
        }

        _useUnsafe = _useUnsafe && !Boolean.getBoolean( "io.advantageous.boon.noUnsafe" );
    }

    private static final boolean useUnsafe = _useUnsafe;



    private final static Context _context;
    private static WeakReference<Context> weakContext = new WeakReference<>( null );


    static {

        boolean noStatics = Boolean.getBoolean( "io.advantageous.boon.noStatics" );
        if ( noStatics || Sys.inContainer() ) {

            _context = null;
            weakContext = new WeakReference<>( new Context() );

        } else {
            ;
            _context = new Context();
        }
    }




    public static Unsafe getUnsafe() {
        if ( context().control == null ) {
            try {
                Field f = Unsafe.class.getDeclaredField( "theUnsafe" );
                f.setAccessible( true );
                context().control = ( Unsafe ) f.get( null );
                return context().control;
            } catch ( Exception e ) {
                return null;
            }
        } else {
            return context().control;
        }
    }


    public static Object contextToHold() {
        return context();
    }

    /* Manages weak references. */
    static Context context() {

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

    static class Context {


        Map<Class<?>, List<Field>> __fields = new ConcurrentHashMap<>( 200 );

        Unsafe control;
        Map<String, String> _sortableFields = new ConcurrentHashMap<>();

        Map<Class<?>, ClassMeta<?>> _classMetaMap = new ConcurrentHashMap<>( 200 );

        Map<Class<?>, Map<String, FieldAccess>> _allAccessorReflectionFieldsCache = new ConcurrentHashMap<>( 200 );
        Map<Class<?>, Map<String, FieldAccess>> _allAccessorPropertyFieldsCache = new ConcurrentHashMap<>( 200 );
        Map<Class<?>, Map<String, FieldAccess>> _allAccessorUnsafeFieldsCache = new ConcurrentHashMap<>( 200 );

        Map<Class<?>, Map<String, FieldAccess>> _combinedFieldsFieldsFirst = new ConcurrentHashMap<>( 200 );
        Map<Class<?>, Map<String, FieldAccess>> _combinedFieldsFieldsFirstForSerializer = new ConcurrentHashMap<>( 200 );

        Map<Class<?>, Map<String, FieldAccess>> _combinedFieldsPropertyFirst = new ConcurrentHashMap<>( 200 );
        Map<Class<?>, Map<String, FieldAccess>> _combinedFieldsPropertyFirstForSerializer = new ConcurrentHashMap<>( 200 );

    }



    private static Map<String, FieldAccess> getCombinedFieldsPropertyFirst(Class<? extends Object> theClass) {
        return context()._combinedFieldsPropertyFirst.get(theClass);
    }


    private static Map<String, FieldAccess> getCombinedFieldsPropertyFirstForSerializer(Class<? extends Object> theClass) {
        return context()._combinedFieldsPropertyFirstForSerializer.get(theClass);
    }

    private static Map<String, FieldAccess> getCombinedFieldsFieldFirst(Class<? extends Object> theClass) {
        return context()._combinedFieldsFieldsFirst.get(theClass);
    }

    private static Map<String, FieldAccess> getCombinedFieldsFieldFirstForSerializer(Class<? extends Object> theClass) {
        return context()._combinedFieldsFieldsFirstForSerializer.get(theClass);
    }

    private static void  putCombinedFieldsPropertyFirst(Class<?> theClass, Map<String, FieldAccess> map) {
        context()._combinedFieldsPropertyFirst.put(theClass, map);
    }


    private static void  putCombinedFieldsPropertyFirstForSerializer(Class<?> theClass, Map<String, FieldAccess> map) {
        context()._combinedFieldsPropertyFirst.put(theClass, map);
    }

    private static void putCombinedFieldsFieldFirst(Class<?> theClass,  Map<String, FieldAccess> map) {
        context()._combinedFieldsFieldsFirst.put(theClass, map);
    }

    private static void putCombinedFieldsFieldFirstForSerializer(Class<?> theClass,  Map<String, FieldAccess> map) {
        context()._combinedFieldsFieldsFirstForSerializer.put(theClass, map);
    }

    static {
        try {
            if ( _useUnsafe ) {
                Field field = String.class.getDeclaredField( "value" );
            }
        } catch ( Exception ex ) {
            Exceptions.handle(ex);
        }
    }


    private static void setAccessorFieldInCache( Class<? extends Object> theClass, boolean useUnsafe, Map<String, FieldAccess> map ) {
        if ( useUnsafe ) {
            context()._allAccessorUnsafeFieldsCache.put( theClass, map );
        } else {
            context()._allAccessorReflectionFieldsCache.put( theClass, map );

        }
    }

    private static void setPropertyAccessorFieldsInCache( Class<? extends Object> theClass, Map<String, FieldAccess> map ) {
        context()._allAccessorPropertyFieldsCache.put( theClass, map );
    }


    private static Map<String, FieldAccess> getPropertyAccessorFieldsFromCache( Class<? extends Object> theClass ) {
        return context()._allAccessorPropertyFieldsCache.get( theClass );
    }

    private static Map<String, FieldAccess> getAccessorFieldsFromCache(Class<? extends Object> theClass, boolean useUnsafe) {

        if ( useUnsafe ) {
            return context()._allAccessorUnsafeFieldsCache.get( theClass );
        } else {
            return context()._allAccessorReflectionFieldsCache.get( theClass );

        }
    }




    /**
     * Gets a list of fields merges with properties if field is not found.
     *
     * @param clazz get the properties or fields
     * @return map
     */
    public static Map<String, FieldAccess> getPropertyFieldAccessMapFieldFirst( Class<?> clazz ) {
        Map<String, FieldAccess> combinedFieldsFieldFirst = getCombinedFieldsFieldFirst(clazz);

        if (combinedFieldsFieldFirst!=null) {
            return combinedFieldsFieldFirst;
        } else {

            /* Fallback map. */
            Map<String, FieldAccess> fieldsFallbacks = null;

            /* Primary merge into this one. */
            Map<String, FieldAccess> fieldsPrimary = null;


             /* Try to find the fields first if this is set. */
            fieldsPrimary = Reflection.getAllAccessorFields(clazz, true);

            fieldsFallbacks = Reflection.getPropertyFieldAccessors(clazz);


            combineFieldMaps(fieldsFallbacks, fieldsPrimary);

            combinedFieldsFieldFirst = fieldsPrimary;

            putCombinedFieldsFieldFirst(clazz, combinedFieldsFieldFirst);
            return combinedFieldsFieldFirst;

        }


    }



    /**
     * Gets a list of fields merges with properties if field is not found.
     *
     * @param clazz get the properties or fields
     * @return map
     */
    public static Map<String, FieldAccess> getPropertyFieldAccessMapFieldFirstForSerializer( Class<?> clazz ) {
        Map<String, FieldAccess> combinedFieldsFieldFirst = getCombinedFieldsFieldFirstForSerializer(clazz);

        if (combinedFieldsFieldFirst!=null) {
            return combinedFieldsFieldFirst;
        } else {

            /* Fallback map. */
            Map<String, FieldAccess> fieldsFallbacks = null;

            /* Primary merge into this one. */
            Map<String, FieldAccess> fieldsPrimary = null;


             /* Try to find the fields first if this is set. */
            fieldsPrimary = Reflection.getAllAccessorFields(clazz, true);
            fieldsFallbacks = Reflection.getPropertyFieldAccessors(clazz);

            fieldsPrimary = removeNonSerializable(fieldsPrimary);
            fieldsFallbacks = removeNonSerializable(fieldsFallbacks);

            combineFieldMaps(fieldsFallbacks, fieldsPrimary);

            combinedFieldsFieldFirst = fieldsPrimary;

            putCombinedFieldsFieldFirstForSerializer(clazz, combinedFieldsFieldFirst);
            return combinedFieldsFieldFirst;

        }


    }


    private static void combineFieldMaps( Map<String, FieldAccess> fieldsFallbacks, Map<String, FieldAccess> fieldsPrimary ) {
    /* Add missing fields */
        for ( Map.Entry<String, FieldAccess> field : fieldsFallbacks.entrySet() ) {
            if ( !fieldsPrimary.containsKey( field.getKey() ) ) {
                fieldsPrimary.put( field.getKey(), field.getValue() );
            }
        }
    }

    public static Map<String, FieldAccess> getPropertyFieldAccessMapPropertyFirst( Class<?> clazz ) {

        Map<String, FieldAccess> combinedFields = getCombinedFieldsPropertyFirst(clazz);

        if (combinedFields!=null) {
            return combinedFields;
        } else {
             /* Fallback map. */
            Map<String, FieldAccess> fieldsFallbacks = null;

            /* Primary merge into this one. */
            Map<String, FieldAccess> fieldsPrimary = null;



             /* Try to find the properties first if this is set. */
            fieldsFallbacks = Reflection.getAllAccessorFields(clazz, true);
            fieldsPrimary = Reflection.getPropertyFieldAccessors(clazz);


            /* Add missing fields */
            combineFieldMaps(fieldsFallbacks, fieldsPrimary);

            combinedFields = fieldsPrimary;
            putCombinedFieldsPropertyFirst(clazz, combinedFields);
            return combinedFields;
        }
    }


    public static Map<String, FieldAccess> getPropertyFieldAccessMapPropertyFirstForSerializer( Class<?> clazz ) {

        Map<String, FieldAccess> combinedFields = getCombinedFieldsPropertyFirstForSerializer(clazz);

        if (combinedFields!=null) {
            return combinedFields;
        } else {
             /* Fallback map. */
            Map<String, FieldAccess> fieldsFallbacks = null;

            /* Primary merge into this one. */
            Map<String, FieldAccess> fieldsPrimary = null;



             /* Try to find the properties first if this is set. */
            fieldsFallbacks = Reflection.getAllAccessorFields(clazz, true);
            fieldsFallbacks = removeNonSerializable(fieldsFallbacks);

            fieldsPrimary = Reflection.getPropertyFieldAccessors(clazz);
            fieldsPrimary = removeNonSerializable(fieldsPrimary);

            /* Add missing fields */
            combineFieldMaps(fieldsFallbacks, fieldsPrimary);

            combinedFields = fieldsPrimary;
            putCombinedFieldsPropertyFirstForSerializer(clazz, combinedFields);
            return combinedFields;
        }
    }

    private static Map<String, FieldAccess> removeNonSerializable(Map<String, FieldAccess> fieldAccessMap) {

        LinkedHashMap<String, FieldAccess> map = new LinkedHashMap<>(fieldAccessMap);
        final List<String> set = new ArrayList(fieldAccessMap.keySet());
        for (String key : set) {
            final FieldAccess fieldAccess = fieldAccessMap.get(key);
            if (fieldAccess.isStatic() || fieldAccess.ignore() ) {
                map.remove(key);
            }
        }
        return map;
    }

    @SuppressWarnings ( "serial" )
    public static class ReflectionException extends RuntimeException {

        public ReflectionException() {
            super();
        }

        public ReflectionException( String message, Throwable cause ) {
            super( message, cause );
        }

        public ReflectionException( String message ) {
            super( message );
        }

        public ReflectionException( Throwable cause ) {
            super( cause );
        }
    }


    private static void handle( Exception ex ) {
        throw new ReflectionException( ex );
    }





    public static Class<?> loadClass( String className ) {

        try {
            Class<?> clazz = Class.forName( className );


            return clazz;


        } catch ( Exception ex ) {
            log.info( String.format( "Unable to create load class %s", className ) );
            return null;
        }
    }
    public static Object newInstance( String className ) {

        try {
            Class<?> clazz = Class.forName( className );


            return newInstance( clazz );


        } catch ( Exception ex ) {
            log.info( String.format( "Unable to create this class %s", className ) );
            return null;
        }
    }

    public static <T> T newInstance( Class<T> clazz ) {
        T newInstance = null;
        ClassMeta <T> cls = ClassMeta.classMeta(clazz);

        try {
            /* See if there is a no arg constructor. */
            ConstructorAccess<T> declaredConstructor = cls.noArgConstructor();
            if (declaredConstructor !=null ) {
                /* If there was a no argument constructor, then use it. */
                newInstance = declaredConstructor.create();
            } else {
                if ( _useUnsafe ) {
                    newInstance = ( T ) getUnsafe().allocateInstance( clazz );
                } else {
                    Exceptions.die (Boon.sputs(clazz.getName(), "does not have a no arg constructor and unsafe is not turned on"));
                }

            }
        } catch ( Exception ex ) {
            try {
                if ( _useUnsafe ) {
                    newInstance = ( T ) getUnsafe().allocateInstance( clazz );
                    return newInstance; //we handled it.
                }
            } catch ( Exception ex2 ) {
                handle( ex2 );
            }

            handle( ex );
        }

        return newInstance;

    }

    public static <T> T newInstance( Class<T> clazz, Object arg ) {
        T newInstance = null;


        ClassMeta <T> cls = ClassMeta.classMeta(clazz);
         try {
            /* See if there is a no arg constructor. */
            ConstructorAccess<T> declaredConstructor = cls.declaredConstructor(arg.getClass());
            if (declaredConstructor !=null ) {
                /* If there was a no argument constructor, then use it. */
                newInstance = declaredConstructor.create(arg);
            }
        } catch ( Exception ex ) {
            handle( ex );
        }
        return newInstance;
    }

    public static Class<?> getComponentType( Collection<?> collection, FieldAccess fieldAccess ) {
        Class<?> clz = fieldAccess.getComponentClass();
        if ( clz == null ) {
            clz = getComponentType( collection );
        }
        return clz;

    }

    public static Class<?> getComponentType( Collection<?> value ) {
        if ( value.size() > 0 ) {
            Object next = value.iterator().next();
            return next.getClass();
        } else {
            return Typ.object;
        }
    }

    private static class FieldConverter implements Function<Field, FieldAccess> {

        boolean thisUseUnsafe;

        FieldConverter( boolean useUnsafe ) {
            this.thisUseUnsafe = useUnsafe;
        }

        @Override
        public FieldAccess apply( Field from ) {
            if ( useUnsafe && thisUseUnsafe ) {
                return UnsafeField.createUnsafeField(from);
            } else {
                return new ReflectField( from );
            }
        }
    }

    public static Map<String, FieldAccess> getAllAccessorFields(
            Class<? extends Object> theClass ) {
        return getAllAccessorFields( theClass, true );
    }

    public static Map<String, FieldAccess> getAllAccessorFields(
            Class<? extends Object> theClass, boolean useUnsafe ) {
        Map<String, FieldAccess> map = getAccessorFieldsFromCache(theClass, useUnsafe);
        if ( map == null ) {
            List<FieldAccess> list = Lists.mapBy( getAllFields( theClass ), new FieldConverter( useUnsafe ) );
            map = new LinkedHashMap<>( list.size() );
            for ( FieldAccess fieldAccess : list ) {
                map.put( fieldAccess.name(), fieldAccess );
            }
            setAccessorFieldInCache( theClass, useUnsafe, map );
        }
        return map;
    }


    public static List<Field> getAllFields( Class<? extends Object> theClass ) {

        try {
            List<Field> list = getFields( theClass );
            while ( theClass != Typ.object ) {

                theClass = theClass.getSuperclass();
                getFields( theClass, list );
            }
            return list;
        } catch (Exception ex) {
            return  Exceptions.handle(List.class, ex, "getAllFields the class", theClass);
        }
    }


    public static Map<String, FieldAccess> getPropertyFieldAccessors(
            Class<? extends Object> theClass ) {


        Map<String, FieldAccess> fields = getPropertyAccessorFieldsFromCache( theClass );
        if ( fields == null ) {
            Map<String, Pair<Method, Method>> methods = getPropertySetterGetterMethods( theClass );

            fields = new LinkedHashMap<>();

            for ( Map.Entry<String, Pair<Method, Method>> entry :
                    methods.entrySet() ) {

                final Pair<Method, Method> methodPair = entry.getValue();
                final String key = entry.getKey();

                PropertyField pf = new PropertyField( key, methodPair.getFirst(), methodPair.getSecond() );

                fields.put( pf.alias(), pf );

            }

            setPropertyAccessorFieldsInCache( theClass, fields );
        }


        return fields;
    }

    public static Map<String, Pair<Method, Method>> getPropertySetterGetterMethods(
            Class<? extends Object> theClass ) {

        try {
            Method[] methods = theClass.getMethods();

            Map<String, Pair<Method, Method>> methodMap = new LinkedHashMap<>( methods.length );
            List<Method> getterMethodList = new ArrayList<>( methods.length );

            for ( int index = 0; index < methods.length; index++ ) {
                Method method = methods[ index ];
                if (extractPropertyInfoFromMethodPair(methodMap, getterMethodList, method)) continue;
            }

            for ( Method method : getterMethodList ) {
                extractProperty(methodMap, method);

            }
            return methodMap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Exceptions.handle(Map.class, ex, theClass);
        }
    }

    private static boolean extractPropertyInfoFromMethodPair(Map<String, Pair<Method, Method>> methodMap,
                                                             List<Method> getterMethodList,
                                                             Method method) {
        String name = method.getName();

        try {

            if ( method.getParameterTypes().length == 1
                    && name.startsWith( "set" ) ) {
                Pair<Method, Method> pair = new Pair<>();
                pair.setFirst( method );
                String propertyName = Str.slc(name, 3);

                propertyName = Str.lower(Str.slc(propertyName, 0, 1)) + Str.slc(propertyName, 1);
                methodMap.put( propertyName, pair );
            }

            if ( method.getParameterTypes().length > 0
                    || method.getReturnType() == void.class
                    || !( name.startsWith( "get" ) || name.startsWith( "is" ) )
                    || name.equals( "getClass" ) || name.equals("get") || name.equals("is") ) {
                return true;
            }
            getterMethodList.add( method );
            return false;

        } catch (Exception ex) {
            return Exceptions.handle(Boolean.class, ex, name, method);
        }
    }

    private static void extractProperty(Map<String, Pair<Method, Method>> methodMap,
                                        Method method) {
        try {
            String name = method.getName();
            String propertyName = null;
            if ( name.startsWith( "is" ) ) {
                propertyName = name.substring( 2 );
            } else if ( name.startsWith( "get" ) ) {
                propertyName = name.substring( 3 );
            }

            propertyName = Str.lower(propertyName.substring(0, 1)) + propertyName.substring( 1 );

            Pair<Method, Method> pair = methodMap.get( propertyName );
            if ( pair == null ) {
                pair = new Pair<>();
                methodMap.put( propertyName, pair );
            }
            pair.setSecond(method);

        } catch (Exception ex) {
            Exceptions.handle(ex, "extractProperty property extract of getPropertySetterGetterMethods", method);
        }
    }

    public static void getFields( Class<? extends Object> theClass,
                                  List<Field> list ) {

        try {
            List<Field> more = getFields( theClass );
            list.addAll( more );

        }catch (Exception ex) {
            Exceptions.handle(ex, "getFields", theClass, list);
        }
    }


    public static boolean respondsTo( Class<?> type, String methodName) {
        return ClassMeta.classMeta(type).respondsTo(methodName);
    }

    public static boolean respondsTo( Class<?> type, String methodName, Class<?>... params) {
        return ClassMeta.classMeta(type).respondsTo(methodName, params);
    }


    public static boolean respondsTo( Class<?> type, String methodName, Object... params) {
        return ClassMeta.classMeta(type).respondsTo(methodName, params);
    }


    public static boolean respondsTo( Class<?> type, String methodName, List<?> params) {
        return ClassMeta.classMeta(type).respondsTo(methodName, params);
    }


    public static boolean respondsTo( Object object, String methodName) {
        if (object == null || methodName == null) {
            return false;
        }
        return ClassMeta.classMeta(object.getClass()).respondsTo(methodName);
    }

    public static boolean respondsTo( Object object, String methodName, Class<?>... params) {
        return ClassMeta.classMeta(object.getClass()).respondsTo(methodName, params);
    }


    public static boolean respondsTo( Object object, String methodName, Object... params) {
        return ClassMeta.classMeta(object.getClass()).respondsTo(methodName, params);
    }


    public static boolean respondsTo( Object object, String methodName, List<?> params) {
        return ClassMeta.classMeta(object.getClass()).respondsTo(methodName, params);
    }


    public static boolean handles( Object object, Class<?> interfaceCls) {
        return ClassMeta.classMeta(object.getClass()).handles(interfaceCls);
    }


    public static boolean handles( Class cls, Class<?> interfaceCls) {
        return ClassMeta.classMeta(cls).handles(interfaceCls);
    }


    public static Object invoke (Object object, String name, Object... args){
        return ClassMeta.classMeta( object.getClass() ).invokeUntyped(object, name, args );
    }


    public static Object invoke (Object object, String name, List<?> args){
        return ClassMeta.classMeta( object.getClass() ).invokeUntyped(object, name, args.toArray(new Object[args.size()]));
    }
}