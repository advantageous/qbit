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
import io.advantageous.boon.core.reflection.fields.FieldAccessMode;
import io.advantageous.boon.Lists;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.fields.FieldsAccessor;
import io.advantageous.boon.core.value.ValueContainer;
import io.advantageous.boon.primitive.CharBuf;

import java.lang.invoke.ConstantCallSite;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Exceptions.handle;
import static io.advantageous.boon.core.Conversions.coerce;
import static io.advantageous.boon.core.TypeType.gatherTypes;
import static io.advantageous.boon.core.reflection.MapObjectConversion.*;

/**
 *
 * @author Rick Hightower
 *         Created by Richard on 2/17/14.
 */
public class Invoker {


    public static Object invokeOverloadedFromObject(Object object, String name, Object args) {
        return invokeOverloadedFromObject(false, null, null, object, name, args);
    }

    public static Object invokeOverloadedFromObject(boolean respectIgnore, String view,
                                                    Set<String> ignoreProperties,
                                                    Object object, String name,
                                                    Object args) {

        try {
            if (args instanceof Map) {
                return invokeOverloadedFromList(respectIgnore, view, ignoreProperties, object, name, Lists.list(args));
            } else if (args instanceof List) {
                List list = (List) args;
                ClassMeta classMeta = ClassMeta.classMeta(object.getClass());
                MethodAccess m = classMeta.method(name);
                if (m.parameterTypes().length == 1 && list.size() > 0) {

                    Object firstArg = list.get(0);
                    if (firstArg instanceof Map || firstArg instanceof List) {
                        return invokeOverloadedFromList(respectIgnore, view, ignoreProperties, object, name, list);

                    } else {
                        return invokeOverloadedFromList(respectIgnore, view, ignoreProperties, object, name, Lists.list(args));
                    }
                } else {

                    return invokeOverloadedFromList(respectIgnore, view, ignoreProperties, object, name, list);

                }
            } else if (args == null) {
                return invoke(object, name);
            } else {
                return invokeOverloadedFromList(respectIgnore, view, ignoreProperties, object, name, Lists.list(args));
            }
        }
        catch(Exception ex) {
            return Exceptions.handle(Object.class, ex, "Unable to invoke method object", object, "name", name, "args", args);
        }
    }


    public static Object invokeFromObject(Object object, String name, Object args) {
        return invokeFromObject(false, null, null, object, name, args);

    }


    public static Object invokeFromObject(Class<?> cls, String name, Object args) {
        return invokeFromObject(false, null, null, cls, null, name, args);

    }


    /**
     * Invokes method from list or map depending on what the Object arg is.
     * @param object object
     * @param method method
     * @param args args
     * @return result
     */
    public static Object invokeMethodFromObjectArg(Object object, MethodAccess method, Object args) {
        return invokeMethodFromObjectArg(false, null, null, object, method, args);

    }


    public static Object invokeMethodFromObjectArg(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                          Object object, MethodAccess method, Object args) {

        try {
            if (args instanceof Map) {
                return invokeMethodFromList(respectIgnore, view, ignoreProperties, object, method, Lists.list(args));
            } else if (args instanceof List) {
                List list = (List) args;

                Class<?>[] paramTypes = method.parameterTypes();

                if (paramTypes.length == 1 && list.size() > 0) {

                    Class<?> firstParamType = paramTypes[0];
                    Object firstArg = list.get(0);


                    if ( firstArg instanceof Map ) {
                        return invokeMethodFromList(respectIgnore, view, ignoreProperties, object, method, list);

                    }

                    else if (firstArg instanceof List &&
                            !Typ.isCollection(firstParamType)
                            && !firstParamType.isArray()) {
                        return invokeMethodFromList(respectIgnore, view, ignoreProperties, object, method, list);
                    }
                    else {
                        return invokeMethodFromList(respectIgnore, view, ignoreProperties, object, method,
                                Lists.list(args));
                    }
                } else {

                    return invokeMethodFromList(respectIgnore, view, ignoreProperties, object, method, list);

                }
            } else if (args == null) {
                return method.invoke(object);
            } else {
                return invokeMethodFromList(respectIgnore, view, ignoreProperties, object, method, Lists.list(args));
            }
        }catch (Exception ex) {
            return Exceptions.handle(Object.class, ex, "Unable to invoke method object", object, "method", method, "args", args);

        }

    }



