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
import io.advantageous.boon.Lists;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.core.reflection.fields.FieldAccessMode;
import io.advantageous.boon.core.reflection.fields.FieldsAccessorFieldThenProp;
import io.advantageous.boon.core.value.ValueList;
import io.advantageous.boon.core.value.ValueMap;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.reflection.fields.FieldsAccessor;
import io.advantageous.boon.core.value.ValueContainer;
import io.advantageous.boon.core.value.ValueMapImpl;
import io.advantageous.boon.primitive.Arry;
import io.advantageous.boon.primitive.CharBuf;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.handle;
import static io.advantageous.boon.core.Conversions.coerce;
import static io.advantageous.boon.core.Conversions.toEnum;
import static io.advantageous.boon.core.TypeType.*;

/**
 * Created by Richard on 2/17/14.
 */
public class MapperComplex implements Mapper {

    private final FieldsAccessor fieldsAccessor;
    private final Set<String> ignoreSet;
    private final String view;
    private final boolean respectIgnore;
    private final boolean acceptSingleValueAsArray;
    private final boolean outputType;


    public MapperComplex(boolean outputType, FieldAccessMode fieldAccessType, boolean useAnnotations,
                         boolean caseInsensitiveFields, Set<String> ignoreSet,
                         String view, boolean respectIgnore, boolean acceptSingleValueAsArray) {
        fieldsAccessor = FieldAccessMode.create( fieldAccessType, useAnnotations, caseInsensitiveFields );
        this.ignoreSet = ignoreSet;
        this.view = view;
        this.respectIgnore = respectIgnore;
        this.acceptSingleValueAsArray = acceptSingleValueAsArray;
        this.outputType = outputType;
    }

    public MapperComplex(FieldAccessMode fieldAccessType, boolean useAnnotations,
                         boolean caseInsensitiveFields, Set<String> ignoreSet,
                         String view, boolean respectIgnore, boolean acceptSingleValueAsArray) {
        fieldsAccessor = FieldAccessMode.create( fieldAccessType, useAnnotations, caseInsensitiveFields );
        this.ignoreSet = ignoreSet;
        this.view = view;
        this.respectIgnore = respectIgnore;
        this.acceptSingleValueAsArray = acceptSingleValueAsArray;
        this.outputType = true;
    }
    public MapperComplex(FieldsAccessor fieldsAccessor, Set<String> ignoreSet, String view, boolean respectIgnore) {
        this.fieldsAccessor = fieldsAccessor;
        this.ignoreSet = ignoreSet;
        this.view = view;
        this.respectIgnore = respectIgnore;
        this.acceptSingleValueAsArray = false;
        this.outputType = true;
    }

    public MapperComplex(Set<String> ignoreSet, String view, boolean respectIgnore) {
        this.fieldsAccessor = new FieldsAccessorFieldThenProp(true);
        this.ignoreSet = ignoreSet;
        this.view = view;
        this.respectIgnore = respectIgnore;
        this.acceptSingleValueAsArray = false;
        this.outputType = true;
    }


    public MapperComplex(Set<String> ignoreSet) {
        this.fieldsAccessor = new FieldsAccessorFieldThenProp(true);;
        this.ignoreSet = ignoreSet;
        this.view = null;
        this.respectIgnore = true;
        this.acceptSingleValueAsArray = false;
        this.outputType = true;
    }

    public MapperComplex(boolean acceptSingleValueAsArray) {
        fieldsAccessor = new FieldsAccessorFieldThenProp(true);

        ignoreSet = null;
        view = null;
        respectIgnore = true;
        this.acceptSingleValueAsArray = acceptSingleValueAsArray;
        this.outputType = true;

    }

    public MapperComplex() {
        fieldsAccessor = new FieldsAccessorFieldThenProp(true);

        ignoreSet = null;
        view = null;
        respectIgnore = true;
        acceptSingleValueAsArray = false;
        this.outputType = true;
    }


    /**
         * This converts a list of maps to objects.
         * I always forget that this exists. I need to remember.
         *
         * @param list the input list
         * @param <T> generics
         * @return a new list
         */
    @Override
    public  <T> List<T> convertListOfMapsToObjects(List<Map> list, Class<T> componentType) {
        List<Object> newList = new ArrayList<>( list.size() );
        for ( Object obj : list ) {

            if ( obj instanceof Value ) {
                obj = ( ( Value ) obj ).toValue();
            }

            if ( obj instanceof Map ) {

                Map map = ( Map ) obj;
                if ( map instanceof ValueMapImpl) {
                    newList.add( fromValueMap(  ( Map<String, Value> ) map, componentType ) );
                } else {
                    newList.add( fromMap(  map, componentType ) );
                }
            } else {
                newList.add( Conversions.coerce( componentType, obj ) );
            }
        }
        return ( List<T> ) newList;
    }



