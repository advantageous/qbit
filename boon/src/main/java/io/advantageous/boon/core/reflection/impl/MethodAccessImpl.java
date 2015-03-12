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

package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.Annotations;
import io.advantageous.boon.core.reflection.Invoker;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.Lists;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.primitive.Arry;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Richard on 2/17/14.
 */
public class MethodAccessImpl implements MethodAccess {

    final public Method method;
    final List<AnnotationData> annotationData;

    final List<List<AnnotationData>> annotationDataForParams;
    final Map<String, AnnotationData> annotationMap;


    final List<TypeType> paramTypeEnumList = new ArrayList<>();


    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle methodHandle;

    Object instance;
    private int score;

    public MethodAccessImpl() {
        method=null;
        annotationData=null;
        annotationMap=null;
        methodHandle = null;
        this.annotationDataForParams = null;
    }

    public List<List<AnnotationData>> annotationDataForParams() {
        return annotationDataForParams;
    }


    public MethodAccessImpl( Method method ) {
        this.method = method;
        this.method.setAccessible( true );
        this.annotationData = Annotations.getAnnotationDataForMethod(method);
        this.annotationDataForParams = Annotations.getAnnotationDataForMethodParams(method);



        for (Class<?> cls : method.getParameterTypes()) {
            paramTypeEnumList.add(TypeType.getType(cls));

        }


        annotationMap = new ConcurrentHashMap<>(  );
        for (AnnotationData data : annotationData) {
            annotationMap.put( data.getName(), data );
            annotationMap.put( data.getSimpleClassName(), data );
            annotationMap.put( data.getFullClassName(), data );
        }

        score(method);

    }

    private void score(Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        int index=0;

        for (Class<?> paramType : parameterTypes) {


            if (paramType.isPrimitive()) {
                score+=100;
                continue;
            }
            final TypeType type = this.paramTypeEnumList.get(index);

            switch (type) {

                case LONG_WRAPPER:
                    score+=85;
                    break;

                case INTEGER_WRAPPER:
                    score+=75;
                    break;

                case SHORT_WRAPPER:
                case BYTE_WRAPPER:
                    score+=65;
                    break;

                case BOOLEAN_WRAPPER:
                    score+=60;
                    break;


                case FLOAT_WRAPPER:
                    score+=55;
                    break;

                case DOUBLE_WRAPPER:
                    score+=50;
                    break;

                case BIG_INT:
                    score+=45;
                    break;


                case BIG_DECIMAL:
                    score+=40;
                    break;

                case STRING:
                    score+=30;
                    break;


                case INSTANCE:
                    score+=25;
                    break;


            }

            index++;
        }

        if (method.isVarArgs()) {
            score += -10_000;
        }


    }


    public Object invokeDynamicList(final Object object, List<?> args) {

        return invokeDynamic(object, Arry.objectArray(args));
    }

    public Object invokeDynamicObject(final Object object, final Object args) {

        if (args instanceof List) {
            return invokeDynamicList(object, (List)args);
        } else {
            return invokeDynamic(object, args);
        }
    }

    @Override
    public Object invokeDynamic(final Object object, final Object... args) {

        final Class<?>[] parameterTypes = parameterTypes();
        final int paramLength = parameterTypes.length;
        final int argsLength = args.length;


            /* If there are no parameters, just invoke it. */
        if (paramLength == 0) {
            return invoke(object);

        }

        if (paramLength == argsLength) {

            if (argsLength == 1) {

                Object arg = args[0];
                Class<?> paramType = parameterTypes[0];
                if (!paramType.isInstance(arg)) {
                    TypeType type = paramTypeEnumList.get(0);
                    arg = Conversions.coerce(type, paramType, arg);
                }

                return invoke(object, arg);
            }
            /* If the paramLength and argument are greater than one and
            sizes match then invoke using invokeFromList. */
            else  {

                Object[] newArgs = new Object[argsLength];

                for (int index = 0; index < argsLength; index++) {

                    Object arg = args[index];
                    Class<?> paramType = parameterTypes[index];

                    if (!paramType.isInstance(arg)) {
                        TypeType type = paramTypeEnumList.get(index);
                        newArgs[index] = Conversions.coerce(type, paramType, arg);
                    } else {
                        newArgs[index] = arg;
                    }

                }


                return invoke(object, newArgs);

            }
        }else {
            if (method.isVarArgs() && paramLength == 1) {

                return this.invoke(object, (Object)args);
            } else {
                return Invoker.invokeOverloadedFromList(object, name(), Lists.list(args));
            }

        }

    }

    public Object invoke(Object object, Object... args) {
        try {

            return method.invoke( object, args );
        } catch (InvocationTargetException invocationTargetException) {

            return Exceptions.handle(Object.class, invocationTargetException.getTargetException(), "unable to invoke method", method,
                    " on object ", object, "with arguments", args,
                    "\nparameter types", parameterTypes(), "\nargument types are", args);

        }

        catch ( Throwable ex ) {

            return Exceptions.handle(Object.class, ex, "unable to invoke method", method,
                    " on object ", object, "with arguments", args,
                    "\nparameter types", parameterTypes(), "\nargument types are", args);

        }
    }


    public Object invokeBound(Object... args) {
        try {
            return method.invoke( instance, args );
        } catch ( Throwable ex ) {

            return Exceptions.handle(Object.class, ex, "unable to invoke method", method,
                    " on object with arguments", args,
                    "\nparameter types", parameterTypes(), "\nargument types are");

        }
    }

    @Override
    public Object invokeStatic(Object... args) {
        try {

            return method.invoke(null, args);
        } catch ( Throwable ex ) {
            return Exceptions.handle(Object.class, ex, "unable to invoke method", method,
                    " with arguments", args);

        }
    }