    public static Object invokeFromObject(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                          Object object, String name, Object args) {
        return invokeFromObject(respectIgnore, view, ignoreProperties, object.getClass(), object, name, args);

    }

    public static Object invokeFromObject(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                          Class<?> cls, Object object, String name, Object args) {

        try {
            if (args instanceof Map) {
                return invokeFromList(respectIgnore, view, ignoreProperties, cls, object, name, Lists.list(args));
            } else if (args instanceof List) {
                List list = (List) args;
                ClassMeta classMeta = ClassMeta.classMeta(cls);
                MethodAccess m = classMeta.method(name);
                if (m.parameterTypes().length == 1 && list.size() > 0) {

                    Object firstArg = list.get(0);
                    if (firstArg instanceof Map || firstArg instanceof List) {
                        return invokeFromList(respectIgnore, view, ignoreProperties, cls, object, name, list);

                    } else {
                        final Class<?> aClass = m.parameterTypes()[0];
                        final TypeType type = TypeType.getType(aClass);
                        switch (type) {
                            case INSTANCE:
                                return invokeFromList(respectIgnore, view, ignoreProperties, cls, object, name, Lists.list(args));

                            default:
                                return invokeFromList(respectIgnore, view, ignoreProperties, cls, object, name, list);

                        }
                    }
                } else {

                    return invokeFromList(respectIgnore, view, ignoreProperties, cls, object, name, list);

                }
            } else if (args == null) {
                return invoke(object, name);
            } else {
                return invokeFromList(respectIgnore, view, ignoreProperties, cls, object, name, Lists.list(args));
            }
        } catch (Exception ex) {
            return Exceptions.handle(Object.class, ex, "Unable to invoke method object", object, "name", name, "args", args);
        }

    }

    public static Object invokeFromList(Object object, String name, List<?> args) {
        return invokeFromList(true, null, null, object, name, args);
    }


    public static Object invokeFromList(Class<?> cls, String name, List<?> args) {
        return invokeFromList(true, null, null, cls, null, name, args);
    }

    public static Object invokeFromList(boolean respectIgnore, String view, Set<String> ignoreProperties, Object object, String name, List<?> args) {


        return invokeFromList(respectIgnore, view, ignoreProperties, object.getClass(), object, name, args);

    }

    private static Object[] convertArguments(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                                 Object object,  List<?> argsList, MethodAccess methodAccess
                                                  ) {

        List<Object> convertedArguments = new ArrayList(argsList);
        Class<?>[] parameterTypes = methodAccess.parameterTypes();

        boolean[] flag = new boolean[1];

        if (convertedArguments.size() != parameterTypes.length) {
            return Exceptions.die(Object[].class, "The list size does not match the parameter" +
                    " length of the method. Unable to invoke method", methodAccess.name(), "on object", object, "with arguments", convertedArguments);
        }

        FieldsAccessor fieldsAccessor = FieldAccessMode.FIELD.create(true);



        for (int index = 0; index < parameterTypes.length; index++) {

            if (!matchAndConvertArgs(respectIgnore, view, fieldsAccessor, convertedArguments, methodAccess, parameterTypes, index, ignoreProperties, flag, true)) {
                return Exceptions.die(Object[].class, index, "Unable to invoke method as argument types did not match",
                        methodAccess.name(), "on object", object, "with arguments", convertedArguments,
                        "\nValue at index = ", convertedArguments.get(index));
            }

        }

        return convertedArguments.toArray(new Object[convertedArguments.size()]);
    }