    /**
     * fromMap converts a map into a java object
     * @param map map to create the object from.
     * @param cls class type of new object
     * @param <T> map to create teh object from.
     * @return new object of type cls
     */
    @Override
    public  <T> T fromMap(final Map<String, Object> map, final Class<T> cls) {


        T toObject = Reflection.newInstance( cls );
        Map<String, FieldAccess> fields = fieldsAccessor.getFields( toObject.getClass() );
        Set<Map.Entry<String, Object>> mapKeyValuesEntrySet = map.entrySet();


        /* Iterate through the map keys/values. */
        for ( Map.Entry<String, Object> mapEntry : mapKeyValuesEntrySet ) {

            /* Get the field name. */
            String key = mapEntry.getKey();

            if ( ignoreSet != null ) {
                if ( ignoreSet.contains( key ) ) {
                    continue;
                }
            }

            /* Get the field and if it missing then ignore this map entry. */
            FieldAccess field = fields.get( fieldsAccessor.isCaseInsensitive() ? key.toLowerCase() : key );


            if ( field == null ) {
                continue;
            }



            /* Check the view if it is active. */
            if ( view != null ) {
                if ( !field.isViewActive( view ) ) {
                    continue;
                }
            }


            /* Check respects ignore is active.
             * Then needs to be a chain of responsibilities.
             * */
            if ( respectIgnore ) {
                if ( field.ignore() ) {
                    continue;
                }
            }

            /* Get the value from the map. */
            Object value = mapEntry.getValue();


            /* If the value is a Value (a index overlay), then convert ensure it is not a container and inject
            it into the field, and we are done so continue.
             */
            if ( value instanceof Value ) {
                if ( ( ( Value ) value ).isContainer() ) {
                    value = ( ( Value ) value ).toValue();
                } else {
                    field.setFromValue( toObject, ( Value ) value );
                    continue;
                }
            }

            /* If the value is null, then inject an null value into the field.
            * Notice we do not check to see if the field is a primitive, if
            * it is we die which is the expected behavior.
            */
            if ( value == null ) {
                field.setObject( toObject, null );
                continue;
            }

            /* if the value's type and the field type are the same or
            the field just takes an object, then inject what we have as is.
             */
            if ( value.getClass() == field.type() || field.type() == Object.class) {
                field.setValue(toObject, value);
            } else if ( Typ.isBasicType(value) ) {

                field.setValue(toObject, value);
            }


            /* See if it is a map<string, object>, and if it is then process it.
             *  REFACTOR:
             *  It looks like we are using some utility classes here that we could have used in
             *  oldMatchAndConvertArgs.
             *  REFACTOR
              * */
            else if ( value instanceof Map ) {
                setFieldValueFromMap(toObject, field, (Map)value);
            } else if ( value instanceof Collection) {
                /*It is a collection so process it that way. */
                processCollectionFromMapUsingFields( toObject, field, ( Collection ) value);
            } else if ( value instanceof Map[] ) {
                /* It is an array of maps so, we need to process it as such. */
                processArrayOfMaps(toObject, field, ( Map<String, Object>[] )value );
            } else {
                /* If we could not determine how to convert it into some field
                object then we just go ahead an inject it using setValue which
                will call Conversion.coerce.
                 */
                field.setValue( toObject, value );
            }

        }

        return toObject;

    }



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
    @Override
    public  <T> T fromList(List<?> argList, Class<T> clazz) {

        /* Size of the arguments. */
        int size = argList.size();


        /* Meta data holder of the class. */
        ClassMeta<T> classMeta = ClassMeta.classMeta( clazz );

        /* The constructor to match. */
        ConstructorAccess<T> constructorToMatch = null;

        /* The final arguments. */
        Object[] finalArgs = null;


        boolean[] flag = new boolean[1];
        List<Object> convertedArguments = null;


        try {


        /* List to hold items that we coerce into parameter types. */
            convertedArguments  = new ArrayList<>( argList );

            constructorToMatch = lookupConstructorMeta( size,
                    convertedArguments, classMeta, constructorToMatch, flag, false);



        /* List to hold items that we coerce into parameter types. */
            if (constructorToMatch == null) {
                convertedArguments = new ArrayList<>( argList );
                constructorToMatch = lookupConstructorMeta( size,
                        convertedArguments, classMeta, constructorToMatch, flag, true);
            }




            /* If we were not able to match then we bail. */
            if ( constructorToMatch != null ) {
                finalArgs = convertedArguments.toArray( new Object[argList.size()] );
                return constructorToMatch.create( finalArgs );
            } else {
                return (T) Exceptions.die(Object.class, "Unable to convert list", convertedArguments, "into", clazz);
            }

            /* Catch all of the exceptions and try to report why this failed.
            * Since we are doing reflection and a bit of "magic", we have to be clear as to why/how things failed.
            * */
        } catch ( Exception e ) {


            if (constructorToMatch != null)  {


                CharBuf buf = CharBuf.create(200);
                buf.addLine();
                buf.multiply('-', 10).add("FINAL ARGUMENTS").multiply('-', 10).addLine();
                if (finalArgs!=null) {
                    for (Object o : finalArgs) {
                        buf.puts("argument type    ", Boon.className(o));
                    }
                }


                buf.multiply('-', 10).add("CONSTRUCTOR").add(constructorToMatch).multiply('-', 10).addLine();
                buf.multiply('-', 10).add("CONSTRUCTOR PARAMS").multiply('-', 10).addLine();
                for (Class<?> c : constructorToMatch.parameterTypes()) {
                    buf.puts("constructor type ", c);
                }

                buf.multiply('-', 35).addLine();

                if (Boon.debugOn()) {
                    puts(buf);
                }



                buf.addLine("PARAMETER TYPES");
                buf.add(Lists.list(constructorToMatch.parameterTypes())).addLine();

                buf.addLine("ORIGINAL TYPES PASSED");
                buf.add(gatherTypes(convertedArguments)).addLine();

                buf.add(gatherActualTypes(convertedArguments)).addLine();

                buf.addLine("CONVERTED ARGUMENT TYPES");
                buf.add(gatherTypes(convertedArguments)).addLine();
                buf.add(gatherActualTypes(convertedArguments)).addLine();

                //Boon.error( e, "unable to create object based on constructor", buf );


                return ( T ) handle(Object.class, e, buf.toString());
            } else {
                return ( T ) handle(Object.class, e,
                        "\nlist args after conversion", convertedArguments, "types",
                        gatherTypes(convertedArguments),
                        "\noriginal args", argList,
                        "original types", gatherTypes(argList));

            }
        }

    }





    /**
     * Processes an array of maps.
     * @param newInstance  new instance we are injecting field into
     * @param field    field we are injecting a value into
     */
    private  void processArrayOfMaps( Object newInstance, FieldAccess field, Map<String, Object>[] maps) {
        List<Map<String, Object>> list = Lists.list(maps);
        handleCollectionOfMaps(  newInstance, field,
                list);

    }