    @Override
    public MethodAccess bind(Object instance) {
        Exceptions.die("Bind does not work for cached methodAccess make a copy with methodAccsess() first");
        return null;
    }

    @Override
    public MethodHandle methodHandle() {


        MethodHandle m;
        try {
            m = lookup.unreflect(method);

        } catch (Exception e) {
            m = null;
            Exceptions.handle(e);
        }

        return  m;
    }

    @Override
    public MethodAccess methodAccess() {
        if (methodHandle == null) {
            methodHandle = methodHandle();
        }
        return new MethodAccessImpl(this.method){


            @Override
            public MethodAccess bind(Object instance) {
                methodHandle.bindTo(instance);
                this.instance = instance;
                return this;
            }


            @Override
            public Object bound() {
                return instance;
            }


        };
    }

    @Override
    public Object bound() {
        return null;
    }

    @Override
    public <T> ConstantCallSite invokeReducerLongIntReturnLongMethodHandle(T object) {

        MethodType methodType = MethodType.methodType(long.class, long.class, int.class);
        try {
            return new ConstantCallSite(this.lookup.bind(object, this.name(), methodType));
        } catch (NoSuchMethodException e) {
            Exceptions.handle(e, "Method not found", this.name());
        } catch (IllegalAccessException e) {
            Exceptions.handle(e, "Illegal access to method", this.name());
        }
        return  null;
    }

    @Override
    public Method method() {
        return this.method;
    }

    @Override
    public int score() {
        return score;
    }


    @Override
    public Iterable<AnnotationData> annotationData() {
        return new Iterable<AnnotationData>() {
            @Override
            public Iterator<AnnotationData> iterator() {
                return annotationData.iterator();
            }
        };
    }

    @Override
    public boolean hasAnnotation( String annotationName ) {
        return this.annotationMap.containsKey( annotationName );
    }

    @Override
    public AnnotationData annotation(String annotationName) {
        return this.annotationMap.get(annotationName);
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }


    @Override
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }


    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(method.getModifiers());
    }

    @Override
    public String name() {
        return method.getName();
    }

    @Override
    public Class<?> declaringType() {
        return method.getDeclaringClass();
    }

    @Override
    public Class<?> returnType() {
        return method.getReturnType();
    }

    @Override
    public boolean respondsTo(Class<?>[] parametersToMatch) {
        boolean match = true;

        Class<?>[] parameterTypes = method.getParameterTypes();


        if ( parameterTypes.length != parametersToMatch.length ) {
            return false;
        }


        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> type = parameterTypes[index];
            Class<?> matchToType = parametersToMatch[index];
            if (type.isPrimitive()) {

                if (!(type == int.class &&  ( matchToType == Integer.class || matchToType == int.class) ||
                        type == boolean.class &&  ( matchToType == Boolean.class || matchToType == boolean.class) ||
                        type == long.class &&  ( matchToType == Long.class  || matchToType == long.class) ||
                        type == float.class &&  ( matchToType == Float.class   || matchToType == float.class) ||
                        type == double.class &&  ( matchToType == Double.class   || matchToType == double.class) ||
                        type == short.class &&  ( matchToType == Short.class   || matchToType == short.class) ||
                        type == byte.class &&  ( matchToType == Byte.class   || matchToType == byte.class) ||
                        type == char.class &&  ( matchToType == Character.class || matchToType == char.class) )
                )
                {
                    match = false;
                    break;
                }


            } else if (!type.isAssignableFrom( matchToType )) {
                match = false;
                break;
            }
        }

        return match;
    }

    @Override
    public boolean respondsTo(Object... args) {



        boolean match = true;
        Class<?>[] parameterTypes = method.getParameterTypes();



        if ( parameterTypes.length != args.length ) {
            return false;
        }

        for (int index = 0; index < parameterTypes.length; index++) {
            Object arg = args[index];
            Class<?> type = parameterTypes[index];
            Class<?> matchToType = arg != null ? arg.getClass() : null;

            if (type.isPrimitive()) {

                if (arg == null) {
                    match = false;
                    break;
                }
                if (!(type == int.class &&  matchToType == Integer.class ||
                        type == boolean.class &&  matchToType == Boolean.class ||
                        type == long.class &&  matchToType == Long.class   ||
                        type == float.class &&  matchToType == Float.class   ||
                        type == double.class &&  matchToType == Double.class   ||
                        type == short.class &&  matchToType == Short.class   ||
                        type == byte.class &&  matchToType == Byte.class   ||
                        type == char.class &&  matchToType == Character.class
                ))
                {
                    match = false;
                    break;
                }


            } else if (arg == null) {

            } else if (!type.isInstance( arg )) {
                match = false;
                break;
            }
        }

        return match;
    }


    @Override
    public Class<?>[] parameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return method.getGenericParameterTypes();
    }


    @Override
    public String toString() {
        return "MethodAccessImpl{" +
                "method=" + method +
                ", annotationData=" + annotationData +
                ", instance=" + instance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodAccessImpl that = (MethodAccessImpl) o;

        if (annotationData != null ? !annotationData.equals(that.annotationData) : that.annotationData != null)
            return false;
        if (annotationMap != null ? !annotationMap.equals(that.annotationMap) : that.annotationMap != null)
            return false;
        if (instance != null ? !instance.equals(that.instance) : that.instance != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (annotationData != null ? annotationData.hashCode() : 0);
        result = 31 * result + (annotationMap != null ? annotationMap.hashCode() : 0);
        result = 31 * result + (instance != null ? instance.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(MethodAccess o2) {

        if (this.score() > o2.score()) {
            return -1;
        } else if (this.score() < o2.score()){
            return 1;
        } else {
            return 0;
        }
    }


    public List<TypeType> paramTypeEnumList() {
        return paramTypeEnumList;
    }
}