    public static Object invokeFromList(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                        Class<?> cls, Object object,
                                        String name, List<?> argsList) {




        Object[] finalArgs=null;
        ClassMeta classMeta;
        MethodAccess methodAccess;


        classMeta = ClassMeta.classMeta(cls);
        methodAccess = classMeta.method(name);


        try {
            finalArgs = convertArguments(respectIgnore, view, ignoreProperties,
                    object, argsList, methodAccess
                    );

            return methodAccess.invoke(object, finalArgs);


        } catch (Exception ex) {



            if (methodAccess != null)  {


                CharBuf buf = CharBuf.create(200);
                buf.addLine();
                buf.multiply('-', 10).add("FINAL ARGUMENTS").multiply('-', 10).addLine();
                if (finalArgs!=null) {
                    for (Object o : finalArgs) {
                        buf.puts("argument type    ", Boon.className(o));
                    }
                }


                buf.multiply('-', 10).add("INVOKE METHOD").add(methodAccess).multiply('-', 10).addLine();
                buf.multiply('-', 10).add("INVOKE METHOD PARAMS").multiply('-', 10).addLine();
                for (Class<?> c : methodAccess.parameterTypes()) {
                    buf.puts("constructor type ", c);
                }

                buf.multiply('-', 35).addLine();

                if (Boon.debugOn()) {
                    puts(buf);
                }



                return  handle(Object.class, ex, buf.toString(),
                        "\nconstructor parameter types", methodAccess.parameterTypes(),
                        "\noriginal args\n", argsList,
                        "\nlist args after conversion", finalArgs, "\nconverted types\n",
                        gatherTypes(finalArgs),
                        "original types\n", gatherTypes(argsList), "\n");
            } else {
                return  handle(Object.class, ex,
                        "\nlist args after conversion", finalArgs, "types",
                        gatherTypes(finalArgs),
                        "\noriginal args", argsList,
                        "original types\n", gatherTypes(argsList), "\n");

            }


        }


    }


    public static Object invokeMethodFromList(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                              Object object, MethodAccess method, List<?> argsList) {

        try {


            if (argsList == null && method.parameterTypes().length == 0) {
                return method.invoke(object);
            } else {

                Object [] finalArgs = convertArguments(respectIgnore, view, ignoreProperties,
                        object, argsList, method
                );


                return method.invoke(object, finalArgs);
            }
        }catch (Exception ex) {
            return Exceptions.handle(Object.class, ex, "Unable to invoke method object", object, "method", method, "args", argsList);
        }

    }


    public static Object invokeEither(Object object, String name, Object... args) {
        if (object instanceof Class) {
            return invoke((Class<?>)object, name, args);
        } else {
            return invoke(object, name, args);
        }
    }

    public static Object invoke(Object object, String name, Object... args) {
        return ClassMeta.classMetaUnTyped(object.getClass()).invokeUntyped(object, name, args);
    }


    public static MethodAccess invokeMethodAccess( Object object, String name ) {
        return ClassMeta.classMeta(object.getClass()).invokeMethodAccess(name);
    }


    public static MethodAccess invokeMethodAccess(Class<?> cls, String name) {
        return ClassMeta.classMeta(cls).invokeMethodAccess(name);
    }

    public static Object invoke(Class cls, String name, Object... args) {
        return ClassMeta.classMeta(cls).invokeStatic(name, args);
    }


    public static Object invokeOverloaded(Object object, String name, Object... args) {
        ClassMeta classMeta = ClassMeta.classMeta(object.getClass());
        Iterable<MethodAccess> invokers = classMeta.methods(name);

        for (MethodAccess m : invokers) {
            if (m.respondsTo(args)) {
                return m.invoke(object, args);
            }
        }
        return Exceptions.die(Object.class, "Unable to invoke method", name, "on object", object, "with arguments", args);
    }


    public static Object invokeOverloadedFromList(Object object, String name, List<?> args) {

        return invokeOverloadedFromList(true, null, null, object, name, args);

    }

    public static Object invokeOverloadedFromList(boolean respectIgnore,
                                                  String view,
                                                  Set<String> ignoreProperties,
                                                  Object object,
                                                  String name,
                                                  List<?> args) {
        ClassMeta classMeta = ClassMeta.classMeta(object.getClass());
        Iterable<MethodAccess> invokers = classMeta.methods(name);

        List<Object> list = new ArrayList(args);
        FieldsAccessor fieldsAccessor = FieldAccessMode.FIELD.create(true);

        boolean[] flag = new boolean[1];

        MethodAccess method = lookupOverloadedMethod(respectIgnore, view, ignoreProperties, invokers, list, fieldsAccessor, flag, false);

        if (method == null) {
            method = lookupOverloadedMethod(respectIgnore, view, ignoreProperties, invokers, list, fieldsAccessor, flag, true);
        }

        if (method!=null) {
            return method.invoke(object, list.toArray(new Object[list.size()]));
        } else {
            return Exceptions.die(Object.class, "Unable to invoke method", name, "on object", object, "with arguments", args);
        }
    }