    /**
     * Processes an collection of maps.
     * @param newInstance  new instance we are injecting field into
     * @param field    field we are injecting a value into
     */
    @SuppressWarnings("unchecked")
    private  void handleCollectionOfMaps( Object newInstance,
                                                FieldAccess field, Collection<Map<String, Object>> collectionOfMaps
                                                 ) {

        Collection<Object> newCollection = Conversions.createCollection( field.type(), collectionOfMaps.size() );


        Class<?> componentClass = field.getComponentClass();

        if ( componentClass != null ) {


            for ( Map<String, Object> mapComponent : collectionOfMaps ) {

                newCollection.add( fromMap( mapComponent, componentClass ) );

            }
            field.setObject( newInstance, newCollection );

        }

    }





    private  <T> ConstructorAccess<T> lookupConstructorMeta(int size,
                                                                  List<Object> convertedArguments,
                                                                  ClassMeta<T> classMeta,
                                                                  ConstructorAccess<T> constructorToMatch,
                                                                  boolean[] flag, boolean loose) {


    /* Iterate through the constructors and see if one matches the arguments passed after coercion. */
        loop:
        for ( ConstructorAccess constructor : classMeta.constructors() ) {

            /* Get the parameters on the constructor and see if the size matches what was passed. */
            Class[] parameterTypes = constructor.parameterTypes();
            if ( parameterTypes.length == size ) {

                /* Iterate through each parameter and see if it can be converted. */
                for ( int index = 0; index < size; index++ ) {
                    /* The match and convert does the bulk of the work. */
                    if ( !matchAndConvertArgs(  convertedArguments, constructor,
                            parameterTypes, index, flag, loose ) ) continue loop;
                }
                constructorToMatch = constructor;
            }
        }
        return constructorToMatch;
    }



    /**
     * This converts/coerce a constructor argument to the given parameter type.
     *
     * REFACTOR:
     * This method was automatically refactored and its functionality gets duplicated in a few places.
     * Namely Invoker lib. It needs to be documented. Refactored to use TypeType.
     * And code coverage. I have used it on several projects and have modified to work on
     * edge cases for certain customers and have not updated the unit test.
     * This method is beastly and important. It is currently 250 lines of code.
     * It started off small, and kept getting added to. It needs love, but it was a bitch to write.
     * REFACTOR
     *
     * @param convertedArgumentList   arguments being converted to match parameter types
     * @param methodAccess    constructor
     * @param parameterTypes   parameterTypes
     * @param index           index of argument
     * @return   true or false
     */
    private boolean matchAndConvertArgs( List<Object> convertedArgumentList,
                                               BaseAccess methodAccess,
                                               Class[] parameterTypes,
                                               int index,
                                               boolean[] flag, boolean loose) {


        Object value = null;

        try {

            Class parameterClass;
            Object item;

            parameterClass = parameterTypes[index];
            item = convertedArgumentList.get( index );


            final TypeType parameterType = TypeType.getType(parameterClass);


            if ( item instanceof ValueContainer ) {
                item = ( ( ValueContainer ) item ).toValue();

                convertedArgumentList.set( index, item );
            }




            if (item == null) {
                return true;
            }

            switch (parameterType) {
                case INT:
                case SHORT:
                case BYTE:
                case BOOLEAN:
                case CHAR:
                case FLOAT:
                case DOUBLE:
                case LONG:
                    if (item == null) {
                        return false;
                    }


                case INTEGER_WRAPPER:
                case BYTE_WRAPPER:
                case SHORT_WRAPPER:
                case BOOLEAN_WRAPPER:
                case CHAR_WRAPPER:
                case FLOAT_WRAPPER:
                case DOUBLE_WRAPPER:
                case CHAR_SEQUENCE:
                case NUMBER:
                case LONG_WRAPPER:

                    if (!loose ) {
                        if (item instanceof Number) {
                            value = Conversions.coerceWithFlag(parameterType, parameterClass, flag, item );
                            convertedArgumentList.set( index, value );

                            return flag[0];
                        } else {
                            return false;
                        }

                    } else {
                        value = Conversions.coerceWithFlag(parameterType, parameterClass, flag, item );
                        convertedArgumentList.set( index, value );

                        return flag[0];

                    }



                case ENUM:


                    if (item instanceof Enum) {
                       return true;
                    }

                    if (item instanceof CharSequence) {
                        value = toEnum(parameterClass, item.toString());
                        convertedArgumentList.set( index, value );

                        return value!=null;

                    } else if (item instanceof Number){
                        value = toEnum(parameterClass, ((Number)item).intValue());
                        convertedArgumentList.set( index, value );

                        return value!=null;

                    } else {
                        return false;
                    }


                case CLASS:
                    if (item instanceof Class) {
                        return true;
                    }

                    value = Conversions.coerceWithFlag(parameterType, parameterClass, flag, item );
                    convertedArgumentList.set( index, value );

                    return flag[0];


                case STRING:

                    if (item instanceof String) {
                        return true;
                    }

                    if (item instanceof CharSequence) {

                        value = item.toString();
                        convertedArgumentList.set( index, value );
                        return true;


                    } else if (loose) {

                        value = item.toString();
                        convertedArgumentList.set( index, value );
                        return true;
                    } else {
                        return false;
                    }

                case MAP:
                case VALUE_MAP:

                    if (item instanceof Map) {
                        Map itemMap = (Map)item;

                    /* This code creates a map based on the parameterized types of the constructor arg.
                     *  This does ninja level generics manipulations and needs to be captured in some
                     *  reusable way.
                      * */
                        Type type = methodAccess.getGenericParameterTypes()[index];
                        if ( type instanceof ParameterizedType) {
                            ParameterizedType pType = (ParameterizedType) type;
                            Class<?> keyType = (Class<?>) pType.getActualTypeArguments()[0];

                            Class<?> valueType = (Class<?>) pType.getActualTypeArguments()[1];


                            Map newMap = Conversions.createMap(parameterClass, itemMap.size());


                    /* Iterate through the map items and convert the keys/values to match
                    the parameterized constructor parameter args.
                     */

                            for (Object o : itemMap.entrySet()) {
                                Map.Entry entry = (Map.Entry) o;

                                Object key = entry.getKey();
                                value = entry.getValue();

                                key = ValueContainer.toObject(key);

                                value = ValueContainer.toObject(value);


                        /* Here is the actual conversion from a list or a map of some object.
                        This can be captured in helper method the duplication is obvious.
                         */
                                if (value instanceof List) {
                                    value = fromList((List) value, valueType);

                                } else if (value instanceof Map) {
                                    value = fromMap((Map) value, valueType);

                                } else {
                                    value = coerce(valueType, value);
                                }


                                if (key instanceof List) {
                                    key = fromList((List) key, keyType);

                                } else if (value instanceof Map) {
                                    key = fromMap((Map) key, keyType);

                                } else {
                                    key = coerce(keyType, key);
                                }

                                newMap.put(key, value);
                            }
                            convertedArgumentList.set(index, newMap);
                            return true;
                        }
                    }
                    break;
                case INSTANCE:
                    if ( parameterClass.isInstance( item ) ) {
                        return true;
                    }

                    if (item instanceof Map) {
                        item = fromMap( ( Map<String, Object> ) item, parameterClass );
                        convertedArgumentList.set( index, item );
                        return true;
                    } else if ( item instanceof List ) {

                        List<Object> listItem = null;

                        listItem =      ( List<Object> ) item;

                        value = fromList(listItem, parameterClass );

                        convertedArgumentList.set( index, value );
                        return true;

                    } else {
                        convertedArgumentList.set( index, coerce( parameterClass, item ) );
                        return true;
                    }
                    //break;
                case INTERFACE:
                case ABSTRACT:
                    if ( parameterClass.isInstance( item ) ) {
                        return true;
                    }

                    if (item instanceof Map) {

                        /** Handle conversion of user define interfaces. */
                        String className = (String) ((Map) item).get("class");
                        if (className != null) {
                            item = fromMap( (Map<String, Object>) item, Reflection.loadClass(className));
                            convertedArgumentList.set(index, item);
                            return true;
                        } else {
                            return false;
                        }

                    }
                    break;


                case ARRAY:
                   item = Conversions.toList(item);
                   return true;

                case SET:
                case COLLECTION:
                case LIST:
                    if (item instanceof List ) {

                        List<Object> itemList = ( List<Object> ) item;

                        /* Items have stuff in it, the item is a list of lists.
                         * This is like we did earlier with the map.
                         * Here is some more ninja generics Java programming that needs to be captured in one place.
                         * */
                        if ( itemList.size() > 0 && (itemList.get( 0 ) instanceof List ||
                                itemList.get(0) instanceof ValueContainer)  ) {

                            /** Grab the generic type of the list. */
                            Type type = methodAccess.getGenericParameterTypes()[index];

                            /*  Try to pull the generic type information out so you can create
                               a strongly typed list to inject.
                             */
                            if ( type instanceof ParameterizedType ) {
                                ParameterizedType pType = ( ParameterizedType ) type;


                                Class<?> componentType;
                                if (! (pType.getActualTypeArguments()[0] instanceof Class)) {
                                    componentType = Object.class;
                                } else {
                                    componentType = (Class<?>) pType.getActualTypeArguments()[0];
                                }

                                Collection newList =  Conversions.createCollection( parameterClass, itemList.size() );

                                for ( Object o : itemList ) {
                                    if ( o instanceof ValueContainer ) {
                                        o = ( ( ValueContainer ) o ).toValue();
                                    }

                                    if (componentType==Object.class) {
                                        newList.add(o);
                                    } else {

                                        List fromList = ( List ) o;
                                        o = fromList(  fromList, componentType );
                                        newList.add( o );
                                    }
                                }
                                convertedArgumentList.set( index, newList );
                                return true;

                            }
                        } else {

                        /* Just a list not a list of lists so see if it has generics and pull out the
                        * type information and created a strong typed list. This looks a bit familiar.
                        * There is a big opportunity for some reuse here. */
                            Type type = methodAccess.getGenericParameterTypes()[index];
                            if ( type instanceof ParameterizedType ) {
                                ParameterizedType pType = ( ParameterizedType ) type;

                                Class<?> componentType = pType.getActualTypeArguments()[0] instanceof Class ? (Class<?>) pType.getActualTypeArguments()[0] : Object.class;

                                Collection newList =  Conversions.createCollection( parameterClass, itemList.size() );


                                for ( Object o : itemList ) {
                                    if ( o instanceof ValueContainer ) {
                                        o = ( ( ValueContainer ) o ).toValue();
                                    }
                                    if (o instanceof List) {

                                        if (componentType != Object.class) {

                                            List fromList = ( List ) o;
                                            o = fromList(fromList, componentType);
                                        }
                                        newList.add( o );
                                    } else if (o instanceof Map) {
                                        Map fromMap = ( Map ) o;
                                        o = fromMap( fromMap, componentType );
                                        newList.add( o );

                                    } else {
                                        newList.add( Conversions.coerce(componentType, o));
                                    }
                                }
                                convertedArgumentList.set( index, newList );
                                return true;

                            }

                        }
                    }
                    return false;


                default:
                    final TypeType itemType = TypeType.getInstanceType(item);

                    switch (itemType) {
                        case LIST:
                            convertedArgumentList.set(index, fromList((List<Object>) item, parameterClass));
                            return true;
                        case MAP:
                        case VALUE_MAP:
                            convertedArgumentList.set(index, fromMap( (Map<String, Object>) item, parameterClass));
                            return true;

                        case NUMBER:
                        case BOOLEAN:
                        case INT:
                        case SHORT:
                        case BYTE:
                        case FLOAT:
                        case DOUBLE:
                        case LONG:
                        case DOUBLE_WRAPPER:
                        case FLOAT_WRAPPER:
                        case INTEGER_WRAPPER:
                        case SHORT_WRAPPER:
                        case BOOLEAN_WRAPPER:
                        case BYTE_WRAPPER:
                        case LONG_WRAPPER:
                        case CLASS:
                        case VALUE:
                            value = Conversions.coerceWithFlag( parameterClass, flag, item );

                            if (flag[0] == false) {
                                return false;
                            }
                            convertedArgumentList.set( index, value );
                            return true;



                        case CHAR_SEQUENCE:
                        case STRING:

                            value = Conversions.coerceWithFlag( parameterClass, flag, item );

                            if (flag[0] == false) {
                                return false;
                            }
                            convertedArgumentList.set( index, value );
                            return true;



                    }



            }


            if ( parameterClass.isInstance( item ) ) {
                return true;
            }


        } catch (Exception ex) {
//            Boon.error(ex, "PROBLEM WITH oldMatchAndConvertArgs",
//                    "respectIgnore", respectIgnore, "view", view,
//                    "fieldsAccessor", fieldsAccessor, "list", convertedArgumentList,
//                    "constructor", methodAccess, "parameters", parameterTypes,
//                    "index", index, "ignoreSet", ignoreSet);
//
               return false;
        }

        return false;
    }