    private static
        MethodAccess
        lookupOverloadedMethod(boolean respectIgnore, String view, Set<String> ignoreProperties,
                                               Iterable<MethodAccess> invokers, List<Object> list,
                                               FieldsAccessor fieldsAccessor, boolean[] flag, boolean loose) {

        MethodAccess method = null;

        loop:
        for (MethodAccess m : invokers) {
            Class<?>[] parameterTypes = m.parameterTypes();
            if (!(parameterTypes.length == list.size())) {
                continue;
            }
            for (int index = 0; index < parameterTypes.length; index++) {
                if (!matchAndConvertArgs(respectIgnore, view, fieldsAccessor, list, m,
                        parameterTypes, index, ignoreProperties, flag, loose)) {

                    continue loop;
                }
            }
            method = m;
            break;
        }

        return method;
    }

    public static void invokeMethodWithAnnotationNoReturn(Object object, String annotation) {
        invokeMethodWithAnnotationWithReturnType(object, annotation, void.class);
    }

    public static void invokeMethodWithAnnotationWithReturnType(Object object, String annotation, Class<?> returnType) {
        invokeMethodWithAnnotationWithReturnType(object.getClass(), object, annotation, returnType);
    }

    public static void invokeMethodWithAnnotationWithReturnType(Class<?> type, Object object, String annotation, Class<?> returnType) {
        ClassMeta classMeta = ClassMeta.classMeta(type);
        Iterable<MethodAccess> iterate = classMeta.methods();
        for (MethodAccess m : iterate) {
            if (m.hasAnnotation(annotation)) {
                    if (m.parameterTypes().length == 0 && m.returnType() == void.class) {
                        m.invoke(object);
                        break;
                    }
            }
        }
    }


    public static <T> boolean invokeBooleanReturn(Object object, T v) {
        Class cls;
        Object instance = null;
        if (object instanceof  Class) {
            cls = (Class) object;
        } else {
            cls = object.getClass();
            instance = object;
        }

        ClassMeta meta = ClassMeta.classMeta(cls);
        return meta.invokePredicate(instance, v);

    }

    public static Object invokeReducer(Object object, Object sum, Object value) {
        if (object instanceof  Class) {
            ClassMeta meta = ClassMeta.classMeta((Class<?>)object);
            return meta.invokeReducer(null, sum, value);
        } else {
            ClassMeta meta = ClassMeta.classMeta(object.getClass());

            return meta.invokeReducer(object, sum, value);

        }
    }

    public static Object invokeFunction(Object object, Object arg) {

        if (object instanceof  Class) {
            ClassMeta meta = ClassMeta.classMeta((Class<?>)object);
            return meta.invokeFunction(null, arg);
        } else {
            ClassMeta meta = ClassMeta.classMeta(object.getClass());

            return meta.invokeFunction(object, arg);

        }
    }



    public static MethodAccess invokeFunctionMethodAccess(Object object) {

        if (object instanceof  Class) {
            ClassMeta meta = ClassMeta.classMeta((Class<?>)object);
            return meta.invokeFunctionMethodAccess();
        } else {
            ClassMeta meta = ClassMeta.classMeta(object.getClass());

            return meta.invokeFunctionMethodAccess();

        }
    }

    public static ConstantCallSite invokeReducerLongIntReturnLongMethodHandle(Object object ) {

            ClassMeta meta = ClassMeta.classMeta(object.getClass());
            return meta.invokeReducerLongIntReturnLongMethodHandle(object);
    }


    public  static <T> ConstantCallSite invokeReducerLongIntReturnLongMethodHandle(T object, String methodName ) {

        ClassMeta meta = ClassMeta.classMeta(object.getClass());
        return meta.invokeReducerLongIntReturnLongMethodHandle(object, methodName);
    }

    public static Method invokeReducerLongIntReturnLongMethod(Object object ) {

        ClassMeta meta = ClassMeta.classMeta(object.getClass());
        return meta.invokeReducerLongIntReturnLongMethod(object);
    }