    /**
     * Processes an collection of maps.
     * This can inject into an array and appears to be using some of the TypeType lib.
     * @param newInstance  new instance we are injecting field into
     * @param field    field we are injecting a value into
     */
    @SuppressWarnings("unchecked")
    private  void handleCollectionOfValues(
         Object newInstance,
            FieldAccess field, Collection<Value> acollectionOfValues ) {

        Collection collectionOfValues = acollectionOfValues;
        if (null == collectionOfValues) {
            field.setObject(newInstance, null);
            return;
        }

        if(field.typeEnum() == INSTANCE) {

            field.setObject(newInstance, fromList((List) acollectionOfValues, field.type()));
            return;

        }

        if ( collectionOfValues instanceof ValueList) {
            collectionOfValues = ( ( ValueList ) collectionOfValues ).list();
        }


        Class<?> componentClass = field.getComponentClass();



        /** If the field is a collection than try to convert the items in the collection to
         * the field type.
         */
        switch (field.typeEnum() ) {


            case LIST:
            case SET:
            case COLLECTION:


                Collection<Object> newCollection = Conversions.createCollection( field.type(), collectionOfValues.size() );


                for ( Value value : ( List<Value> ) collectionOfValues ) {

                    if ( value.isContainer() ) {
                        Object oValue = value.toValue();
                        if ( oValue instanceof Map ) {
                            newCollection.add( fromValueMap(  ( Map ) oValue, componentClass ) );
                        }
                    } else {
                        newCollection.add( Conversions.coerce( componentClass, value.toValue() ) );
                    }


                }
                field.setObject( newInstance, newCollection );
                break;


            case ARRAY:
            case ARRAY_INT:
            case ARRAY_BYTE:
            case ARRAY_SHORT:
            case ARRAY_FLOAT:
            case ARRAY_DOUBLE:
            case ARRAY_LONG:
            case ARRAY_STRING:
            case ARRAY_OBJECT:
                TypeType componentType =  field.componentType();
                int index = 0;

                switch (componentType) {
                    case INT:
                        int [] iarray = new int[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            iarray[index] = value.intValue();
                            index++;

                        }
                        field.setObject( newInstance, iarray);
                        return;
                    case SHORT:
                        short [] sarray = new short[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            sarray[index] = value.shortValue();
                            index++;

                        }
                        field.setObject( newInstance, sarray);
                        return;
                    case DOUBLE:
                        double [] darray = new double[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            darray[index] = value.doubleValue();
                            index++;

                        }
                        field.setObject( newInstance, darray);
                        return;
                    case FLOAT:
                        float [] farray = new float[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            farray[index] = value.floatValue();
                            index++;

                        }
                        field.setObject( newInstance, farray);
                        return;

                    case LONG:
                        long [] larray = new long[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            larray[index] = value.longValue();
                            index++;

                        }
                        field.setObject( newInstance, larray);
                        return;


                    case BYTE:
                        byte [] barray = new byte[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            barray[index] = value.byteValue();
                            index++;

                        }
                        field.setObject( newInstance, barray);
                        return;


                    case CHAR:
                        char [] chars = new char[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            chars[index] = value.charValue();
                            index++;
                        }
                        field.setObject( newInstance, chars);
                        return;

                    case STRING:
                        CharBuf buffer = CharBuf.create(100);
                        String [] strings = new String[collectionOfValues.size()];
                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            strings[index] = value.stringValue(buffer);
                            index++;
                        }
                        field.setObject( newInstance, strings);
                        return;


                    default:
                        Object array = Array.newInstance(componentClass, collectionOfValues.size());
                        Object o;

                        for ( Value value : ( List<Value> ) collectionOfValues ) {
                            if (value instanceof ValueContainer) {
                                o = value.toValue();
                                if (o instanceof List) {
                                    o = fromList( (List)o, componentClass);
                                    if (componentClass.isInstance( o )) {
                                        Array.set(array, index, o);
                                    } else {
                                        break;
                                    }
                                } else if (o instanceof  Map) {
                                    o = fromMap((Map) o, componentClass);
                                    if (componentClass.isInstance( o )) {
                                        Array.set(array, index, o);
                                    } else {
                                        break;
                                    }
                                }
                            } else {
                                o = value.toValue();
                                if (componentClass.isInstance( o )) {
                                    Array.set(array, index, o);
                                } else {
                                    Array.set(array, index, Conversions.coerce( componentClass, o ));
                                }
                            }
                            index++;
                        }
                        field.setValue( newInstance, array);
                }
                break;
        }

    }


    /**
     * Creates an object from a value map.
     *
     * This does some special handling to take advantage of us using the value map so it avoids creating
     * a bunch of array objects and collections. Things you have to worry about when writing a
     * high-speed JSON serializer.
     * @return new object from value map
     */
    @Override
    @SuppressWarnings("unchecked")
    public  Object fromValueMap(final Map<String, Value> valueMap
    ) {


        try {
            String className = valueMap.get( "class" ).toString();
            Class<?> cls = Reflection.loadClass( className );
            return fromValueMap( valueMap, cls );
        } catch ( Exception ex ) {
            return handle(Object.class, sputs("fromValueMap", "map", valueMap, "fieldAccessor", fieldsAccessor), ex);
        }
    }



    /**
     * Creates an object from a value map.
     *
     * This does some special handling to take advantage of us using the value map so it avoids creating
     * a bunch of array objects and collections. Things you have to worry about when writing a
     * high-speed JSON serializer.
     * @param cls the new type
     * @return new object from value map
     */
    @Override
    @SuppressWarnings("unchecked")
    public  <T> T fromValueMap(final Map<String, Value> valueMap,
                               final Class<T> cls) {

        T newInstance = Reflection.newInstance( cls );
        ValueMap map = ( ValueMap ) ( Map ) valueMap;


        Map<String, FieldAccess> fields = fieldsAccessor.getFields( cls);
        Map.Entry<String, Object>[] entries;

        FieldAccess field = null;
        String fieldName = null;
        Map.Entry<String, Object> entry;


        int size;


        /* if the map is not hydrated get its entries right form the array to avoid collection creations. */
        if ( !map.hydrated() ) {
            size = map.len();
            entries = map.items();
        } else {
            size = map.size();
            entries = ( Map.Entry<String, Object>[] ) map.entrySet().toArray( new Map.Entry[size] );
        }

        /* guard. We should check if this is still needed.
        * I might have added it for debugging and forgot to remove it.*/
        if ( size == 0 || entries == null ) {
            return newInstance;
        }


        /* Iterate through the entries. */
        for ( int index = 0; index < size; index++ ) {
            Object value = null;
            try {

                entry    = entries[index];

                fieldName = entry.getKey();


                if ( ignoreSet != null ) {
                    if ( ignoreSet.contains( fieldName ) ) {
                        continue;
                    }
                }

                field = fields.get(fieldsAccessor.isCaseInsensitive() ? fieldName.toLowerCase() : fieldName);


                if ( field == null ) {
                    continue;
                }

                if ( view != null ) {
                    if ( !field.isViewActive( view ) ) {
                        continue;
                    }
                }


                if ( respectIgnore ) {
                    if ( field.ignore() ) {
                        continue;
                    }
                }


                value = entry.getValue();


                if ( value instanceof Value ) {
                    fromValueMapHandleValueCase( newInstance, field, ( Value ) value );
                } else {
                    fromMapHandleNonValueCase(  newInstance, field, value );
                }
            }catch (Exception ex) {
                return (T) Exceptions.handle(Object.class, ex, "fieldName", fieldName, "of class", cls, "had issues for value", value, "for field", field);
            }

        }

        return newInstance;
    }

    /**
     *
     * Gets called by  fromValueMap
     * This does some special handling to take advantage of us using the value map so it avoids creating
     * a bunch of array objects and collections. Things you have to worry about when writing a
     * high-speed JSON serializer.
     * @param field  field we want to inject something into
     * @param newInstance the thing we want to inject a field value into
     * @param objectValue object value we want to inject into the field.
     */
    private  <T> void fromMapHandleNonValueCase( T newInstance, FieldAccess field,
                                                       Object objectValue ) {
        try {
            if ( objectValue instanceof Map ) {
                Class<?> clazz = field.type();
                if ( !clazz.isInterface() && !Typ.isAbstract( clazz ) ) {
                    objectValue = fromValueMap(  ( Map<String, Value> ) objectValue, field.type() );
                } else {
                     String className = (( Map<String, Value> ) objectValue)
                             .get("class").toString();
                    Class<?> cls = Reflection.loadClass( className );

                    objectValue = fromValueMap(   ( Map<String, Value> ) objectValue, cls );
                }
                field.setValue(newInstance, objectValue);
            } else if ( objectValue instanceof Collection ) {
                handleCollectionOfValues( newInstance, field,
                        ( Collection<Value> ) objectValue );
            } else {
                field.setValue( newInstance, objectValue );
            }
        } catch ( Exception ex ) {
            Exceptions.handle(Boon.sputs("Problem handling non value case of fromValueMap", "field", field.name(),
                    "fieldType", field.type().getName(), "object from map", objectValue), ex);
        }
    }



    /**
     *
     * Gets called by  fromValueMap
     * This does some special handling to take advantage of us using the value map so it avoids creating
     * a bunch of array objects and collections. Things you have to worry about when writing a
     * high-speed JSON serializer.
     * @param field  field we want to inject something into
     * @param newInstance the thing we want to inject a field value into
     * @param value object value of type Value we want to inject into the field.
     */
    private  <T> void fromValueMapHandleValueCase(
             T newInstance, FieldAccess field, Value value  ) {


        Object objValue =
                ValueContainer.toObject(value);

        Class<?> clazz = field.type();


        switch (field.typeEnum()) {

            case OBJECT:
            case ABSTRACT:
            case INTERFACE:
                if (objValue instanceof  Map) {
                    final Map<String, Value> valueMap = (Map<String, Value>) objValue;

                    final Value aClass = valueMap.get("class");
                    clazz = Reflection.loadClass(aClass.stringValue());

                }
            case INSTANCE:
                switch (value.type()) {
                    case MAP:
                        objValue = fromValueMap( ( Map<String, Value> ) objValue, clazz );
                        break;
                    case LIST:
                        objValue = fromList((List<Object>) objValue, clazz);
                        break;


                }
                field.setValue(newInstance, objValue);

                break;

            case MAP:
            case VALUE_MAP:


                Class keyType = (Class)field.getParameterizedType().getActualTypeArguments()[0];
                Class valueType = (Class)field.getParameterizedType().getActualTypeArguments()[1];

                Map mapInner = (Map)objValue;
                Set<Map.Entry> set = mapInner.entrySet();
                Map newMap = new LinkedHashMap(  );

                for (Map.Entry entry : set) {
                    Object evalue = entry.getValue();

                    Object key = entry.getKey();

                    if (evalue instanceof ValueContainer) {
                        evalue = ((ValueContainer) evalue).toValue();
                    }

                    key  = Conversions.coerce( keyType, key );
                    evalue = Conversions.coerce( valueType, evalue );
                    newMap.put( key, evalue );
                }

                objValue = newMap;

                field.setValue(newInstance, objValue);

                break;

            case LIST:
            case COLLECTION:
            case SET:
            case ARRAY:
            case ARRAY_INT:
            case ARRAY_BYTE:
            case ARRAY_SHORT:
            case ARRAY_FLOAT:
            case ARRAY_DOUBLE:
            case ARRAY_LONG:
            case ARRAY_STRING:
            case ARRAY_OBJECT:
                if (acceptSingleValueAsArray && ValueContainer.NULL != value && !(objValue instanceof Collection)) {
                    if (objValue instanceof ValueMapImpl) {
                        objValue = Arrays.asList(new ValueContainer(objValue, MAP, false));
                    } else {
                        objValue = Arrays.asList(objValue);
                    }
                }

                handleCollectionOfValues( newInstance, field,
                        ( Collection<Value> ) objValue );

                break;

            default:
                field.setFromValue(newInstance, value);

        }
    }



    /**
     * Inject a map into an object's field.
     * @param field field we are injecting a value into
     */
    private void setFieldValueFromMap( final Object parentObject,
                                       final FieldAccess field,  final Map mapInner ) {


        Class<?> fieldClassType = field.type();
        Object value = null;

        /* Is the field not a map. */
        if ( !Typ.isMap( fieldClassType ) )  {

            if ( !fieldClassType.isInterface() && !Typ.isAbstract( fieldClassType ) ) {
                value = fromMap(  mapInner, field.type() );

            } else {
                Object  oClassName =  mapInner.get( "class" );
                if (oClassName != null)  {
                    value = fromMap( mapInner, Reflection.loadClass( oClassName.toString() ));
                } else {
                    value = null;
                }
            }

           /*
           REFACTOR:
           This is at least the third time that I have seen this code in the class.
            It was either cut and pasted or I forgot I wrote it three times.
           REFACTOR:
             */
        }  else if (Typ.isMap( fieldClassType ))  {
            Class keyType = (Class)field.getParameterizedType().getActualTypeArguments()[0];
            Class valueType = (Class)field.getParameterizedType().getActualTypeArguments()[1];

            Set<Map.Entry> set = mapInner.entrySet();
            Map newMap = new LinkedHashMap(  );

            for (Map.Entry entry : set) {
                Object evalue = entry.getValue();

                Object key = entry.getKey();

                if (evalue instanceof ValueContainer) {
                    evalue = ((ValueContainer) evalue).toValue();
                }

                key  = Conversions.coerce(keyType, key);
                evalue = Conversions.coerce( valueType, evalue );
                newMap.put( key, evalue );
            }

            value  = newMap;

        }

        field.setValue(parentObject, value);

    }



    /**
     * Helper method to extract collection of values into some field collection.
     * REFACTOR:
     * This could be refactored to use the TypeType system which should be faster.
     * REFACTOR
     * @param collection the collection we are coercing into a field value
     */
    private  void processCollectionFromMapUsingFields(final Object newInstance,
                                                      final FieldAccess field,
                                                      final Collection<?> collection ) {
        final Class<?> fieldComponentClass = field.getComponentClass();

        final Class<?> valueComponentClass = Reflection.getComponentType(collection);


        /** See if we have a collection of maps because if we do, then we have some
         * recursive processing to do.
         */
        if ( Typ.isMap( valueComponentClass ) ) {
            handleCollectionOfMaps( newInstance, field,
                    ( Collection<Map<String, Object>> ) collection );
            return;

        }

        /** See if this is a value object of some sort. */
        if ( Typ.isValue( valueComponentClass ) ) {
            handleCollectionOfValues( newInstance, field,
                    ( Collection<Value> ) collection );
            return;
        }


        /**
         * See if the collection implements the same type as the field.
         * I saw a few places that could have used this helper method earlier in the file but were not.
         */
        if (Typ.implementsInterface( collection.getClass(), field.type() )) {

            if (fieldComponentClass!=null && fieldComponentClass.isAssignableFrom(valueComponentClass)) {
                field.setValue(newInstance, collection);

                return;
            }

        }

        /** See if this is some sort of collection.
         * TODO we need a coerce that needs a respectIgnore
         *
         * REFACTOR:
         * Note we are assuming it is a collection of instances.
         * We don't handle enums here.
         *
         * We do in other places.
         *
         * We handle all sorts of generics but not here.
         *
         * REFACTOR
         *
         **/
        if (!field.typeEnum().isCollection()) {
            if (collection instanceof List) {
                try {
                    Object value = fromList( (List) collection, field.getComponentClass());
                    field.setValue(newInstance, value);
                } catch  (Exception ex) {
                    //There is an edge case that needs this. We need a coerce that takes respectIngore, etc.
                    field.setValue(newInstance, collection);
                }
            } else {
                field.setValue(newInstance, collection);
            }
            return;
        }


        /**
         * Create a new collection. if the types already match then just copy them over.
         * Note that this is currently untyped in the null case.
         * We are relying on the fact that the field.setValue calls the Conversion.coerce.
         */
        Collection<Object> newCollection = Conversions.createCollection( field.type(), collection.size() );

        if ( fieldComponentClass == null || fieldComponentClass.isAssignableFrom(valueComponentClass)) {

            newCollection.addAll(collection);
            field.setValue( newInstance, newCollection );
            return;
        }



        /* Here we try to do the coercion for each individual collection item. */
        for (Object itemValue : collection) {
            newCollection.add(Conversions.coerce(fieldComponentClass, itemValue));
            field.setValue(newInstance, newCollection);
        }

    }


    /**
     * fromMap converts a map into a Java object.
     * This version will see if there is a class parameter in the map, and dies if there is not.
     * @param map map to create the object from.
     * @return new object
     */
    @Override
    public  Object fromMap(Map<String, Object> map) {
        String clazz = (String) map.get( "class" );
        Class cls = Reflection.loadClass( clazz );
        return fromMap(map, cls);
    }




    /**
     * This could be refactored to use core.TypeType class and it would run faster.
     * Converts an object into a map
     * @param object the object that we want to convert
     * @return map map representation of the object
     */
    @Override
    public Map<String, Object> toMap(final Object object) {

        if ( object == null ) {
            return null;
        }

        if (object instanceof Map) {
            return (Map<String, Object>) object;
        }
        Map<String, Object> map = new LinkedHashMap<>();



        final Map<String, FieldAccess> fieldMap = Reflection.getAllAccessorFields( object.getClass() );
        List<FieldAccess> fields = new ArrayList( fieldMap.values() );


        Collections.reverse( fields ); // make super classes fields first that



        if (outputType) {
            map.put("class", object.getClass().getName());
        }

        for ( FieldAccess access : fields ) {

            String fieldName = access.name();

            if (access.isStatic()) {
                continue;
            }

            if (ignoreSet!=null) {
                if ( ignoreSet.contains( fieldName ) ) {
                    continue;
                }
            }

            Object value = access.getValue(object);


            if ( value == null ) {
                continue;
            }


            switch (access.typeEnum()) {
                case BYTE:
                case BYTE_WRAPPER:
                case SHORT:
                case SHORT_WRAPPER:
                case INT:
                case INTEGER_WRAPPER:
                case LONG:
                case LONG_WRAPPER:
                case FLOAT:
                case FLOAT_WRAPPER:
                case DOUBLE:
                case DOUBLE_WRAPPER:
                case CHAR:
                case CHAR_WRAPPER:
                case BIG_DECIMAL:
                case BIG_INT:
                case BOOLEAN:
                case BOOLEAN_WRAPPER:
                case CURRENCY:
                case CALENDAR:
                case DATE:
                    map.put( fieldName, value );
                    break;

                case ARRAY:
                case ARRAY_INT:
                case ARRAY_BYTE:
                case ARRAY_SHORT:
                case ARRAY_FLOAT:
                case ARRAY_DOUBLE:
                case ARRAY_LONG:
                case ARRAY_STRING:
                case ARRAY_OBJECT:
                    if (Typ.isBasicType( access.getComponentClass() ))  {
                        map.put(fieldName, value);
                    } else {
                        int length = Arry.len(value);
                        List<Map<String, Object>> list = new ArrayList<>( length );
                        for ( int index = 0; index < length; index++ ) {
                            Object item = Arry.fastIndex( value, index );
                            list.add( toMap( item ) );
                        }
                        map.put( fieldName, list );
                    }
                    break;
                case COLLECTION:
                case LIST:
                case SET:
                    Collection<?> collection = ( Collection<?> ) value;
                    Class<?> componentType = access.getComponentClass();
                    if ( Typ.isBasicType( componentType ) ) {
                        map.put(fieldName, value);
                    } else if (Typ.isEnum(componentType)) {
                        List<String> list = new ArrayList<>(
                                collection.size() );
                        for ( Object item : collection ) {
                            if ( item != null ) {
                                list.add( item.toString() );
                            }
                        }
                        map.put( fieldName, list );

                    } else {
                        List<Map<String, Object>> list = new ArrayList<>(
                                collection.size() );
                        for ( Object item : collection ) {
                            if ( item != null ) {
                                list.add( toMap( item ) );
                            }
                        }
                        map.put( fieldName, list );
                    }
                    break;
                case MAP:
                    map.put(fieldName, value);
                    break;

                case INSTANCE:
                    map.put(fieldName, toMap(value));
                    break;


                case INTERFACE:
                case ABSTRACT:
                    final Map<String, Object> abstractMap = toMap(value);
                    abstractMap.put("class", Boon.className(value));
                    map.put(fieldName, abstractMap);
                    break;

                case ENUM:
                    map.put(fieldName, value);
                    break;




                default:
                    map.put(fieldName, Conversions.toString(value));
                    break;





            }

        }
        return map;


    }


//
//    /**
//     * Converts a field access set into a collection of map entries.
//     */
//    public static class FieldToEntryConverter implements
//            Function<FieldAccess, Entry<String, Object>> {
//
//        final Object object;
//
//        public FieldToEntryConverter(Object object) {
//            this.object = object;
//        }
//
//        @Override
//        public Entry<String, Object> apply( FieldAccess from ) {
////            if ( from.isReadOnly() ) {
////                return null;
////            }
//            Entry<String, Object> entry = new Pair<>( from.name(),
//                    from.getValue( object ) );
//            return entry;
//        }
//    }



    /**
     * Creates a list of maps from a list of class instances.
     * @param collection  the collection we are coercing into a field value
     * @return the return value.
     */
    @Override
    public  List<Map<String, Object>> toListOfMaps(Collection<?> collection) {
        List<Map<String, Object>> list = new ArrayList<>();
        for ( Object o : collection ) {
            list.add( toMap( o ) );
        }
        return list;
    }


    /** Convert an object to a list.
     *
     * @param object the object we want to convert to a list
     * @return new list from an object
     */
    @Override
    public  List<?> toList(Object object) {

        TypeType instanceType = TypeType.getInstanceType(object);

        switch (instanceType) {
            case NULL:
                return Lists.list((Object)null);


            case ARRAY:
            case ARRAY_INT:
            case ARRAY_BYTE:
            case ARRAY_SHORT:
            case ARRAY_FLOAT:
            case ARRAY_DOUBLE:
            case ARRAY_LONG:
            case ARRAY_STRING:
            case ARRAY_OBJECT:
                return Conversions.toList(object);

            case INSTANCE:
                if (Reflection.respondsTo(object, "toList")) {
                    return (List<?>) Reflection.invoke(object, "toList");
                }
                break;
        }
        return Lists.list(object);
    }


}