    public  static <T> Method invokeReducerLongIntReturnLongMethod(T object, String methodName ) {

        ClassMeta meta = ClassMeta.classMeta(object.getClass());
        return meta.invokeReducerLongIntReturnLongMethod(object, methodName);
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
     * @param view honor views for fields
     * @param fieldsAccessor how we are going to access the fields (by field, by property, combination)
     * @param ignoreSet a set of properties to ignore
     * @param respectIgnore  honor @JsonIgnore, transients, etc. of the field
     * @param convertedArgumentList   arguments being converted to match parameter types
     * @param methodAccess    constructor
     * @param parameterTypes   parameterTypes
     * @param index           index of argument
     * @param flag flag
     * @param loose loose
     * @return   true or false
     */
    public static boolean matchAndConvertArgs( boolean respectIgnore,
                                               String view,
                                               FieldsAccessor fieldsAccessor,
                                               List<Object> convertedArgumentList,
                                               BaseAccess methodAccess,
                                               Class[] parameterTypes,
                                               int index,
                                               Set<String> ignoreSet,
                                               boolean[] flag, boolean loose) {


        Object value;

        try {

            Class parameterClass;
            Object item;

            parameterClass = parameterTypes[index];
            item = convertedArgumentList.get( index );


            final TypeType parameterType = TypeType.getType(parameterClass);


            if ( item instanceof ValueContainer) {
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




                case CLASS:
                case ENUM:
                case STRING:
                    if (!loose && !(item instanceof CharSequence)) {
                        return false;
                    }


                    value = Conversions.coerceWithFlag(parameterType, parameterClass, flag, item );

                    if (flag[0] == false) {
                        return false;
                    }
                    convertedArgumentList.set( index, value );
                    return true;

                case MAP:
                case VALUE_MAP:

                    if (item instanceof Map) {
                        Map itemMap = (Map)item;

                    /* This code creates a map based on the parameterized types of the constructor arg.
                     *  This does ninja level generics manipulations and needs to be captured in some
                     *  reusable way.
                      * */
                        java.lang.reflect.Type type = methodAccess.getGenericParameterTypes()[index];
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
                                    value = fromList(respectIgnore, view, fieldsAccessor, (List) value, valueType, ignoreSet);

                                } else if (value instanceof Map) {
                                    value = fromMap(respectIgnore, view, fieldsAccessor, (Map) value, valueType, ignoreSet);

                                } else {
                                    value = coerce(valueType, value);
                                }


                                if (key instanceof List) {
                                    key = fromList(respectIgnore, view, fieldsAccessor, (List) key, keyType, ignoreSet);

                                } else if (value instanceof Map) {
                                    key = fromMap(respectIgnore, view, fieldsAccessor, (Map) key, keyType, ignoreSet);

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
                        item = fromMap( respectIgnore, view, fieldsAccessor, ( Map<String, Object> ) item, parameterClass, ignoreSet );
                        convertedArgumentList.set( index, item );
                        return true;
                    } else if ( item instanceof List ) {

                        List<Object> listItem = null;

                        listItem =      ( List<Object> ) item;

                        value = fromList(respectIgnore, view, fieldsAccessor, listItem, parameterClass, ignoreSet );

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
                            item = fromMap(respectIgnore, view, fieldsAccessor, (Map<String, Object>) item, Reflection.loadClass(className), ignoreSet);
                            convertedArgumentList.set(index, item);
                            return true;
                        } else {
                            return false;
                        }

                    }
                    break;


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
                            java.lang.reflect.Type type = methodAccess.getGenericParameterTypes()[index];

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
                                        o = fromList( respectIgnore, view, fieldsAccessor, fromList, componentType, ignoreSet );
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
                            java.lang.reflect.Type type = methodAccess.getGenericParameterTypes()[index];
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
                                            o = fromList(fieldsAccessor, fromList, componentType);
                                        }
                                        newList.add( o );
                                    } else if (o instanceof Map) {
                                        Map fromMap = ( Map ) o;
                                        o = fromMap(respectIgnore, view, fieldsAccessor, fromMap, componentType, ignoreSet);
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
                            convertedArgumentList.set(index, fromList(respectIgnore, view, fieldsAccessor, (List<Object>) item, parameterClass, ignoreSet));
                        case MAP:
                        case VALUE_MAP:
                            convertedArgumentList.set(index, fromMap(respectIgnore, view, fieldsAccessor, (Map<String, Object>) item, parameterClass, ignoreSet));

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
//            return false;
            return false;
        }

        return false;
    }



}
